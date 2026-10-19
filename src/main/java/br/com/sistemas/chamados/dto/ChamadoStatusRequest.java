package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record ChamadoStatusRequest(
        @NotNull(message = "O status do chamado é obrigatório")
        StatusChamado status
) {}
