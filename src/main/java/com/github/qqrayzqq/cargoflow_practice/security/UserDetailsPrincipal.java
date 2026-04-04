package com.github.qqrayzqq.cargoflow_practice.security;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Адаптер между нашим доменным User и интерфейсом UserDetails которого ожидает Spring Security.
// Spring не знает про наш User — он работает только через UserDetails.
@RequiredArgsConstructor
public class UserDetailsPrincipal implements UserDetails {

    private final User user; // оригинальный доменный объект

    // Метод для получения оригинального User — может понадобиться в сервисах
    public User getUser() {
        return user;
    }

    // Возвращает список ролей пользователя в формате Spring Security.
    // Добавляем префикс "ROLE_" — Spring требует его для проверок hasRole().
    // Итог: ADMIN → "ROLE_ADMIN", MANAGER → "ROLE_MANAGER" и т.д.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // Возвращает хэш пароля — Spring сравнивает его с введённым паролем через PasswordEncoder
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    // Возвращает username — по нему Spring идентифицирует пользователя
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Аккаунт не истёк — у нас нет такой логики, всегда true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Аккаунт не заблокирован — у нас нет такой логики, всегда true
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Пароль не истёк — у нас нет такой логики, всегда true
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Пользователь активен — берём поле isActive из нашего User.
    // Если false (деактивирован) — Spring заблокирует вход автоматически.
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
