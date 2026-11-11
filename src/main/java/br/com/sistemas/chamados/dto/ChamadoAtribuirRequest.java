package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotNull;

public record ChamadoAtribuirRequest(
        @NotNull(message = "O técnico do chamado é obrigatório")
        Long tecnicoId
) {}
