package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.LoginRequest;
import com.alvescosta.loja.dto.UserCreateRequest;
import com.alvescosta.loja.dto.UserResponse;
import com.alvescosta.loja.exception.DuplicateEmailException;
import com.alvescosta.loja.exception.UnauthorizedException;
import com.alvescosta.loja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // CORREÇÃO AQUI: Adicionada a string "E-mail já cadastrado: " antes do e-mail
            throw new DuplicateEmailException("E-mail já cadastrado: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .hashedPassword(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UnauthorizedException("Credenciais invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new UnauthorizedException("Credenciais invalidas");
        }

        return user;
    }
}