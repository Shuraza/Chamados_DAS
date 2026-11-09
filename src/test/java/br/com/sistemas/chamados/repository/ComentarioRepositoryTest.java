package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.Prioridade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ComentarioRepositoryTest {

    @Autowired
    private ComentarioRepository repository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Chamado chamado;

    @BeforeEach
    void setUp() {
        Cliente cliente = clienteRepository.save(new Cliente("Maria Silva", "maria@email.com", null));
        Categoria categoria = categoriaRepository.save(new Categoria("Hardware", null));
        chamado = chamadoRepository.save(new Chamado("PC não liga", "Não liga desde ontem", Prioridade.ALTA, cliente, categoria, null));
    }

    @Test
    void salvarGeraIdEData() {
        Comentario salvo = repository.save(new Comentario("Verificando a fonte", "Carlos Souza", chamado));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getData()).isNotNull();
    }

    @Test
    void salvarSemChamadoFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Comentario("Verificando a fonte", "Carlos Souza", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarSemTextoFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Comentario(null, "Carlos Souza", chamado)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByChamadoIdEExistsByChamadoId() {
        repository.save(new Comentario("Verificando a fonte", "Carlos Souza", chamado));
        repository.save(new Comentario("Fonte trocada", "Carlos Souza", chamado));

        assertThat(repository.findByChamadoId(chamado.getId()))
                .extracting(Comentario::getTexto)
                .containsExactlyInAnyOrder("Verificando a fonte", "Fonte trocada");
        assertThat(repository.existsByChamadoId(chamado.getId())).isTrue();
        assertThat(repository.existsByChamadoId(999L)).isFalse();
    }

    @Test
    void chamadoCarregaSeusComentarios() {
        repository.save(new Comentario("Verificando a fonte", "Carlos Souza", chamado));
        repository.save(new Comentario("Fonte trocada", "Carlos Souza", chamado));
        entityManager.flush();
        entityManager.clear();

        Chamado carregado = chamadoRepository.findById(chamado.getId()).orElseThrow();

        assertThat(carregado.getComentarios())
                .extracting(Comentario::getTexto)
                .containsExactlyInAnyOrder("Verificando a fonte", "Fonte trocada");
    }
}
