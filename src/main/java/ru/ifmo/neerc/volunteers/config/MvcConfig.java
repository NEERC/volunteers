package ru.ifmo.neerc.volunteers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

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
        registry.addViewController("/index").setViewName("login");
        registry.addViewController("/success").setViewName("index");
    }

}
