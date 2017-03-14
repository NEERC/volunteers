package org.thymeleaf.spring.support;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thymeleaf.spring.support.Layout;
import org.thymeleaf.spring.support.ThymeleafLayoutInterceptor;

/**
 * Copied from here: https://github.com/kolorobot/thymeleaf-custom-layout/blob/master/src/test/java/it/CustomLayoutTestApp.java
 * Created by Matvey on 3/14/17.
 */
@SpringBootApplication
public class CustomLayoutTestApp {
    @Configuration
    public static class ThymeleafLayoutInterceptorConfig extends WebMvcConfigurationSupport {
        @Override
        protected void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new ThymeleafLayoutInterceptor().layoutPrefix("layouts/").defaultLayout("default"));
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/vc").setViewName("viewController");
        }
    }

    @Controller
    @Layout(value = "default")
    public static class LayoutController {

        @RequestMapping({"/", "index"})
        public String defaultLayout() {
            return "index";
        }

        @RequestMapping("simple")
        @Layout("simple")
        public String simpleLayout() {
            return "simple";
        }

        @RequestMapping("no-layout")
        @Layout(Layout.NONE)
        public String noLayout() {
            return "noLayout";
        }
    }
}