/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
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
                LOGGER.log(Level.SEVERE, "Error closing Statement.", ex);
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
                LOGGER.log(Level.SEVERE, "Error closing ResultSet.", ex);
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
                LOGGER.log(Level.SEVERE, "Error closing Connection.", ex);
            }
        }
    }

    /**
     * Query to see if the SCHEMA_VERSION is set in the db.
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
            String sql = "SELECT value FROM db_info WHERE name='SCHEMA_VERSION'";
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
