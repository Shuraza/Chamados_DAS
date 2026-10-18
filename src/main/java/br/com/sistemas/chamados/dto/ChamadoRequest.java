package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "O título do chamado é obrigatório")
        @Size(max = 100, message = "O título do chamado deve ter no máximo 100 caracteres")
        String titulo,

        @NotBlank(message = "A descrição do chamado é obrigatória")
        @Size(max = 1000, message = "A descrição do chamado deve ter no máximo 1000 caracteres")
        String descricao,

        @NotNull(message = "A prioridade do chamado é obrigatória")
        Prioridade prioridade,

        @NotNull(message = "O cliente do chamado é obrigatório")
        Long clienteId,

        @NotNull(message = "A categoria do chamado é obrigatória")
        Long categoriaId,

        Long tecnicoId
) {}
