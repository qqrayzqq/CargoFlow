package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.dto.user.UpdateUserDto;
import com.github.qqrayzqq.cargoflow.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import com.github.qqrayzqq.cargoflow.security.JwtService;
import com.github.qqrayzqq.cargoflow.security.UserDetailsPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public String updateUser(UserDetails userDetails, UpdateUserDto dto){
        User user = getCurrentUser(userDetails);
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
        userRepository.update(user);

        log.info("User {} updated profile", user.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(user));
    }

    public Boolean deactivateUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User not found");
        }
        User user = optionalUser.get();
        userRepository.deactivate(user.getId());
        log.info("User {} username was deactivated", user.getUsername());
        return true;
    }

}
