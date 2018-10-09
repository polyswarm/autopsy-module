/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.datamodel;

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
public final class SwarmItDbSettings {
    
    private final static Logger LOGGER = Logger.getLogger(SwarmItDbSettings.class.getName());
    private final String DB_NAME = "swarmit.db"; // NON-NLS
    private final String DB_DIRECTORY_NAME = "polyswarm"; // NON-NLS
    private final String JDBC_DRIVER = "org.sqlite.JDBC"; // NON-NLS
    private final String JDBC_BASE_URI = "jdbc:sqlite:"; // NON-NLS
    private final String VALIDATION_QUERY = "SELECT count(*) from sqlite_master"; // NON-NLS
    private static final String PRAGMA_SYNC_OFF = "PRAGMA synchronous = OFF"; // NON-NLS
    private static final String PRAGMA_SYNC_NORMAL = "PRAGMA synchronous = NORMAL"; // NON-NLS
    private static final String PRAGMA_JOURNAL_WAL = "PRAGMA journal_mode = WAL"; // NON-NLS
    private static final String PRAGMA_READ_UNCOMMITTED_TRUE = "PRAGMA read_uncommitted = True"; // NON-NLS
    private static final String PRAGMA_ENCODING_UTF8 = "PRAGMA encoding = 'UTF-8'"; // NON-NLS
    private static final String PRAGMA_PAGE_SIZE_4096 = "PRAGMA page_size = 4096"; // NON-NLS
    private String baseDirPath;
    
    public SwarmItDbSettings() {
        
    }

    public boolean initialize() {
        try {
            baseDirPath = Case.getCurrentCaseThrows().getModuleDirectory();
        } catch (NoCurrentCaseException ex) {
            LOGGER.log(Level.SEVERE, "Cannot determine base directory for sqlite db. Case is not open.", ex); // NON-NLS
            return false;
        }
        
        if (createDbDirectory() == true) {
            boolean result = initializeDatabaseSchema()
                    && insertDefaultDatabaseContent();
            if (result == false) {
                LOGGER.log(Level.SEVERE, "Unable to initialize sqlite db."); // NON-NLS
            }
            return result;
        } else {
            LOGGER.log(Level.SEVERE, "Unable to initialize sqlite db. Failed to create base directory."); // NON-NLS
            return false;
        }
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
        if (! dbFile.exists()) {
            return false;
        }
        return ( ! dbFile.isDirectory());
    }

    /**
     * Get the full path to the db file in the db directory.
     * 
     * @return Full path
     */
    private String getDbFilePath() {
        return baseDirPath + File.separator + DB_NAME;
    }

    /**
     * Determine whether the db directory exists.
     * 
     * @return true if exists, else false
     */
    private boolean dbDirectoryExists() {
        File dbDir = new File(getDbDirectory());
        
        if (!dbDir.exists()) {
            return false;
        } else if (!dbDir.isDirectory()) {
            return false;
        }
        return true;
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
        
        boolean result = false;
        // TODO: run validation query
        // result = query...
        // close connection
        return result;
    }

    public boolean verifyDatabaseSchema() {
        Connection conn = getEphemeralConnection();
        if (null == conn) {
            return false;
        }
        
        boolean result = false;
        // TODO: run schema verision is set query
        // result = query...
        // close connection
        return result;
    }

    private boolean initializeDatabaseSchema() {

        StringBuilder createPendingLookupsTable = new StringBuilder();
        createPendingLookupsTable.append("CREATE TABLE IF NOT EXISTS pending_lookups (");
        createPendingLookupsTable.append("id integer primary key autoincrement NOT NULL,");
        createPendingLookupsTable.append("file_obj_id integer NOT NULL,");
        createPendingLookupsTable.append("txid integer NOT NULL,");
        createPendingLookupsTable.append("status integer NOT NULL,");
        createPendingLookupsTable.append("CONSTRAINT file_obj_id_unique UNIQUE (file_obj_id)");
        createPendingLookupsTable.append(")");

        StringBuilder createDbInfoTable = new StringBuilder();
        createDbInfoTable.append("CREATE TABLE IF NOT EXISTS db_info (");
        createDbInfoTable.append("id integer primary key NOT NULL,");
        createDbInfoTable.append("name text NOT NULL,");
        createDbInfoTable.append("value text NOT NULL");
        createDbInfoTable.append(")");

        Connection conn;
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
                    
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize sqlite db schema.", ex); // NON-NLS
            return false;
        } finally {
            // TODO close connection.
        }
        
        return true;
    }

    private boolean insertDefaultDatabaseContent() {
        
        return true;
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
