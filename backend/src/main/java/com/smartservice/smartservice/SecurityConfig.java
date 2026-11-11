
package com.smartservice.smartservice;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()))

                    // Enable CSRF protection using cookie-based tokens.
            .csrf(csrf -> csrf
    .csrfTokenRepository(
        CookieCsrfTokenRepository.withHttpOnlyFalse()
    )
    .csrfTokenRequestHandler(
        new CsrfTokenRequestAttributeHandler()
    )
)

            .securityContext(context -> context
                    .securityContextRepository(securityContextRepository))

            .userDetailsService(userDetailsService)

            .authorizeHttpRequests(auth -> auth

                // Allow browser CORS preflight requests
                .requestMatchers(
                        HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Public registration and login
                // Public registration and login
.requestMatchers(
        HttpMethod.POST,
        "/api/users",
        "/api/users/login"
).permitAll()

// Public CSRF token endpoint
.requestMatchers(
        HttpMethod.GET,
        "/api/csrf"
).permitAll()

// Public provider viewing
.requestMatchers(
        HttpMethod.GET,
        "/api/providers",
        "/api/providers/**"
).permitAll()

// Public service viewing
.requestMatchers(
        HttpMethod.GET,
        "/api/services",
        "/api/services/**"
).permitAll()

                // Only authenticated users can view users
                .requestMatchers(
        HttpMethod.GET,
        "/api/users"
).hasRole("ADMIN")

                // Only admins can create providers
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/providers"
                ).hasRole("ADMIN")

                // Only admins can delete providers
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/providers/**"
                ).hasRole("ADMIN")

                // Only admins can create services
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/services"
                ).hasRole("ADMIN")

                // All booking endpoints require authentication
               // All authentication-status endpoints require authentication
// All booking endpoints require authentication
.requestMatchers("/api/bookings/**").authenticated()

// All authentication-status endpoints require authentication
.requestMatchers("/api/auth/**").authenticated()

.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                // Preserve existing behavior for other endpoints
                .anyRequest().denyAll()
            )

            // Return 401 instead of 403 for unauthenticated requests
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(
                            new HttpStatusEntryPoint(
                                    HttpStatus.UNAUTHORIZED
                            )
                    )
            )

            // Disable default form login and HTTP Basic authentication
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.IF_REQUIRED
                    )
            )

            // Logout and invalidate the session
            .logout(logout -> logout
                    .logoutUrl("/api/users/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(
                            (request, response, authentication) -> {
                                response.setStatus(
                                        HttpServletResponse.SC_OK
                                );
                            }
                    )
            );

        return http.build();
    }
}
