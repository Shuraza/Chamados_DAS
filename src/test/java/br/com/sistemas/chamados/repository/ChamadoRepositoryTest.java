package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ChamadoRepositoryTest {

    private static final Pageable PAGINA = PageRequest.of(0, 10, Sort.by("id"));

    @Autowired
    private ChamadoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    private Cliente maria;
    private Cliente pedro;
    private Categoria hardware;
    private Tecnico carlos;

    @BeforeEach
    void setUp() {
        maria = clienteRepository.save(new Cliente("Maria Silva", "maria@email.com", null));
        pedro = clienteRepository.save(new Cliente("Pedro Costa", "pedro@email.com", null));
        hardware = categoriaRepository.save(new Categoria("Hardware", null));
        carlos = tecnicoRepository.save(new Tecnico("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));
    }

    @Test
    void salvarDefineStatusAbertoEDataAbertura() {
        Chamado salvo = repository.save(chamado("PC não liga", Prioridade.ALTA, maria));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusChamado.ABERTO);
        assertThat(salvo.getDataAbertura()).isNotNull();
        assertThat(salvo.getDataFechamento()).isNull();
    }

    @Test
    void salvarSemClienteFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(chamado("PC não liga", Prioridade.ALTA, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarSemCategoriaFalha() {
        Chamado semCategoria = new Chamado("PC não liga", "Descrição", Prioridade.ALTA, maria, null, null);

        assertThatThrownBy(() -> repository.saveAndFlush(semCategoria))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filtrarSemFiltrosRetornaTodos() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PAGINA);

        assertThat(pagina.getTotalElements()).isEqualTo(4);
    }

    @Test
    void filtrarPorStatus() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.FECHADO, null, null, PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Impressora travada");
    }

    @Test
    void filtrarPorPrioridade() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, Prioridade.ALTA, null, PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Sistema lento", "Sem acesso à VPN");
    }

    @Test
    void filtrarPorCliente() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, pedro.getId(), PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Sem acesso à VPN");
    }

    @Test
    void filtrarCombinado() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.ABERTO, Prioridade.ALTA, maria.getId(), PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Sistema lento");
    }

    @Test
    void filtrarSemResultado() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.AGUARDANDO_CLIENTE, null, null, PAGINA);

        assertThat(pagina.getContent()).isEmpty();
        assertThat(pagina.getTotalElements()).isZero();
    }

    @Test
    void filtrarPaginaEOrdena() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(1, 2, Sort.by("titulo")));

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Sem acesso à VPN", "Sistema lento");
        assertThat(pagina.getTotalElements()).isEqualTo(4);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void filtrarOrdenaPorNomeDoCliente() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "cliente.nome")));

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Sem acesso à VPN");
    }

    @Test
    void existsByClienteId() {
        repository.save(chamado("PC não liga", Prioridade.ALTA, maria));

        assertThat(repository.existsByClienteId(maria.getId())).isTrue();
        assertThat(repository.existsByClienteId(pedro.getId())).isFalse();
    }

    @Test
    void existsByCategoriaId() {
        Categoria software = categoriaRepository.save(new Categoria("Software", null));
        repository.save(chamado("PC não liga", Prioridade.ALTA, maria));

        assertThat(repository.existsByCategoriaId(hardware.getId())).isTrue();
        assertThat(repository.existsByCategoriaId(software.getId())).isFalse();
    }

    @Test
    void existsByTecnicoId() {
        Tecnico julia = tecnicoRepository.save(new Tecnico("Julia Alves", "julia@empresa.com", Perfil.ADMIN));
        Chamado chamado = chamado("PC não liga", Prioridade.ALTA, maria);
        chamado.setTecnico(carlos);
        repository.save(chamado);

        assertThat(repository.existsByTecnicoId(carlos.getId())).isTrue();
        assertThat(repository.existsByTecnicoId(julia.getId())).isFalse();
    }

    // Ordem alfabética dos títulos: Impressora travada, PC não liga, Sem acesso à VPN, Sistema lento
    private void criarMassa() {
        repository.save(chamado("PC não liga", Prioridade.BAIXA, maria));
        repository.save(chamado("Sistema lento", Prioridade.ALTA, maria));
        Chamado fechado = repository.save(chamado("Impressora travada", Prioridade.CRITICA, maria));
        fechado.setStatus(StatusChamado.FECHADO);
        repository.save(chamado("Sem acesso à VPN", Prioridade.ALTA, pedro));
        repository.flush();
    }

    private Chamado chamado(String titulo, Prioridade prioridade, Cliente cliente) {
        return new Chamado(titulo, "Descrição", prioridade, cliente, hardware, null);
    }
}
