package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        Long clienteId,
        Long categoriaId,
        Long tecnicoId
) {
    public static ChamadoResponse de(Chamado c) {
        return new ChamadoResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getPrioridade(),
                c.getStatus(),
                c.getCliente().getId(),
                c.getCategoria().getId(),
                c.getTecnico() != null ? c.getTecnico().getId() : null
        );
    }
}
