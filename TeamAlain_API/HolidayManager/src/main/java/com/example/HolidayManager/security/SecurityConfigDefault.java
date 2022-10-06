package com.example.HolidayManager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * the security configuration for local
 *
 * @author cvisan
 */

@Configuration
@Order(1000)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfigDefault extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()// for
                .antMatchers("/auth/**","/notification/**").permitAll()//
                .and()
                .authorizeRequests()
                .antMatchers("/**")//
                .authenticated()//
                .and()
                .exceptionHandling()
                .and()//
                .csrf().disable()
                .addFilterBefore(jwtFilter(), BasicAuthenticationFilter.class);

    }

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
