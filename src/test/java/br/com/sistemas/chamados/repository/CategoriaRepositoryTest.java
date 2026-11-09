package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository repository;

    @Test
    void salvarGeraId() {
        Categoria salva = repository.save(new Categoria("Hardware", "Problemas de equipamento"));

        assertThat(salva.getId()).isNotNull();
        assertThat(repository.findById(salva.getId())).get()
                .extracting(Categoria::getNome, Categoria::getDescricao)
                .containsExactly("Hardware", "Problemas de equipamento");
    }

    @Test
    void salvarSemNomeFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Categoria(null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarNomeDuplicadoFalha() {
        repository.saveAndFlush(new Categoria("Hardware", null));

        assertThatThrownBy(() -> repository.saveAndFlush(new Categoria("Hardware", "Outra")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByNome() {
        repository.save(new Categoria("Hardware", null));

        assertThat(repository.existsByNome("Hardware")).isTrue();
        assertThat(repository.existsByNome("Software")).isFalse();
    }

    @Test
    void existsByNomeAndIdNotIgnoraAPropriaCategoria() {
        Categoria hardware = repository.save(new Categoria("Hardware", null));
        Categoria software = repository.save(new Categoria("Software", null));

        assertThat(repository.existsByNomeAndIdNot("Hardware", hardware.getId())).isFalse();
        assertThat(repository.existsByNomeAndIdNot("Hardware", software.getId())).isTrue();
    }
}
