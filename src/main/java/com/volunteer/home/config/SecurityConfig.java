package com.volunteer.home.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.volunteer.home.service.UserDetailsServiceImpl;

/**
 * Created by Алексей on 20.02.2017.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
                .antMatchers("/admin", "/users").hasAnyRole("ADMIN")
                .antMatchers("/result","/").hasAnyRole("USER","ADMIN")
                .antMatchers( "/login", "/signup").permitAll()
        .and()
            .formLogin()
                .failureUrl("/login?error")
                .defaultSuccessUrl("/result")
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password").permitAll()
        .and()
            .logout()
                .logoutSuccessUrl("/login?logout").permitAll()
        .and()
            .csrf()
        .and()
            .exceptionHandling()
                .accessDeniedPage("/403");
        // @formatter:on
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }
}
