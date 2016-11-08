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
package com.github.viktornar.migration.dao;

import javax.sql.DataSource;

import com.github.viktornar.migration.schema.Schema;
import com.github.viktornar.migration.schema.hsqldb.SQLSchema1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DAO helper class which creates the data source, and updates the database schema.
 *
 * @author Sindre Mehus
 * @author v.nareiko
 */
@Component
public class DaoHelper {
    Logger logger = LoggerFactory.getLogger(DaoHelper.class);
    private Schema[] schemas = {
            new SQLSchema1()
    };
    private static boolean shutdownHookAdded;
    private DataSource dataSource;

    public DaoHelper(){

    }

    public DaoHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DaoHelper(DataSource dataSource, boolean dropTablesAndAddShutdownHook) {
        this.dataSource = dataSource;
        if (dropTablesAndAddShutdownHook) {
            dropTables();
            addShutdownHook();
        }
        checkDatabase();
    }

    /**
     * Clean up (drops) all tables while development mode is one after application shutdown
     */
    private void addShutdownHook() {
        if (shutdownHookAdded) {
            return;
        }
        shutdownHookAdded = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                dropTables();
            }
        });
    }

    /**
     * Returns a JDBC template for performing database operations.
     *
     * @return A JDBC template.
     */
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Similar to {@link #getJdbcTemplate()}, but with named parameters.
     *
     * @return A JDBC template.
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Checks database and if application tables doesn't exist create them.
     */
    public void checkDatabase() {
        logger.info("Checking database schema.");
        try {
            for (Schema schema : schemas) {
                schema.execute(getJdbcTemplate());
            }
            logger.info("Done checking database schema.");
        } catch (Exception e) {
            logger.error("Failed to initialize database.");
            e.printStackTrace();
        }
    }

    /**
     * Drops database tables
     */
    public void dropTables() {
        logger.info("Dropping tables schema.");
        try {
            for (Schema schema : schemas) {
                schema.drop(getJdbcTemplate());
            }
            logger.info("Done dropping database tables.");
        } catch (Exception e) {
            logger.error("Failed to drop database tables.");
            e.printStackTrace();
        }
    }
}
