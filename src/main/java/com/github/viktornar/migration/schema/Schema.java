/*
 This file is part of Composer.
 Composer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 Composer is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Composer.  If not, see <http://www.gnu.org/licenses/>.
 Copyright 2016 (C) Viktor Nareiko
 */

package com.github.viktornar.migration.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Used for creating and evolving the database schema.
 *
 * @author Sindre Mehus
 * @author v.nareiko
 */
public abstract class Schema {
    private Logger logger = LoggerFactory.getLogger(Schema.class);

    /**
     * Executes this schema.
     *
     * @param template The JDBC template to use.
     */
    public abstract void execute(JdbcTemplate template);

    /**
     * Executes this schema.
     *
     * @param template The JDBC template to use.
     */
    public abstract void drop(JdbcTemplate template);

    /**
     * Returns whether the given table exists.
     *
     * @param template The JDBC template to use.
     * @param table    The table in question.
     * @return Whether the table exists.
     */
    protected boolean tableExists(JdbcTemplate template, String table) {
        assert template != null;
        assert table != null && !table.isEmpty();

        try {
            template.execute(format("select 1 from %s", table));
        } catch (Exception x) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether the given column in the given table exists.
     *
     * @param template The JDBC template to use.
     * @param column   The column in question.
     * @param table    The table in question.
     * @return Whether the column exists.
     */
    protected boolean columnExists(JdbcTemplate template, String column, String table) {
        assert template != null;
        assert column != null && !column.isEmpty();
        assert table != null && !table.isEmpty();

        try {
            template.execute(format("select %s from %s where 1 = 0", column, table));
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    /**
     * Check if row exist in given table.
     *
     * @param template    The JDBC template to use.
     * @param whereClause The where clause
     * @param table       The table in question
     * @return Whether the row exists.
     */

    protected boolean rowExists(JdbcTemplate template, String whereClause, String table) {
        assert template != null;
        assert whereClause != null && !whereClause.isEmpty();
        assert table != null && !table.isEmpty();

        try {
            Integer rowCount = template.queryForObject(
                    format("select count(*) from %s where %s", table, whereClause),
                    Integer.class
            );
            return rowCount > 0;
        } catch (Exception x) {
            return false;
        }
    }

    /**
     * Drops given tables by given names of tables.
     *
     * @param template
     * @param tablesToDrop
     */
    protected void drop(JdbcTemplate template, List<String> tablesToDrop) {
        assert template != null;
        assert tablesToDrop != null;

        if (tablesToDrop.size() > 0) {
            String tableList = join(tablesToDrop.toArray(), ", ");
            String dropSql = format("DROP TABLE %s", tableList);
            template.execute(dropSql);
            logger.info(format("Database tables '%s' was dropped successfully.", tableList));
        } else {
            logger.info(format("There are no tables to drop in database."));
        }
    }
}
