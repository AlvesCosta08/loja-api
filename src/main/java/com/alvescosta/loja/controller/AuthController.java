package com.alvescosta.loja.controller;

import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.LoginRequest;
import com.alvescosta.loja.dto.TokenResponse;
import com.alvescosta.loja.dto.UserCreateRequest;
import com.alvescosta.loja.dto.UserResponse;
import com.alvescosta.loja.security.JwtService;
import com.alvescosta.loja.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserCreateRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request);
        String token = jwtService.generate(user);
        return TokenResponse.bearer(token, jwtService.getExpirationMs());
    }
}
