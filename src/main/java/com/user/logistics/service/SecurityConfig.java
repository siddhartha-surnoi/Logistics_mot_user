//package com.example.Logistics.service;
//
//
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing purposes
////                .authorizeHttpRequests(auth -> auth
////                        .anyRequest().permitAll() // Allow all requests without authentication
////                )
////                .headers(headers -> headers
////                        .frameOptions(frameOptions -> frameOptions.disable()) // Disable frame options to allow H2 console
////                );
////
////        return http.build();
////    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/user/register", "/user/login", "/user/drivers", "/user/forgot-password","/profileSettings/user/{userId}","/bank/user/{userId}","/api/vehicles/Vehicle/{transporterId}","/documents/upload/{userId}").permitAll()  // Allow register and login
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults());  // Use HTTP Basic Authentication (username and password in request headers)
//
//        return http.build();
//    }
//
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
package com.user.logistics.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // Permit all requests
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
