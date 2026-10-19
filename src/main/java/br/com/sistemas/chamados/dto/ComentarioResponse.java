package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Comentario;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String texto, LocalDateTime data, String autor, Long chamadoId) {
    public static ComentarioResponse de(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getTexto(), c.getData(), c.getAutor(), c.getChamado().getId());
    }
}
