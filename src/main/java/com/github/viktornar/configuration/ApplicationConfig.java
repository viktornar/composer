/*
 This file is part of Composer.
 Composer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
 Copyright 2016 (C) Viktor Nareiko
 */
package com.github.viktornar.configuration;

import com.github.viktornar.dao.AtlasDao;
import com.github.viktornar.dao.ExtentDao;
import com.github.viktornar.migration.dao.DaoHelper;
import com.github.viktornar.service.repository.Repository;
import com.github.viktornar.service.repository.RepositoryDao;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

/**
 * Main application configuration.
 *
 * @author v.nareiko
 */
@Configuration
@Profile("default")
@ComponentScan({
        "com.github.viktornar.*"
})
@PropertySources({
        @PropertySource("classpath:application.properties")
})
@EnableAsync
public class ApplicationConfig extends WebMvcConfigurerAdapter {
    private final Environment env;

    @Autowired
    public ApplicationConfig(
            Environment env
    ) {
        this.env = env;
    }

    @Profile("development")
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:file:D:/Tmp/atlas/database/composer_db");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    /**
     * Helper bean to access data from data source (database) while development in process
     *
     * @param dataSource The data source
     * @return The helper data access object
     */
    @Bean
    DaoHelper daoHelper(DataSource dataSource) {
        // Use false here. If you specify true instead false you will drop all tables
        // after application shutdown.
        return new DaoHelper(dataSource, false);
    }

    @Bean
    Repository repository(AtlasDao atlasDao, ExtentDao extentDao){
        return new RepositoryDao(atlasDao, extentDao);
    }

    /**
     * Add resource handlers for web jars.
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/META-INF/web-resources/");
    }
}
