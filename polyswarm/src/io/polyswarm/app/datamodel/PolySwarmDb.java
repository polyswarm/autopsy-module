/*
 * The MIT License
 *
 * Copyright 2018 PolySwarm PTE. LTD.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.polyswarm.app.datamodel;

import io.polyswarm.app.tasks.PendingHashLookup;
import io.polyswarm.app.tasks.PendingRescan;
import io.polyswarm.app.tasks.PendingSubmission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Manage the database content and connections to the database.
 *
 * Note: code largely based off of Autopsy's centralrepository sqlite implementation.
 */
public class PolySwarmDb {

    private final static Logger LOGGER = Logger.getLogger(PolySwarmDb.class.getName());
    private final static String FIRST_SCAN_KEY = "FIRST_SCAN";
    private static PolySwarmDb instance;
    private BasicDataSource connectionPool = null;
    private final PolySwarmDbSettings dbSettings;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    /**
     * Get the singleton instance of PolySwarmDb
     *
     * @return the singleton instance of PolySwarmDb
     *
     * @throws PolySwarmDbException
     */
    public synchronized static PolySwarmDb getInstance() throws PolySwarmDbException {
        if (instance == null) {
            instance = new PolySwarmDb();
        }

        return instance;
    }

    /**
     * Constructor. Loads db settings.
     *
     * @throws PolySwarmDbException
     */
    private PolySwarmDb() throws PolySwarmDbException {
        dbSettings = new PolySwarmDbSettings();
        dbSettings.initialize();
    }

    public void shutdownConnections() throws PolySwarmDbException {
        try {
            synchronized (this) {
                if (null != connectionPool) {
                    connectionPool.close();
                    connectionPool = null; // for it to be re-created on next connect()
                }

            }
            // TODO: clearCaches(); // where did this come from?
        } catch (SQLException ex) {
            throw new PolySwarmDbException("Failed to close existing database connections.", ex); // NON-NLS
        }
    }

    /**
     * Setup a connection pool for sqlite db connections.
     *
     * @throws PolySwarmDbException
     */
    private void setupConnectionPool() throws PolySwarmDbException {
        if (dbSettings.dbFileExists() == false) {
            throw new PolySwarmDbException("PolySwarm database missing."); // NON-NLS
        }

        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName(dbSettings.getDriver());
        connectionPool.setUrl(dbSettings.getConnectionURL());

        // adjust pool configuration
        connectionPool.setInitialSize(4);
        connectionPool.setMaxWaitMillis(1000);
        connectionPool.setValidationQuery(dbSettings.getValidationQuery());
        connectionPool.setConnectionInitSqls(Arrays.asList("PRAGMA foreign_keys = ON"));
    }

    /**
     * Lazily setup Singleton connection on first connection request
     *
     * @return A connection from the connection pool
     *
     * @throws PolySwarmDbException
     */
    protected Connection connect() throws PolySwarmDbException {
        synchronized (this) {
            if (connectionPool == null) {
                setupConnectionPool();
            }

            try {
                return connectionPool.getConnection();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting connectin from connection pool.", ex); // NON-NLS
            }
        }
    }

    /**
     * Check if any scans have been performed
     *
     * @return boolean, true if first scan
     */
    public boolean isFirstScan() {
        try {
            String value = getDbInfo(FIRST_SCAN_KEY);
            return value == null;
        } catch (PolySwarmDbException e) {
            return true;
        }
    }

    /**
     * Mark that a scan has been performed
     *
     */
    public void approvedScan() throws PolySwarmDbException {
        newDbInfo(FIRST_SCAN_KEY, "");
    }

