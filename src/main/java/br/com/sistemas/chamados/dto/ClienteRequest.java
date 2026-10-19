package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "O nome do cliente é obrigatório")
        @Size(max = 100, message = "O nome do cliente deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email do cliente é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Size(max = 20, message = "O telefone do cliente deve ter no máximo 20 caracteres")
        String telefone
) {}
