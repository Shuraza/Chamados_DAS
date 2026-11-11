package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 100, message = "O nome da categoria deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição da categoria deve ter no máximo 255 caracteres")
    private String descricao;

    public static CategoriaForm de(CategoriaResponse categoria) {
        CategoriaForm form = new CategoriaForm();
        form.setNome(categoria.nome());
        form.setDescricao(categoria.descricao());
        return form;
    }

    public CategoriaRequest paraRequest() {
        return new CategoriaRequest(nome, descricao);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
