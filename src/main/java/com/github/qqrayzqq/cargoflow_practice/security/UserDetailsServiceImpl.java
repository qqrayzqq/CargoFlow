package com.github.qqrayzqq.cargoflow_practice.security;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import com.github.qqrayzqq.cargoflow_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service             // регистрируем как Spring бин
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; // репозиторий для поиска пользователя в БД

    // Spring Security вызывает этот метод когда нужно найти пользователя по username.
    // Используется в JwtFilter (через userDetailsService.loadUserByUsername) и при логине.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем пользователя в БД. Если не найден — бросаем исключение.
        // Spring Security поймает его и автоматически вернёт 401.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Оборачиваем доменный User в UserDetailsPrincipal — адаптер между нашей моделью и Spring Security
        return new UserDetailsPrincipal(user);
    }
}
