package com.huawei.aitransform.config;

import com.huawei.aitransform.util.CryptoUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置类
 * 支持自动解密数据库用户名和密码
 */
@Configuration
public class DataSourceConfig {
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;
    
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;
    
    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;
    
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;
    
    /**
     * 创建数据源Bean
     * 自动解密数据库用户名和密码
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        
        // 自动解密用户名（如果是加密格式）
        String decryptedUsername = CryptoUtil.decryptIfNeeded(username);
        config.setUsername(decryptedUsername);
        
        // 自动解密密码（如果是加密格式）
        String decryptedPassword = CryptoUtil.decryptIfNeeded(password);
        config.setPassword(decryptedPassword);
        
        // HikariCP连接池配置
        config.setMinimumIdle(minimumIdle);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        return new HikariDataSource(config);
    }
}

