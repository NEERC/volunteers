package ru.ifmo.neerc.volunteers.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring.support.ThymeleafLayoutInterceptor;

/**
 * Created by Алексей on 17.02.2017.
 */
@Configuration
@EnableTransactionManagement
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Resource
    private Environment env;

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/result").setViewName("result");
    }


    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new ThymeleafLayoutInterceptor().layoutPrefix("layouts/").viewAttributeName("view").defaultLayout("public"));
    }
}
