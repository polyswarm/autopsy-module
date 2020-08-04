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

import io.polyswarm.app.datamodel.migrations.AddCancelledColumnMigration;
import io.polyswarm.app.datamodel.migrations.CreatePendingHashLookupMigration;
import io.polyswarm.app.datamodel.migrations.CreatePendingRescanMigration;
import io.polyswarm.app.datamodel.migrations.CreatePendingSubmissionMigration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;

/**
 * Manage settings for the sqlite db it uses.
 *
 * REQUIRES AN OPEN CASE!
 */
public final class PolySwarmDbSettings {

    private final static Logger LOGGER = Logger.getLogger(PolySwarmDbSettings.class.getName());
    private final String DB_NAME = "polyswarm.db"; // NON-NLS
    private final String DB_DIRECTORY_NAME = "polyswarm"; // NON-NLS
    private final String JDBC_DRIVER = "org.sqlite.JDBC"; // NON-NLS
    private final String JDBC_BASE_URI = "jdbc:sqlite:"; // NON-NLS
    private final String VALIDATION_QUERY = "SELECT count(*) from sqlite_master"; // NON-NLS
    private static final Integer DB_SCHEMA_VERSION_MAJOR = 2;
    private static final Integer DB_SCHEMA_VERSION_MINOR = 0;
    private static final String PRAGMA_SYNC_OFF = "PRAGMA synchronous = OFF"; // NON-NLS
    private static final String PRAGMA_SYNC_NORMAL = "PRAGMA synchronous = NORMAL"; // NON-NLS
    private static final String PRAGMA_JOURNAL_WAL = "PRAGMA journal_mode = WAL"; // NON-NLS
    private static final String PRAGMA_READ_UNCOMMITTED_TRUE = "PRAGMA read_uncommitted = True"; // NON-NLS
    private static final String PRAGMA_ENCODING_UTF8 = "PRAGMA encoding = 'UTF-8'"; // NON-NLS
    private static final String PRAGMA_PAGE_SIZE_4096 = "PRAGMA page_size = 4096"; // NON-NLS
    private String baseDirPath;

    public PolySwarmDbSettings() {

    }

    public boolean initialize() {
        try {
            baseDirPath = Case.getCurrentCaseThrows().getModuleDirectory();
        } catch (NoCurrentCaseException ex) {
            LOGGER.log(Level.SEVERE, "Cannot determine base directory for sqlite db. Case is not open.", ex); // NON-NLS
            return false;
        }

        if (!createDbDirectory()) {
            LOGGER.log(Level.SEVERE, "Unable to initialize sqlite db. Failed to create base directory."); // NON-NLS
            return false;
        }

        boolean result = initializeDatabaseSchema() && insertDefaultDatabaseContent();
        if (!result) {
            LOGGER.log(Level.SEVERE, "Unable to initialize sqlite db."); // NON-NLS
        }
        return result;

    }

