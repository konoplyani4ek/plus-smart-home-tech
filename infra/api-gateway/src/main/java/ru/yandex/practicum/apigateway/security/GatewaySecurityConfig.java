package ru.yandex.practicum.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityUserProperties.class)
public class GatewaySecurityConfig {

    private static final String[] PUBLIC_DOC_ROUTES = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/*/v3/api-docs/**"
    };

    private static final String[] ADMIN_WRITE_ROUTES = {
            "/api/products/**",
            "/api/categories/**",
            "/api/inventory/**"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(PUBLIC_DOC_ROUTES).permitAll()

                        .pathMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/inventory/**").permitAll()

                        .pathMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/**").hasRole("USER")

                        .pathMatchers(HttpMethod.POST, ADMIN_WRITE_ROUTES).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, ADMIN_WRITE_ROUTES).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, ADMIN_WRITE_ROUTES).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, ADMIN_WRITE_ROUTES).hasRole("ADMIN")

                        .anyExchange().denyAll()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(SecurityUserProperties properties,
                                                            PasswordEncoder passwordEncoder) {
        List<UserDetails> users = properties.getUsers().stream()
                .map(account -> User.withUsername(account.getUsername())
                        .password(passwordEncoder.encode(account.getPassword()))
                        .roles(account.getRoles().toArray(new String[0]))
                        .build())
                .collect(Collectors.toList());

        return new MapReactiveUserDetailsService(users);
    }
}