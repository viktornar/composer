package com.github.viktornar.migration.schema;

import static org.apache.commons.lang3.StringUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;

import java.util.List;

/**
 * Used for creating and evolving the database schema.
 *
 * @author Sindre Mehus
 * @author v.nareiko
 */
public abstract class Schema {
    Logger logger = LoggerFactory.getLogger(Schema.class);

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
            template.execute(String.format("select 1 from %s", table));
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
            template.execute(String.format("select %s from %s where 1 = 0", column, table));
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

    protected boolean rowExists( JdbcTemplate template, String whereClause, String table) {
        assert template != null;
        assert whereClause != null && !whereClause.isEmpty();
        assert table != null && !table.isEmpty();

        try {
            Integer rowCount = template.queryForObject(
                    String.format("select count(*) from %s where %s", table, whereClause),
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
            String dropSql = String.format("DROP TABLE %s", tableList);
            template.execute(dropSql);
            logger.info(String.format("Database tables '%s' was dropped successfully.", tableList));
        } else {
            logger.info(String.format("There are no tables to drop in database."));
        }
    }
}
