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

        if (!tableExists(template, "extent")) {
            logger.info("Database table 'extent' not found.  Creating it.");

            template.execute(
                    "CREATE TABLE extent (" +
                            "  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
                            "  xmin DOUBLE," +
                            "  ymin DOUBLE," +
                            "  xmax DOUBLE," +
                            "  ymax DOUBLE)"
            );

            logger.info("Database table 'extent' was created successfully.");
        }

        if (!tableExists(template, "atlas")) {
            logger.info("Database table 'atlas' not found.  Creating it.");

            template.execute(
                    "CREATE TABLE atlas (" +
                            "  id VARCHAR(10) NOT NULL PRIMARY KEY," +
                            "  atlas_name VARCHAR(50)," +
                            "  atlas_folder VARCHAR(1024)," +
                            "  orientation VARCHAR(10)," +
                            "  size VARCHAR(10)," +
                            "  zoom INT," +
                            "  columns INT," +
                            "  rows INT," +
                            "  progress INT," +
                            "  extent_id INT NOT NULL," +
                            "  CONSTRAINT fk_atlas_extent FOREIGN KEY(extent_id) REFERENCES extent(ID) ON DELETE CASCADE)"
            );

            logger.info("Database table 'atlas' was created successfully.");
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

        if (tableExists(template, "atlas")) {
            logger.info("Database table 'version' was found. Adding it to drop list.");
            tablesToDrop.add("extent");
        }

        if (tableExists(template, "extent")) {
            logger.info("Database table 'version' was found. Adding it to drop list.");
            tablesToDrop.add("version");
        }

        drop(template, tablesToDrop);
    }
}
