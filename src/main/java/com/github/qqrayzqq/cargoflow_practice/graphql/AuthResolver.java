package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.dto.user.LoginDto;
import com.github.qqrayzqq.cargoflow_practice.dto.user.RegisterDto;
import com.github.qqrayzqq.cargoflow_practice.service.AuthService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {
    private final AuthService authService;

    @MutationMapping
    public String login(@Argument @Valid LoginDto input){
        return authService.login(input);
    }

    @MutationMapping
    public String register(@Argument @Valid RegisterDto input){
        return authService.register(input);
    }
}
