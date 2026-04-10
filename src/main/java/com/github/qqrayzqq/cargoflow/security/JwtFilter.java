package com.github.qqrayzqq.cargoflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component            // регистрируем как Spring бин, чтобы можно было инжектить в SecurityConfig
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter гарантирует что фильтр выполнится ровно один раз на каждый запрос

    private final JwtService jwtService;               // для работы с JWT токеном
    private final UserDetailsService userDetailsService; // для загрузки пользователя из БД

    @Override
    protected void doFilterInternal(HttpServletRequest request,   // входящий HTTP запрос
                                    HttpServletResponse response,  // исходящий HTTP ответ
                                    FilterChain filterChain)       // цепочка следующих фильтров
            throws ServletException, IOException {                 // пробрасываем исключения из filterChain

        // Достаём заголовок Authorization из запроса.
        // Ожидаем формат: "Bearer eyJhbGci..."
        var header = request.getHeader("Authorization");

        // Если заголовка нет или он не начинается с "Bearer " — это не JWT запрос.
        // Передаём дальше без аутентификации (публичные эндпоинты пройдут, защищённые получат 401).
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // return обязателен — без него код продолжит выполняться вниз
        }

        // Убираем "Bearer " (7 символов) — остаётся только сам токен
        var token = header.substring(7);

        // Достаём username из payload токена (поле "sub")
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (io.jsonwebtoken.JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

                // Если аутентификация уже установлена в этом запросе — не трогаем, передаём дальше.
        // getAuthentication() возвращает null если никто ещё не аутентифицировался.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Загружаем пользователя из БД по username
        UserDetails user = userDetailsService.loadUserByUsername(username);

        // Проверяем что токен валиден: username совпадает и токен не истёк
        if (!jwtService.isTokenValid(token, user)) {
            filterChain.doFilter(request, response);
            return; // токен невалиден — передаём без аутентификации, Spring вернёт 401
        }

        // Создаём объект аутентификации для Spring Security.
        // Аргументы: кто это (UserDetails), пароль (null — уже проверен токеном), его роли.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        // Добавляем детали запроса (IP адрес, session ID) — Spring Security ожидает это
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Кладём аутентификацию в SecurityContext — теперь Spring знает кто этот пользователь.
        // После этого @PreAuthorize и проверки ролей начинают работать.
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Передаём запрос дальше по цепочке фильтров — теперь уже как аутентифицированный
        filterChain.doFilter(request, response);
    }
}
