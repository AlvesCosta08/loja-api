package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;

public record UserResponse(
        Long id,
        String email,
        String name,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
