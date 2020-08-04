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

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Create a table of pending hash lookups
 */
public class CreatePendingHashLookupMigration implements Migration {

    @Override
    public void run(Statement statement) throws SQLException {
        StringBuilder createPendingHashLookupTable = new StringBuilder();
        createPendingHashLookupTable.append("CREATE TABLE IF NOT EXISTS pending_hashes(");
        createPendingHashLookupTable.append("id integer primary key autoincrement NOT NULL,");
        createPendingHashLookupTable.append("abstract_file_id integer NOT NULL,");
        createPendingHashLookupTable.append("md5_hash text NOT NULL,");
        createPendingHashLookupTable.append("CONSTRAINT abstract_file_id_unique UNIQUE (abstract_file_id)");
        createPendingHashLookupTable.append(")");

        statement.execute(createPendingHashLookupTable.toString());
    }

}
