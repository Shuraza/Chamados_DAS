package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ChamadoAtribuirRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ChamadoStatusRequest;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository repository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @InjectMocks
    private ChamadoService service;

    private Chamado chamado;

    @BeforeEach
    void setUp() {
        chamado = new Chamado("PC não liga", "Não liga desde ontem", Prioridade.ALTA,
                new Cliente("Maria Silva", "maria@email.com", null),
                new Categoria("Hardware", null),
                null);
    }

    @Test
    void fecharChamadoPreencheDataFechamento() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        ChamadoResponse resposta = service.alterarStatus(1L, new ChamadoStatusRequest(StatusChamado.FECHADO));

        assertThat(resposta.status()).isEqualTo(StatusChamado.FECHADO);
        assertThat(resposta.dataFechamento()).isNotNull();
    }

    @Test
    void alterarStatusSemFecharNaoPreencheDataFechamento() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        ChamadoResponse resposta = service.alterarStatus(1L, new ChamadoStatusRequest(StatusChamado.EM_ATENDIMENTO));

        assertThat(resposta.status()).isEqualTo(StatusChamado.EM_ATENDIMENTO);
        assertThat(resposta.dataFechamento()).isNull();
    }

    @Test
    void alterarStatusDeChamadoFechadoFalha() {
        chamado.setStatus(StatusChamado.FECHADO);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> service.alterarStatus(1L, new ChamadoStatusRequest(StatusChamado.ABERTO)))
                .isInstanceOf(ChamadoFechadoException.class)
                .hasMessage("Chamado 1 está fechado e não pode ser alterado");
    }

    @Test
    void atribuirDefineTecnico() {
        Tecnico carlos = new Tecnico("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO);
        carlos.setId(5L);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));
        when(tecnicoRepository.findById(5L)).thenReturn(Optional.of(carlos));

        ChamadoResponse resposta = service.atribuir(1L, new ChamadoAtribuirRequest(5L));

        assertThat(chamado.getTecnico()).isSameAs(carlos);
        assertThat(resposta.tecnicoId()).isEqualTo(5L);
    }

    @Test
    void atribuirTecnicoInexistenteFalha() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));
        when(tecnicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atribuir(1L, new ChamadoAtribuirRequest(99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Técnico 99 não encontrado");
    }

    @Test
    void atribuirEmChamadoFechadoFalha() {
        chamado.setStatus(StatusChamado.FECHADO);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> service.atribuir(1L, new ChamadoAtribuirRequest(5L)))
                .isInstanceOf(ChamadoFechadoException.class);
        verifyNoInteractions(tecnicoRepository);
    }

    @Test
    void excluirComComentariosFalha() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));
        when(comentarioRepository.existsByChamadoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).delete(chamado);
    }

    @Test
    void listarComOrdenacaoInvalidaFalha() {
        PageRequest pagina = PageRequest.of(0, 10, Sort.by("senha"));

        assertThatThrownBy(() -> service.listar(null, null, null, pagina))
                .isInstanceOf(OrdenacaoInvalidaException.class)
                .hasMessageStartingWith("Campo de ordenação inválido: senha");
        verifyNoInteractions(repository);
    }

    @Test
    void listarPorClienteInexistenteFalha() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarPorCliente(99L, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente 99 não encontrado");
    }
}