    private boolean createDbDirectory() {
        if (!dbDirectoryExists()) {
            try {
                File dbDir = new File(getDbDirectory());
                Files.createDirectories(dbDir.toPath());
                LOGGER.log(Level.INFO, "sqlite directory did not exist, create it at {0}.", getDbDirectory()); // NON-NLS
            } catch (IOException | InvalidPathException | SecurityException exs) {
                LOGGER.log(Level.SEVERE, "Failed to create sqlite database directory.", exs); // NON-NLS
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether the db file exists.
     *
     * @return true if exists, else false
     */
    public boolean dbFileExists() {
        File dbFile = new File(getDbFilePath());
        if (!dbFile.exists()) {
            return false;
        }
        return (!dbFile.isDirectory());
    }

    /**
     * Get the full path to the db file in the db directory.
     *
     * @return Full path
     */
    private String getDbFilePath() {
        return getDbDirectory() + File.separator + DB_NAME;
    }

    /**
     * Determine whether the db directory exists.
     *
     * @return true if exists, else false
     */
    private boolean dbDirectoryExists() {
        File dbDir = new File(getDbDirectory());

        return dbDir.exists() && dbDir.isDirectory();
    }

    /**
     * Get the full path to the db directory.
     *
     * @return Full path
     */
    private String getDbDirectory() {
        return baseDirPath + File.separator + DB_DIRECTORY_NAME;
    }

    public String getConnectionURL() {
        StringBuilder connUrl = new StringBuilder();
        connUrl.append(getJDBCBaseURI());
        connUrl.append(getDbFilePath());
        return connUrl.toString();
    }

    /**
     * Get an ephemeral client connection for db initialization and testing.
     *
     * @return Connection or null
     */
    private Connection getEphemeralConnection() {
        if (!dbDirectoryExists()) {
            return null;
        }

        Connection conn;
        try {
            String url = getConnectionURL();
            Class.forName(getDriver());
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to acquire ephemeral connection to sqlite.", ex); // NON-NLS
            conn = null;
        }

        return conn;
    }

    /**
     * Verify connection to the database
     *
     * @return true if successful connection, else false
     */
    public boolean verifyConnection() {
        Connection conn = getEphemeralConnection();
        if (null == conn) {
            return false;
        }

        boolean result = PolySwarmDbUtils.executeValidationQuery(conn, VALIDATION_QUERY);
        PolySwarmDbUtils.closeConnection(conn);
        return result;
    }

    public boolean verifyDatabaseSchema() {
        Connection conn = getEphemeralConnection();
        if (null == conn) {
            return false;
        }

        boolean result = PolySwarmDbUtils.schemaVersionIsSet(conn);
        PolySwarmDbUtils.closeConnection(conn);
        return result;
    }

    private boolean initializeDatabaseSchema() {

        StringBuilder createDbInfoTable = new StringBuilder();
        createDbInfoTable.append("CREATE TABLE IF NOT EXISTS db_info (");
        createDbInfoTable.append("id integer primary key NOT NULL,");
        createDbInfoTable.append("name text NOT NULL,");
        createDbInfoTable.append("value text NOT NULL");
        createDbInfoTable.append(")");

        Connection conn = null;
        try {
            conn = getEphemeralConnection();
            if (null == conn) {
                return false;
            }

            Statement stmt = conn.createStatement();
            stmt.execute(PRAGMA_JOURNAL_WAL);
            stmt.execute(PRAGMA_SYNC_OFF);
            stmt.execute(PRAGMA_READ_UNCOMMITTED_TRUE);
            stmt.execute(PRAGMA_ENCODING_UTF8);
            stmt.execute(PRAGMA_PAGE_SIZE_4096);

            stmt.execute(createDbInfoTable.toString());

            runMigrations(stmt);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize sqlite db schema.", ex); // NON-NLS
            return false;
        } finally {
            PolySwarmDbUtils.closeConnection(conn);
        }

        return true;
    }

    /**
     * Runs a set of migrations in a specific order Runs synchronously
     *
     */
    private void runMigrations(Statement statement) throws SQLException {
        new CreatePendingSubmissionMigration().run(statement);
        new CreatePendingHashLookupMigration().run(statement);
        new CreatePendingRescanMigration().run(statement);
        new AddCancelledColumnMigration().run(statement);
    }

    /**
     * Insert initial/default content into the database. Here we insert the database schema major and minor version.
     *
     * @return true on success, else false
     */
    private boolean insertDefaultDatabaseContent() {

        Connection conn = null;
        try {
            conn = getEphemeralConnection();
            if (null == conn) {
                return false;
            }

            return PolySwarmDbUtils.updateSchemaVersion(conn,
                    DB_SCHEMA_VERSION_MAJOR.toString(),
                    DB_SCHEMA_VERSION_MINOR.toString());
        } finally {
            PolySwarmDbUtils.closeConnection(conn);
        }
    }

    /**
     * @return the DRIVER
     */
    String getDriver() {
        return JDBC_DRIVER;
    }

    /**
     * @return the VALIDATION_QUERY
     */
    String getValidationQuery() {
        return VALIDATION_QUERY;
    }

    /**
     * @return the JDBC_BASE_URI
     */
    String getJDBCBaseURI() {
        return JDBC_BASE_URI;
    }
}
