package com.github.qqrayzqq.cargoflow_practice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity        // включает Spring Security в приложении
@EnableMethodSecurity     // включает @PreAuthorize/@PostAuthorize на методах контроллеров
public class SecurityConfig {

    // Объект для хэширования паролей через алгоритм BCrypt.
    // Spring Security использует его автоматически при проверке пароля на логине.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Менеджер аутентификации — используется в AuthService когда пользователь логинится.
    // Мы не создаём его вручную, Spring сам собирает его из контекста через AuthenticationConfiguration.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Иерархия ролей: ADMIN → MANAGER → SHIPPER.
    // Без этого бина Spring не знает что ADMIN "выше" MANAGER.
    // После объявления: hasRole('MANAGER') пустит и ADMIN тоже.
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("MANAGER")    // ADMIN включает в себя права MANAGER
                .role("MANAGER").implies("SHIPPER")  // MANAGER включает в себя права SHIPPER
                .build();
    }

    // Главный бин — описывает все правила безопасности для HTTP запросов.
    // JwtFilter инжектим через параметр метода, а не через поле — чтобы избежать circular dependency.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        // Отключаем CSRF защиту — она нужна только для сессий и куки.
        // Мы используем JWT в заголовках, поэтому CSRF нам не нужен.
        http.csrf(AbstractHttpConfigurer::disable);

        // Правила доступа к эндпоинтам.
        // Порядок важен: requestMatchers проверяются сверху вниз, первое совпадение побеждает.
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()       // регистрация и логин — всем
                .requestMatchers("/api/shipments/track/**").permitAll() // отслеживание посылки — всем
                .requestMatchers("/graphql").permitAll()
                .requestMatchers("/graphiql/**").permitAll()// GraphQL UI для тестирования — всем (только dev)
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());                    // всё остальное — только с токеном

        // Отключаем HTTP сессии — JWT сам несёт всю информацию о пользователе.
        // STATELESS = не создавать и не использовать сессии вообще.
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Встраиваем наш JwtFilter в цепочку фильтров перед стандартным фильтром логина.
        // Порядок важен: сначала проверяем JWT токен, потом остальные фильтры.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
