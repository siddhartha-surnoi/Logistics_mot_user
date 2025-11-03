package com.user.logistics.service;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/logistics_db");
        config.setUsername("postgres");
        config.setPassword("1998");
        config.setDriverClassName("org.postgresql.Driver");


        // Uncomment if needed, usually not required as it can be inferred from the URL
        // config.setDriverClassName("org.postgresql.Driver");

        // Optional Hikari settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
//        config.setConnectionTimeout(30000);
//        config.setIdleTimeout(30000);
//        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
