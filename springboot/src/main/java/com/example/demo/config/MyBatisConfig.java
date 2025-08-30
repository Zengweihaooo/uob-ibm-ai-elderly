package com.example.demo.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * MyBatis Configuration for LocalDateTime type handling
 * 
 * This configuration ensures that LocalDateTime fields are properly
 * handled when converting between Java objects and database records.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Configuration
public class MyBatisConfig {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void configureTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        
        // Register LocalDateTime type handler
        registry.register(LocalDateTime.class, SQLiteLocalDateTimeTypeHandler.class);
        
        System.out.println("MyBatis LocalDateTime type handler registered successfully");
    }
}
