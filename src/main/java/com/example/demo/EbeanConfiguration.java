package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

@Configuration
public class EbeanConfiguration {

    @Bean
    public Database database() {
        DatabaseConfig config = new DatabaseConfig();
        config.setDataSourceConfig(dataSourceConfig());
        config.setDefaultServer(true);
        config.setDdlGenerate(true);
        config.setDdlRun(false);
        config.setRunMigration(false);
        return DatabaseFactory.create(config);
    }

    private io.ebean.datasource.DataSourceConfig dataSourceConfig() {
        io.ebean.datasource.DataSourceConfig dsConfig = new io.ebean.datasource.DataSourceConfig();
        dsConfig.setUrl("jdbc:sqlite:./mydatabase.db");
        dsConfig.setDriver("org.sqlite.JDBC");
        dsConfig.setUsername("");
        dsConfig.setPassword("");
        return dsConfig;
    }
}
