package com.github.qqrayzqq.cargoflow_practice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // Секретный ключ в формате Base64 — читается из application.yaml
    @Value("${jwt.secret}")
    private String secret;

    // Время жизни токена в миллисекундах (3600000 = 1 час) — читается из application.yaml
    @Value("${jwt.expiration}")
    private long expiration;

    // ── PUBLIC API ──────────────────────────────────────────────────────────

    // Генерирует JWT токен для пользователя.
    // Кладёт в payload: username (sub), роли (roles), время выдачи (iat), время истечения (exp).
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(); // карта для дополнительных полей в payload

        // Достаём строковые названия ролей из объектов GrantedAuthority: ["ROLE_ADMIN"]
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", roles); // добавляем роли в payload под ключом "roles"

        return Jwts.builder()
                .claims(claims)                              // кладём наши дополнительные поля
                .subject(userDetails.getUsername())          // поле "sub" — username пользователя
                .issuedAt(new Date())                        // поле "iat" — время выдачи (сейчас)
                .expiration(new Date(System.currentTimeMillis() + expiration)) // поле "exp" — время истечения
                .signWith(getSigningKey())                   // подписываем токен нашим ключом
                .compact();                                  // собираем в строку "xxxxx.yyyyy.zzzzz"
    }

    // Достаёт username из токена (поле "sub" в payload)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Достаёт список ролей из токена и конвертирует в объекты GrantedAuthority для Spring Security
    @SuppressWarnings("unchecked") // Java не может проверить тип внутри List во время компиляции — это нормально
    public List<GrantedAuthority> extractRoles(String token) {
        Claims claims = extractAllClaims(token); // достаём весь payload
        List<String> roles = claims.get("roles", List.class); // берём поле "roles" как список строк

        if (roles == null) return List.of(); // если ролей нет — возвращаем пустой список

        return roles.stream()
                .map(SimpleGrantedAuthority::new) // каждую строку оборачиваем в объект Spring Security
                .collect(Collectors.toList());
    }

    // Проверяет что токен валиден: принадлежит этому пользователю И ещё не истёк
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token); // достаём username из токена
        return username.equals(userDetails.getUsername()) // username совпадает?
                && !isTokenExpired(token);                // токен не истёк?
    }

    // ── PRIVATE HELPERS ─────────────────────────────────────────────────────

    // Проверяет истёк ли токен — сравнивает поле "exp" с текущим временем
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration) // достаём дату истечения
                .before(new Date());                       // она раньше чем сейчас?
    }

    // Универсальный метод для извлечения любого поля из payload.
    // claimsResolver — функция которая говорит что именно достать.
    // Например: Claims::getSubject вернёт String, Claims::getExpiration вернёт Date.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token); // парсим токен, получаем весь payload
        return claimsResolver.apply(claims);     // применяем функцию — достаём нужное поле
    }

    // Парсит токен и возвращает весь payload (Claims).
    // Автоматически проверяет подпись — если она неверна, бросает исключение.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // указываем ключ для проверки подписи
                .build()
                .parseSignedClaims(token)   // парсим и проверяем подпись
                .getPayload();              // возвращаем payload: { sub, roles, iat, exp }
    }

    // Декодирует Base64 секрет и создаёт криптографический ключ для подписи/проверки токенов
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // Base64 строка → массив байт
        return Keys.hmacShaKeyFor(keyBytes);              // из байт создаём объект SecretKey
    }
}
