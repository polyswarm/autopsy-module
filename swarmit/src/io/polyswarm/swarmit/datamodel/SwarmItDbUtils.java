/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static functions to manage initialization, version, and closing of the 
 * database and connections.
 */
public class SwarmItDbUtils {
    private final static Logger LOGGER = Logger.getLogger(SwarmItDbUtils.class.getName());

    /**
     * Close the statement.
     *
     * @param statement The statement to be closed.
     *
     * @throws EamDbException
     */
    public static void closeStatement(Statement statement) {
        if (null != statement) {
            try {
                statement.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error closing Statement.", ex); // NON-NLS
            }
        }
    }

    /**
     * Close the resultSet.
     *
     * @param resultSet
     *
     * @throws EamDbException
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (null != resultSet) {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error closing ResultSet.", ex); // NON-NLS
            }
        }
    }

    /**
     * Close the in-use connection and return it to the pool.
     *
     * @param conn An open connection
     *
     * @throws EamDbException
     */
    public static void closeConnection(Connection conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error closing Connection.", ex); // NON-NLS
            }
        }
    }

    /**
     * Store the SCHEMA VERSION in the db_info table or update it.
     * 
     * @param conn An open database connection
     * 
     * @return true on success, else false
     */
    public static boolean updateSchemaVersion(Connection conn, String majorVersion, String minorVersion) {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sqlUpdate = "UPDATE db_info SET value=? WHERE id=?"; // NON-NLS
        String sqlInsert = "INSERT INTO db_info (name, value) VALUES (?, ?)"; // NON-NLS
        String sqlQueryMajor = "SELECT id FROM db_info WHERE name='SCHEMA_VERSION'"; // NON-NLS
        String sqlQueryMinor = "SELECT id FROM db_info WHERE name='SCHEMA_VERSION_MINOR'"; // NON-NLS
        try {
            preparedStatement = conn.prepareStatement(sqlQueryMajor);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                preparedStatement = conn.prepareStatement(sqlUpdate);
                preparedStatement.setString(1, majorVersion);
                preparedStatement.setInt(2, id);
                preparedStatement.executeQuery();
            } else {
                preparedStatement = conn.prepareStatement(sqlInsert);
                preparedStatement.setString(1, "SCHEMA_VERSION");
                preparedStatement.setString(2, majorVersion);
                preparedStatement.executeQuery();                
            }
            
            preparedStatement = conn.prepareStatement(sqlQueryMinor);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                preparedStatement = conn.prepareStatement(sqlUpdate);
                preparedStatement.setString(1, minorVersion);
                preparedStatement.setInt(2, id);
                preparedStatement.executeQuery();
            } else {
                preparedStatement = conn.prepareStatement(sqlInsert);
                preparedStatement.setString(1, "SCHEMA_VERSION_MINOR");
                preparedStatement.setString(2, minorVersion);
                preparedStatement.executeQuery();
            }            
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error adding schema version to db_info table.", ex); // NON-NLS
            return false;
        } finally {
            SwarmItDbUtils.closeStatement(preparedStatement);
            SwarmItDbUtils.closeResultSet(resultSet);
        }
        
        return true;
    }
            
    /**
     * Query to see if the SCHEMA_VERSION is set in the db.
     *
     * @param conn An open database connection
     * 
     * @return true if set, else false.
     */
    public static boolean schemaVersionIsSet(Connection conn) {
        if (null == conn) {
            return false;
        }

        ResultSet resultSet = null;
        try {
            Statement tester = conn.createStatement();
            String sql = "SELECT value FROM db_info WHERE name='SCHEMA_VERSION'"; // NON-NLS
            resultSet = tester.executeQuery(sql);
            if (resultSet.next()) {
                String value = resultSet.getString("value");
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            SwarmItDbUtils.closeResultSet(resultSet);
        }
        return true;
    }
    
    
    /**
     * Use the current settings and the validation query to test the connection
     * to the database.
     *
     * @return true if successful query execution, else false.
     */
    public static boolean executeValidationQuery(Connection conn, String validationQuery) {
        if (null == conn) {
            return false;
        }

        ResultSet resultSet = null;
        try {
            Statement tester = conn.createStatement();
            resultSet = tester.executeQuery(validationQuery);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            SwarmItDbUtils.closeResultSet(resultSet);
        }

        return false;
    }    
}
