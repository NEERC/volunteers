package ru.ifmo.neerc.volunteers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
                .antMatchers("/components/**", "/css/**", "/fonts/**", "/js/**", "/signup", "/login", "/reset-password/**", "/changePassword/**").permitAll()
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
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
