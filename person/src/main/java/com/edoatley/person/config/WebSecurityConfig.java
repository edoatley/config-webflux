package com.edoatley.person.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

    public static final String ADMIN = "ADMIN";
    public static final String BASIC = "BASIC";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/people").hasRole(ADMIN)
                .pathMatchers(HttpMethod.GET, "/people", "/people/**").hasAnyRole(BASIC, ADMIN)
                .pathMatchers(HttpMethod.GET, "/ping", "/actuator/**").permitAll()
                .and().httpBasic()
                .and().build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        final User.UserBuilder userBuilder = User.builder().passwordEncoder(encoder::encode);

        UserDetails ed = userBuilder
                .username("ed")
                .password("password")
                .roles(ADMIN)
                .build();
        UserDetails bob = userBuilder
                .username("bob")
                .password("password")
                .roles(BASIC)
                .build();
        return new MapReactiveUserDetailsService(ed, bob);
    }
}
