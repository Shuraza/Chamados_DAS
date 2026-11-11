package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void salvarGeraId() {
        Cliente salvo = repository.save(new Cliente("Maria Silva", "maria@email.com", "47999998888"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repository.findById(salvo.getId())).get()
                .extracting(Cliente::getNome, Cliente::getEmail, Cliente::getTelefone)
                .containsExactly("Maria Silva", "maria@email.com", "47999998888");
    }

    @Test
    void salvarSemNomeFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Cliente(null, "maria@email.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarEmailDuplicadoFalha() {
        repository.saveAndFlush(new Cliente("Maria Silva", "maria@email.com", null));

        assertThatThrownBy(() -> repository.saveAndFlush(new Cliente("Outra Maria", "maria@email.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByEmail() {
        repository.save(new Cliente("Maria Silva", "maria@email.com", null));

        assertThat(repository.existsByEmail("maria@email.com")).isTrue();
        assertThat(repository.existsByEmail("pedro@email.com")).isFalse();
    }

    @Test
    void existsByEmailAndIdNotIgnoraOProprioCliente() {
        Cliente maria = repository.save(new Cliente("Maria Silva", "maria@email.com", null));
        Cliente pedro = repository.save(new Cliente("Pedro Costa", "pedro@email.com", null));

        assertThat(repository.existsByEmailAndIdNot("maria@email.com", maria.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("maria@email.com", pedro.getId())).isTrue();
    }

    @Test
    void buscarInexistenteRetornaVazio() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}
