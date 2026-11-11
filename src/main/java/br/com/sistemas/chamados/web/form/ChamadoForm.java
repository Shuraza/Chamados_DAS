package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChamadoForm {

    @NotBlank(message = "O título do chamado é obrigatório")
    @Size(max = 100, message = "O título do chamado deve ter no máximo 100 caracteres")
    private String titulo;

    @NotBlank(message = "A descrição do chamado é obrigatória")
    @Size(max = 1000, message = "A descrição do chamado deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "A prioridade do chamado é obrigatória")
    private Prioridade prioridade;

    @NotNull(message = "O cliente do chamado é obrigatório")
    private Long clienteId;

    @NotNull(message = "A categoria do chamado é obrigatória")
    private Long categoriaId;

    private Long tecnicoId;

    public ChamadoRequest paraRequest() {
        return new ChamadoRequest(titulo, descricao, prioridade, clienteId, categoriaId, tecnicoId);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }
}
