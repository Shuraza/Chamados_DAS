package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TecnicoForm {

    @NotBlank(message = "O nome do técnico é obrigatório")
    @Size(max = 100, message = "O nome do técnico deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O email do técnico é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotNull(message = "O perfil do técnico é obrigatório")
    private Perfil perfil;

    public static TecnicoForm de(TecnicoResponse tecnico) {
        TecnicoForm form = new TecnicoForm();
        form.setNome(tecnico.nome());
        form.setEmail(tecnico.email());
        form.setPerfil(tecnico.perfil());
        return form;
    }

    public TecnicoRequest paraRequest() {
        return new TecnicoRequest(nome, email, perfil);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
