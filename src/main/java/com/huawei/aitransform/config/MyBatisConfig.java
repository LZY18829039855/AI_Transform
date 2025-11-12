package com.huawei.aitransform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 */
@Configuration
@MapperScan("com.huawei.aitransform.mapper")
public class MyBatisConfig {
    // MyBatis配置已通过application.yml完成
}

