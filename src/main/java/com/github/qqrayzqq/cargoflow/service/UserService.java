package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.dto.user.UpdateUserDto;
import com.github.qqrayzqq.cargoflow.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidCredentialsException();
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Boolean updateUser(UpdateUserDto dto){
        User user = getCurrentUser();
        if(dto.getFullName() != null){
            user.setFullName(dto.getFullName());
        }
        if(dto.getUsername() != null){
            if(userRepository.findByUsername(dto.getUsername()).isPresent()){
                throw new AlreadyExistsException("This username is already occupied");
            }
            user.setUsername(dto.getUsername());
        }
        if(dto.getPassword() != null){
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        if(dto.getEmail() != null){
            if(userRepository.findByEmail(dto.getEmail()).isPresent()){
                throw new AlreadyExistsException("This email is already occupied");
            }
            user.setEmail(dto.getEmail());
        }
        userRepository.save(user);

        log.info("User {} updated profile", user.getUsername());
        return true;
    }

    public Boolean deactivateUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException("User not found");
        }
        userRepository.deactivate(id);
        log.info("User {} username was deactivated", userRepository.findById(id).get().getUsername());
        return true;
    }

}
