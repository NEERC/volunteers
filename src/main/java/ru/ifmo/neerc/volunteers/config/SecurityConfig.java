package ru.ifmo.neerc.volunteers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ifmo.neerc.volunteers.service.SecuritySuccessHandler;
import ru.ifmo.neerc.volunteers.service.UserDetailsServiceImpl;

/**
 * Created by Алексей on 20.02.2017.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String PASSWORD_RESET_AUTHORITY = "CHANGE_PASSWORD";

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    SecuritySuccessHandler successHandler;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
                .antMatchers("/components/**", "/css/**", "/fonts/**", "/images/**", "/js/**", "/signup", "/login", "/reset-password/**", "/changePassword/**", "/favicon.ico", "/confirm/**", "/api/**").permitAll()
                .antMatchers("/admin/day/**").hasAnyRole("USER", "ADMIN")
                .antMatchers("/admin/**", "/users/**").hasRole("ADMIN")
                .antMatchers("/updatePassword/**").hasAuthority(PASSWORD_RESET_AUTHORITY)
                .anyRequest().hasAnyRole("USER", "ADMIN")
        .and()
            .formLogin()
                .failureUrl("/login?error")
                .successForwardUrl("/")
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password").permitAll()
        .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login").permitAll()
        .and()
            .csrf()
        .and()
            .exceptionHandling()
                .accessDeniedPage("/403")
                .and()
                .rememberMe()
                .key("bf40d4b2-2968-406a-b553-d8fff3b515cc")
                .rememberMeParameter("remember-me")
                .alwaysRemember(true)
                //.useSecureCookie(true)
                .rememberMeCookieName("my-remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60);
        // @formatter:on
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
