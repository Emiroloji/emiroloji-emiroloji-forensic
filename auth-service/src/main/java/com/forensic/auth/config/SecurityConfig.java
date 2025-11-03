package com.forensic.auth.config;

import com.forensic.auth.security.JwtAuthenticationEntryPoint;
import com.forensic.auth.security.JwtAuthenticationFilter;
import com.forensic.auth.security.JwtTokenProvider;
import com.forensic.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the authentication service
 * 
 * This configuration provides:
 * - JWT-based authentication
 * - Role-based access control (RBAC)
 * - CORS configuration
 * - Password encoding
 * - Session management
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private UserDetailsServiceImpl userDetailsService;

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12); // High strength for forensic system
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                authProvider.setHideUserNotFoundExceptions(true); // Don't hide user not found for security
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
                return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http

                                 .cors(Customizer.withDefaults()) // <--- BU SATIRI EKLE
                                // Disable CSRF for stateless JWT authentication
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Configure exception handling
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                                // Configure session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/api/auth/signin", "/api/auth/signup",
                                                                "/api/auth/refresh")
                                                .permitAll()
                                                .requestMatchers("/api/auth/forgot-password",
                                                                "/api/auth/reset-password")
                                                .permitAll()
                                                .requestMatchers("/actuator/health", "/actuator/info",
                                                                "/actuator/prometheus")
                                                .permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                                                "/swagger-ui.html")
                                                .permitAll()

                                                // Admin only endpoints
                                                .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")

                                                // User management endpoints
                                                .requestMatchers("/api/auth/users/**")
                                                .hasAnyRole("ADMIN", "INVESTIGATOR")

                                                // Profile endpoints
                                                .requestMatchers("/api/auth/profile/**").authenticated()

                                                // 2FA endpoints
                                                .requestMatchers("/api/auth/2fa/**").authenticated()

                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Add JWT filter
                                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                                // Configure authentication provider
                                .authenticationProvider(authenticationProvider());

                return http.build();
        }

    // SecurityConfig sınıfının içine, filterChain'in altına ekle

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend'in adresini buraya yazıyoruz. "*" (herkese izin ver) KULLANMA.
        configuration.setAllowedOrigins(List.of("http://localhost:3001"));

        // İzin verilen HTTP metotları
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // İzin verilen HTTP başlıkları (Authorization: Bearer token için şart)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Tarayıcının token (Credentials) göndermesine izin ver
        configuration.setAllowCredentials(true);

        // Tarayıcının bu ayarları ne kadar süre cache'lemesi gerektiği
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Tüm /api/ altındaki yollara bu CORS ayarlarını uygula
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }


}
