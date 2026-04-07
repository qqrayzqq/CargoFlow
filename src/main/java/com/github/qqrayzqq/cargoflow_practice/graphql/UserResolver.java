package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import com.github.qqrayzqq.cargoflow_practice.dto.user.UpdateUserDto;
import com.github.qqrayzqq.cargoflow_practice.service.UserService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public User getUserById(@Argument Long id){
        return userService.getUserById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User getCurrentUser(){
        return userService.getCurrentUser();
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean updateUser(@Argument @Valid UpdateUserDto input){
        return userService.updateUser(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deactivateUser(@Argument Long id){
        return userService.deactivateUser(id);
    }
}
