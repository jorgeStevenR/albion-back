package com.albion.guildbalance.web.security;



import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration

@EnableWebSecurity

@EnableMethodSecurity

@RequiredArgsConstructor

public class SecurityConfig {



    private static final String[] MEMBER_ROLES = {"ADMIN", "CALLER", "OFFICER", "PLAYER"};

    private static final String[] LEADER_ROLES = {"ADMIN", "CALLER", "OFFICER"};



    private final JwtAuthenticationFilter jwtAuthenticationFilter;



    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/health").permitAll()

                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/balance/player/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers("/api/players/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/avalons/*/penalties/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/avalons/*/penalties/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.DELETE, "/api/avalons/*/penalties/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers("/api/penalties/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/avalons/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/avalons/*/roles/slots/*/join").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.DELETE, "/api/avalons/*/roles/slots/*/leave").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/avalons/*/roles/*/join").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.DELETE, "/api/avalons/*/roles/*/leave").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers("/api/avalons/**").hasAnyRole(LEADER_ROLES)

                        .requestMatchers("/api/sales/**").hasAnyRole(LEADER_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/guild/stats", "/api/guild/transactions").hasAnyRole("ADMIN", "OFFICER")

                        .requestMatchers(HttpMethod.GET, "/api/penalties/all", "/api/penalties/appeals/all").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/penalties/admin/manual").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/money-requests/withdrawals", "/api/money-requests/loans").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/money-requests/*/review").hasRole("ADMIN")

                        .requestMatchers("/api/money-requests/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/ping-templates/all").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/ping-templates").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/ping-templates/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/ping-templates/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/ping-templates/*/create-avalon").hasAnyRole(LEADER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/guild/sync").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/guild/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/items/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/items/sync").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/build-templates/**").hasAnyRole(MEMBER_ROLES)

                        .requestMatchers(HttpMethod.PUT, "/api/build-templates/**").hasAnyRole(LEADER_ROLES)

                        .anyRequest().authenticated()

                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();

    }



    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}


