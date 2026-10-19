package com.alvescosta.loja.controller;

import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.UserResponse;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal String userId) {
        // O Spring injeta diretamente o "subject" do JWT (que é o ID como String)
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
        return UserResponse.from(user);
    }
}