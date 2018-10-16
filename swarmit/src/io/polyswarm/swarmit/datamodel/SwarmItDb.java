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
package io.polyswarm.swarmit.datamodel;

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
public class SwarmItDb {
    private final static Logger LOGGER = Logger.getLogger(SwarmItDb.class.getName());
    private static SwarmItDb instance;
    private BasicDataSource connectionPool = null;
    private final SwarmItDbSettings dbSettings;
    
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    
    /**
     * Get the singleton instance of SwarmItDb
     *
     * @return the singleton instance of SwarmItDb
     *
     * @throws SwarmItDbException
     */
    public synchronized static SwarmItDb getInstance() throws SwarmItDbException {
        if (instance == null) {
            instance = new SwarmItDb();
        }

        return instance;
    }

    /**
     * Constructor. Loads db settings.
     * 
     * @throws SwarmItDbException
     */
    private SwarmItDb() throws SwarmItDbException {
        dbSettings = new SwarmItDbSettings();
        dbSettings.initialize();
    }

    public void shutdownConnections() throws SwarmItDbException {
        try {
            synchronized (this) {
                if (null != connectionPool) {
                    connectionPool.close();
                    connectionPool = null; // for it to be re-created on next connect()
                }
                
            }
            // TODO: clearCaches(); // where did this come from?
        } catch (SQLException ex) {
            throw new SwarmItDbException("Failed to close existing database connections.", ex); // NON-NLS
        }
    }

    /**
     * Setup a connection pool for sqlite db connections.
     * 
     * @throws SwarmItDbException 
     */
    private void setupConnectionPool() throws SwarmItDbException {
        if (dbSettings.dbFileExists() == false) {
            throw new SwarmItDbException("Swarmit database missing."); // NON-NLS
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
     * @throws SwarmItDbException 
     */
    protected Connection connect() throws SwarmItDbException {
        synchronized (this) {
            if (connectionPool == null) {
                setupConnectionPool();
            }
            
            try {
                return connectionPool.getConnection();
            } catch (SQLException ex) {
                throw new SwarmItDbException("Error getting connectin from connection pool.", ex); // NON-NLS
            }
        }
    }
    
    /**
     * Add a new name/value pair to the db_info table
     * 
     * @param name  Key to set
     * @param value Value to set for the name/value key pair.
     * 
     * @throws SwarmItDbException 
     */
    public void newDbInfo(String name, String value) throws SwarmItDbException {
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
                throw new SwarmItDbException("Error adding name/value pair to db_info.", ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeConnection(conn);
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
     * @return  Value associated with the name/value key pair.
     * 
     * @throws SwarmItDbException 
     */
    public String getDbInfo(String name) throws SwarmItDbException {
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
                throw new SwarmItDbException("Error getting value for name.", ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeResultSet(resultSet);
                SwarmItDbUtils.closeConnection(conn);
            }
            return value;
        } finally {
            releaseSharedLock();
        }
    }
    
    /**
     * Update the value for a name in the db_info table.
     * 
     * @param name  Key to lookup
     * @param value Value to set for the name/value key pair
     */
    public void updateDbInfo(String name, String value) throws SwarmItDbException {
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
                throw new SwarmItDbException("Error updating value for name.", ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }
    
    /**
     * Add a new file to the pending_submissions table
     * 
     * @param abstractFileId  Autopsy AbstractFile ID number
     * @param submissionUUID  UUID returned in result of file submission to PolySwarm API 
     * 
     * @throws SwarmItDbException 
     */
    public void newPendingSubmission(Long abstractFileId, String submissionUUID) throws SwarmItDbException {
        try {
            acquireExclusiveLock();
            
            Connection conn = connect();
            
            PreparedStatement preparedStatement = null;
            String sql = "INSERT INTO pending_submissions (abstract_file_id, submission_uuid) VALUES (?, ?)";

            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, abstractFileId);
                preparedStatement.setString(2, submissionUUID);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new SwarmItDbException("Error adding new file to pending_submissions table.", ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Get the list of pending submissions from the pending_submissions table.
     * 
     * @return  List of SwarmItPendingSubmission's.
     * 
     * @throws SwarmItDbException 
     */
    public List<SwarmItPendingSubmission> getPendingSubmissions() throws SwarmItDbException {
        try {
            acquireSharedLock();
            
            Connection conn = connect();
            
            List<SwarmItPendingSubmission> pendingSubmissions = new ArrayList<>();
            SwarmItPendingSubmission psResult;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String sql = "SELECT abstract_file_id, submission_uuid from pending_submissions";
            try {
                preparedStatement = conn.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    psResult = getPendingSubmissionFromResultSet(resultSet);
                    pendingSubmissions.add(psResult);
                }
            } catch (SQLException ex) {
                throw new SwarmItDbException("Error getting all pending submissions.", ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeResultSet(resultSet);
                SwarmItDbUtils.closeConnection(conn);
            }
            return pendingSubmissions;
        } finally {
            releaseSharedLock();
        }
    }

    /**
     * Delete the pending submission from the pending_submissions table.
     * 
     * @param pendingSubmission SwarmItPendingSubmission object
     * 
     * @throws SwarmItDbException
     */
    public void deletePendingSubmission(SwarmItPendingSubmission pendingSubmission) throws SwarmItDbException {
        try {
            acquireExclusiveLock();
            
            Connection conn = connect();
            
            PreparedStatement preparedStatement = null;
            String sql = "DELETE from pending_submissions WHERE abstract_file_id=? AND submission_uuid=?";
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setLong(1, pendingSubmission.getAbstractFileID());
                preparedStatement.setString(2, pendingSubmission.getSubmissionUUID());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new SwarmItDbException("Error deleteing pending submission: " + pendingSubmission.toString(), ex); // NON-NLS
            } finally {
                SwarmItDbUtils.closeStatement(preparedStatement);
                SwarmItDbUtils.closeConnection(conn);
            }
        } finally {
            releaseExclusiveLock();
        }
    }

    /**
     * Convert a ResultSet into a PendingSubmission object.
     * 
     * @param resultSet ResultSet row returned from db query
     * @return  PendingSubmission object
     * @throws SQLException 
     */
    private SwarmItPendingSubmission getPendingSubmissionFromResultSet(ResultSet resultSet) throws SQLException {
        if (null == resultSet) {
            return null;
        }
        
        return new SwarmItPendingSubmission(resultSet.getLong("abstract_file_id"), resultSet.getString("submission_uuid"));
    }
    
    /**
     * Acquire the lock that provides exclusive access to the database.
     * Call this method in a try block with a call to the release method
     * in an associated finally block.
     */
    private void acquireExclusiveLock() {
        rwLock.writeLock().lock();
    }
    
    /**
     * Release the lock that provides exclusive access to the database.
     * This method should be called in the finally block of a try block
     * in which the lock was acquired.
     */
    private void releaseExclusiveLock() {
        rwLock.writeLock().unlock();
    }

    /**
     * Acquire the lock that provides shared access to the database.
     * Call this method in a try block with a call to the release method
     * in an associated finally block.
     */
    private void acquireSharedLock() {
        rwLock.readLock().lock();
    }

    /**
     * Release the lock that provides shared access to the database.
     * This method should be called in the finally block of a try block
     * in which the lock was acquired.
     */
    private void releaseSharedLock() {
        rwLock.readLock().unlock();
    }
}
