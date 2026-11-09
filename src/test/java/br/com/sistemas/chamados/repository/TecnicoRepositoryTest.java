package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Tecnico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TecnicoRepositoryTest {

    @Autowired
    private TecnicoRepository repository;

    @Test
    void salvarGeraId() {
        Tecnico salvo = repository.save(new Tecnico("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repository.findById(salvo.getId())).get()
                .extracting(Tecnico::getNome, Tecnico::getPerfil)
                .containsExactly("Carlos Souza", Perfil.TECNICO);
    }

    @Test
    void salvarSemPerfilFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Tecnico("Carlos Souza", "carlos@empresa.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarEmailDuplicadoFalha() {
        repository.saveAndFlush(new Tecnico("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));

        assertThatThrownBy(() -> repository.saveAndFlush(new Tecnico("Outro Carlos", "carlos@empresa.com", Perfil.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByEmailAndIdNotIgnoraOProprioTecnico() {
        Tecnico carlos = repository.save(new Tecnico("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));
        Tecnico julia = repository.save(new Tecnico("Julia Alves", "julia@empresa.com", Perfil.ADMIN));

        assertThat(repository.existsByEmail("carlos@empresa.com")).isTrue();
        assertThat(repository.existsByEmailAndIdNot("carlos@empresa.com", carlos.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("carlos@empresa.com", julia.getId())).isTrue();
    }
}
