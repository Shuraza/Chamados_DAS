package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

// Usado só no GET /chamados/{id}, a listagem não carrega os comentários
public record ChamadoDetalheResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        ClienteResponse cliente,
        CategoriaResponse categoria,
        TecnicoResponse tecnico,
        List<ComentarioResponse> comentarios
) {
    public static ChamadoDetalheResponse de(Chamado c) {
        return new ChamadoDetalheResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getPrioridade(),
                c.getStatus(),
                ClienteResponse.de(c.getCliente()),
                CategoriaResponse.de(c.getCategoria()),
                c.getTecnico() != null ? TecnicoResponse.de(c.getTecnico()) : null,
                c.getComentarios().stream().map(ComentarioResponse::de).toList()
        );
    }
}
