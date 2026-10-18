package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "O texto do comentário é obrigatório")
        @Size(max = 1000, message = "O texto do comentário deve ter no máximo 1000 caracteres")
        String texto,

        @NotBlank(message = "O autor do comentário é obrigatório")
        @Size(max = 100, message = "O autor do comentário deve ter no máximo 100 caracteres")
        String autor
) {}
