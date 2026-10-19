package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(
        @NotBlank(message = "O nome do técnico é obrigatório")
        @Size(max = 100, message = "O nome do técnico deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email do técnico é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotNull(message = "O perfil do técnico é obrigatório")
        Perfil perfil
) {}
