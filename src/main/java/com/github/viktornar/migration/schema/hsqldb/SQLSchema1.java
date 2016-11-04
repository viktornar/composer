package com.github.viktornar.migration.schema.hsqldb;

import com.github.viktornar.migration.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author v.nareiko
 */
public class SQLSchema1 extends Schema {
    private Logger logger = LoggerFactory.getLogger(SQLSchema1.class);

    @Override
    public void execute(JdbcTemplate template) {
        assert template != null;

        if (!tableExists(template, "version")) {
            logger.info("Database table 'version' not found.  Creating it.");

            template.execute("CREATE TABLE version (version INT NOT NULL)");
            template.execute("INSERT INTO version VALUES (1)");

            logger.info("Database table 'version' was created successfully.");
        }
    }

    @Override
    public void drop(JdbcTemplate template) {
        assert template != null;

        List<String> tablesToDrop = new ArrayList<>();

        if (tableExists(template, "version")) {
            logger.info("Database table 'version' was found. Adding it to drop list.");
            tablesToDrop.add("version");
        }

        drop(template, tablesToDrop);
    }
}
