package com.volunteer.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * Created by Алексей on 17.02.2017.
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class MvcConfig extends WebMvcConfigurerAdapter {

    private static final String PROP_DATABASE_DRIVER ="spring.datasource.driver-class-name";
    private static final String PROP_DATABASE_URL ="spring.datasource.url";
    private static final String PROP_DATABASE_USERNAME ="spring.datasource.username";
    private static final String PROP_DATABASE_PASSWORD ="spring.datasource.password";

    @Resource
    private Environment env;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/result").setViewName("result");
        registry.addViewController("/").setViewName("result");
        registry.addViewController("/index").setViewName("login");
        registry.addViewController("/admin").setViewName("admin");
        registry.addViewController("/success").setViewName("index");
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {

        DriverManagerDataSource driverManagerConnectionSource = new DriverManagerDataSource();
        driverManagerConnectionSource.setDriverClassName(env.getRequiredProperty(PROP_DATABASE_DRIVER));
        driverManagerConnectionSource.setUrl(env.getRequiredProperty(PROP_DATABASE_URL));
        driverManagerConnectionSource.setUsername(env.getRequiredProperty(PROP_DATABASE_USERNAME));
        driverManagerConnectionSource.setPassword(env.getRequiredProperty(PROP_DATABASE_PASSWORD));
        return driverManagerConnectionSource;
    }

}
