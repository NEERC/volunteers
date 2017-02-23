package com.volunteer.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Алексей on 17.02.2017.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/result").setViewName("result");
        registry.addViewController("/").setViewName("login");
        registry.addViewController("/index").setViewName("login");
        registry.addViewController("/admin").setViewName("admin");
        registry.addViewController("/success").setViewName("index");
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource driverManagerConnectionSource = new DriverManagerDataSource();
        driverManagerConnectionSource.setDriverClassName("com.mysql.jdbc.Driver");
        driverManagerConnectionSource.setUrl("jdbc:mysql://localhost:3306/volonteer");
        driverManagerConnectionSource.setUsername("root");
        driverManagerConnectionSource.setPassword("root");
        return driverManagerConnectionSource;
    }

}
