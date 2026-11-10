package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComentarioForm {

    @NotBlank(message = "O texto do comentário é obrigatório")
    @Size(max = 1000, message = "O texto do comentário deve ter no máximo 1000 caracteres")
    private String texto;

    @NotBlank(message = "O autor do comentário é obrigatório")
    @Size(max = 100, message = "O autor do comentário deve ter no máximo 100 caracteres")
    private String autor;

    public ComentarioRequest paraRequest() {
        return new ComentarioRequest(texto, autor);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}
