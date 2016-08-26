package com.github.viktornar.configuration;

import com.github.viktornar.migration.dao.DaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
@EnableScheduling
public class ApplicationConfig extends WebMvcConfigurerAdapter {
    private final Environment env;

    @Autowired
    public ApplicationConfig(
            Environment env
    ) {
        this.env = env;
    }

    /**
     * Get datasource for JDBC template.
     */
    @Profile("development")
    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("composer")
                .build();
    }

    /**
     * Helper bean to access data from data source (database) while development in process
     *
     * @param dataSource The data source
     * @return The helper data access object
     */
    @Bean
    DaoHelper daoHelperDev(DataSource dataSource) {
        // Use false here. If you specify true instead false you will drop all tables
        // after application shutdown.
        return new DaoHelper(dataSource, false);
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
