package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteForm {

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(max = 100, message = "O nome do cliente deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O email do cliente é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @Size(max = 20, message = "O telefone do cliente deve ter no máximo 20 caracteres")
    private String telefone;

    public static ClienteForm de(ClienteResponse cliente) {
        ClienteForm form = new ClienteForm();
        form.setNome(cliente.nome());
        form.setEmail(cliente.email());
        form.setTelefone(cliente.telefone());
        return form;
    }

    public ClienteRequest paraRequest() {
        return new ClienteRequest(nome, email, telefone);
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
