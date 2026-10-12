package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {}