    /**
     * Add a new name/value pair to the db_info table
     *
     * @param name Key to set
     * @param value Value to set for the name/value key pair.
     *
     * @throws PolySwarmDbException
     */
    public void newDbInfo(String name, String value) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "INSERT INTO db_info (name, value) VALUES (?, ?)";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, value);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error adding name/value pair to db_info.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Get the value for the given name (key) from the db_info table.
     *
     * @param name Key to lookup
     *
     * @return Value associated with the name/value key pair.
     *
     * @throws PolySwarmDbException
     */
    public String getDbInfo(String name) throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String value = null;
            String sql = "SELECT value from db_info WHERE name=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, name);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    value = resultSet.getString("value");
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting value for name.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return value;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Update the value for a name in the db_info table.
     *
     * @param name Key to lookup
     * @param value Value to set for the name/value key pair
     */
    public void updateDbInfo(String name, String value) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "UPDATE db_info SET value=? WHERE name=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, name);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error updating value for name.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Add a new file to the pending_submissions table
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @param submissionUUID UUID returned in result of file submission to PolySwarm API
     *
     * @throws PolySwarmDbException
     */
    public void newPendingSubmission(Long abstractFileId) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "INSERT INTO pending_submissions (abstract_file_id, submission_uuid) VALUES (?, ?)";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                preparedStatement.setString(2, "");
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error adding new file to pending_submissions table.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Add a new file to the pending_submissions table
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @param submissionUUID UUID returned in result of file submission to PolySwarm API
     *
     * @throws PolySwarmDbException
     */
    public void updatePendingSubmissionId(Long abstractFileId, String submissionUUID) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "UPDATE pending_submissions SET submission_uuid=? WHERE abstract_file_id=?";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, submissionUUID);
                preparedStatement.setLong(2, abstractFileId);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error updating pending_submissions table.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Check to see if a file is already in the pending_submissions table.
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @return Boolean true if in the table, else false.
     *
     * @throws PolySwarmDbException
     */
    public Boolean isPendingSubmission(Long abstractFileId) throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            Boolean isFound = false;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT count(*) as quantity FROM pending_submissions WHERE abstract_file_id=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Long count = resultSet.getLong("quantity");
                    if (count > 0) {
                        isFound = true;
                    }
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending submissions.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return isFound;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Get the list of pending submissions from the pending_submissions table.
     *
     * @return List of PendingSubmission's.
     *
     * @throws PolySwarmDbException
     */
    public List<PendingSubmission> getPendingSubmissions() throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            List<PendingSubmission> pendingSubmissions = new ArrayList<>();
            PendingSubmission psResult;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT abstract_file_id, submission_uuid FROM pending_submissions";
            try {
                preparedStatement = conn.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    psResult = getPendingSubmissionFromResultSet(resultSet);
                    pendingSubmissions.add(psResult);
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending submissions.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return pendingSubmissions;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Delete the pending submission from the pending_submissions table.
     *
     * @param pendingSubmission PendingSubmission object
     *
     * @throws PolySwarmDbException
     */
    public void deletePendingSubmission(PendingSubmission pendingSubmission) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "DELETE from pending_submissions WHERE abstract_file_id=? AND submission_uuid=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, pendingSubmission.getAbstractFileId());
                preparedStatement.setString(2, pendingSubmission.getSubmissionId());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error deleteing pending submission: " + pendingSubmission.toString(), ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Convert a ResultSet into a PendingSubmission object.
     *
     * @param resultSet ResultSet row returned from db query
     * @return PendingSubmission object
     * @throws SQLException
     */
    private PendingSubmission getPendingSubmissionFromResultSet(ResultSet resultSet) throws SQLException {
        if (null == resultSet) {
            return null;
        }

        return new PendingSubmission(resultSet.getLong("abstract_file_id"), resultSet.getString("submission_uuid"));
    }

    /**
     * Add a new file to the pending_submissions table
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @param submissionUUID UUID returned in result of file submission to PolySwarm API
     *
     * @throws PolySwarmDbException
     */
    public void newPendingRescan(Long abstractFileId, String sha256Hash) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "INSERT INTO pending_rescans (abstract_file_id, sha256_hash, rescan_uuid) VALUES (?, ?, ?)";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                preparedStatement.setString(2, sha256Hash);
                preparedStatement.setString(3, "");
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error adding new file to pending_rescans table.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Add a new file to the pending_submissions table
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @param rescanUuid UUID returned in result of file submission to PolySwarm API
     *
     * @throws PolySwarmDbException
     */
    public void updatePendingRescanId(Long abstractFileId, String rescanUuid) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "UPDATE pending_rescans SET rescan_uuid=? WHERE abstract_file_id=?";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, rescanUuid);
                preparedStatement.setLong(2, abstractFileId);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error updating pending_rescans table.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Check to see if a file is already in the pending_submissions table.
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @return Boolean true if in the table, else false.
     *
     * @throws PolySwarmDbException
     */
    public Boolean isPendingRescan(Long abstractFileId) throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            Boolean isFound = false;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT count(*) as quantity FROM pending_rescans WHERE abstract_file_id=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Long count = resultSet.getLong("quantity");
                    if (count > 0) {
                        isFound = true;
                    }
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending rescans.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return isFound;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Get the list of pending submissions from the pending_submissions table.
     *
     * @return List of PendingSubmission's.
     *
     * @throws PolySwarmDbException
     */
    public List<PendingRescan> getPendingRescans() throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            List<PendingRescan> pendingRescans = new ArrayList<>();
            PendingRescan psResult;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT abstract_file_id, sha256_hash, rescan_uuid FROM pending_rescans";
            try {
                preparedStatement = conn.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    psResult = getPendingRescanFromResultSet(resultSet);
                    pendingRescans.add(psResult);
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending rescans.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return pendingRescans;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Delete the pending submission from the pending_submissions table.
     *
     * @param pendingRescan PendingRescan object
     *
     * @throws PolySwarmDbException
     */
    public void deletePendingRescan(PendingRescan pendingRescan) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "DELETE from pending_rescans WHERE abstract_file_id=? AND rescan_uuid=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, pendingRescan.getAbstractFileId());
                preparedStatement.setString(2, pendingRescan.getRescanId());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error deleteing pending submission: " + pendingRescan.toString(), ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Convert a ResultSet into a PendingSubmission object.
     *
     * @param resultSet ResultSet row returned from db query
     * @return PendingSubmission object
     * @throws SQLException
     */
    private PendingRescan getPendingRescanFromResultSet(ResultSet resultSet) throws SQLException {
        if (null == resultSet) {
            return null;
        }

        return new PendingRescan(resultSet.getLong("abstract_file_id"), resultSet.getString("sha256_hash"), resultSet.getString("rescan_uuid"));
    }

    /**
     * Add a new file to the pending_hashes table
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @param md5Hash hash of the file
     *
     * @throws PolySwarmDbException
     */
    public void newPendingHashLookup(Long abstractFileId, String md5Hash) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "INSERT INTO pending_hashes (abstract_file_id, md5_hash) VALUES (?, ?)";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                preparedStatement.setString(2, md5Hash);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error adding new file to pending_hashes table.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Check to see if a hash is already in the pending_hashes table.
     *
     * @param abstractFileId Autopsy AbstractFile ID number
     * @return Boolean true if in the table, else false.
     *
     * @throws PolySwarmDbException
     */
    public Boolean isPendingHashLookup(Long abstractFileId) throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            Boolean isFound = false;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT count(*) as quantity FROM pending_hashes WHERE abstract_file_id=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Long count = resultSet.getLong("quantity");
                    if (count > 0) {
                        isFound = true;
                    }
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending hashes.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return isFound;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Get the list of pending hashes from the pending_hashes table.
     *
     * @return List of PendingHashLookups's.
     *
     * @throws PolySwarmDbException
     */
    public List<PendingHashLookup> getPendingHashLookups() throws PolySwarmDbException {
        try {
            acquireSharedLock();

            Connection conn = connect();

            List<PendingHashLookup> pendingHashLookups = new ArrayList<>();
            PendingHashLookup psResult;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT abstract_file_id, md5_hash FROM pending_hashes";
            try {
                preparedStatement = conn.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    psResult = getPendingHashLookupFromResultSet(resultSet);
                    pendingHashLookups.add(psResult);
                }
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error getting all pending submissions.", ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeResultSet(resultSet);
                PolySwarmDbUtils.closeConnection(conn);
            }
            return pendingHashLookups;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Delete the pending submission from the pending_submissions table.
     *
     * @param pendingSubmission PendingSubmission object
     *
     * @throws PolySwarmDbException
     */
    public void deletePendingHashLookup(PendingHashLookup pendingHashLookup) throws PolySwarmDbException {
        try {
            acquireExclusiveLock();

            Connection conn = connect();

            PreparedStatement preparedStatement = null;
            String sql = "DELETE from pending_hashes WHERE abstract_file_id=? AND md5_hash=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, pendingHashLookup.getAbstractFileId());
                preparedStatement.setString(2, pendingHashLookup.getMd5Hash());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new PolySwarmDbException("Error deleteing pending hash look up: " + pendingHashLookup.toString(), ex); // NON-NLS
            } finally {
                PolySwarmDbUtils.closeStatement(preparedStatement);
                PolySwarmDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Convert a ResultSet into a PendingHashLookupobject.
     *
     * @param resultSet ResultSet row returned from db query
     * @return PendingHashLookup object
     * @throws SQLException
     */
    private PendingHashLookup getPendingHashLookupFromResultSet(ResultSet resultSet) throws SQLException {
        if (null == resultSet) {
            return null;
        }

        return new PendingHashLookup(resultSet.getLong("abstract_file_id"), resultSet.getString("md5_hash"));
    }

    /**
     * Acquire the lock that provides exclusive access to the database. Call this method in a try block with a call to
     * the release method in an associated finally block.
     */
    private void acquireExclusiveLock() {
        rwLock.writeLock().lock();
    }

    /**
     * Release the lock that provides exclusive access to the database. This method should be called in the finally
     * block of a try block in which the lock was acquired.
     */
    private void releaseExclusiveLock() {
        rwLock.writeLock().unlock();
    }

    /**
     * Acquire the lock that provides shared access to the database. Call this method in a try block with a call to the
     * release method in an associated finally block.
     */
    private void acquireSharedLock() {
        rwLock.readLock().lock();
    }

    /**
     * Release the lock that provides shared access to the database. This method should be called in the finally block
     * of a try block in which the lock was acquired.
     */
    private void releaseSharedLock() {
        rwLock.readLock().unlock();
    }
}
