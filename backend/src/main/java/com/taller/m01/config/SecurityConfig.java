package com.taller.m01.config;

import com.taller.m01.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.*;
import com.taller.m01.repository.UserRepository;
import com.taller.m01.security.AuthenticatedUser;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
import java.time.Instant;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(UserRepository users) {
        return email -> users.findByEmailIgnoreCase(email).map(AuthenticatedUser::new).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(cors -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().requestMatchers("/api/auth/login", "/api/auth/forgot-password", "/api/auth/verify-code", "/api/auth/reset-password", "/api/public/register").permitAll().anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
                res.getWriter().write("{\"code\":\"UNAUTHENTICATED\",\"message\":\"No autenticado.\",\"timestamp\":\"" + Instant.now() + "\",\"path\":\"" + req.getRequestURI() + "\",\"validationErrors\":{}}");
            }))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(AppSecurityProperties props) {
        CorsConfiguration config = new CorsConfiguration(); config.setAllowedOrigins(List.of(props.getFrontendOrigin())); config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS")); config.setAllowedHeaders(List.of("Content-Type", "X-Requested-With")); config.setAllowCredentials(true); config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**", config); return source;
    }
}
