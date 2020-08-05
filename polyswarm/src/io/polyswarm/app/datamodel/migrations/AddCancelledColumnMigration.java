/*
 * The MIT License
 *
 * Copyright 2020 PolySwarm PTE. LTD.
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
package io.polyswarm.app.datamodel.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Add cancelled column to pending_submissions, pending_hashes, and pending_rescans
 */
public class AddCancelledColumnMigration implements Migration {

    private final static Logger LOGGER = Logger.getLogger(AddCancelledColumnMigration.class.getName());
    private static final String ALTER_TABLE_FORMAT = "ALTER TABLE %s ADD cancelled boolean";
    private static final String PRAGMA_TABLE_INFO = "SELECT name from pragma_table_info(?)";
    private static final String COLUMN_NAME = "cancelled";

    private final String tableName;

    public AddCancelledColumnMigration(String table) {
        tableName = table;
    }

    @Override
    public void run(Connection connection) throws SQLException {
        if (!hasRun(connection)) {

            Statement statement = connection.createStatement();
            statement.execute(String.format(ALTER_TABLE_FORMAT, tableName));
        }
    }

    private boolean hasRun(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(PRAGMA_TABLE_INFO);
        preparedStatement.setString(1, tableName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String schemaColumn = resultSet.getString("name");
            LOGGER.log(Level.FINE, "Found {0}", schemaColumn);
            if (COLUMN_NAME.equals(schemaColumn)) {
                return true;
            }
        }

        return false;
    }
}
