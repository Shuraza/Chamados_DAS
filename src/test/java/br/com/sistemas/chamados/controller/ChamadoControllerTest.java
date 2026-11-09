package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.dto.ChamadoAtribuirRequest;
import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ChamadoStatusRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.service.ChamadoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    private static final String CHAMADO_VALIDO = """
            {
              "titulo": "PC não liga",
              "descricao": "Computador não liga desde ontem",
              "prioridade": "ALTA",
              "clienteId": 1,
              "categoriaId": 1
            }
            """;
    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 11, 9, 8, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(resposta(StatusChamado.ABERTO, null));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/chamados/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.tecnicoId").isEmpty());

        verify(service).criar(new ChamadoRequest("PC não liga", "Computador não liga desde ontem", Prioridade.ALTA, 1L, 1L, null));
    }

    @Test
    void criarSemCamposObrigatoriosRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "titulo: O título do chamado é obrigatório",
                        "descricao: A descrição do chamado é obrigatória",
                        "prioridade: A prioridade do chamado é obrigatória",
                        "clienteId: O cliente do chamado é obrigatório",
                        "categoriaId: A categoria do chamado é obrigatória"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComTituloMuitoLongoRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content(CHAMADO_VALIDO.replace("PC não liga", "a".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("titulo: O título do chamado deve ter no máximo 100 caracteres"));

        verifyNoInteractions(service);
    }

    @Test
    void criarComPrioridadeInexistenteRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content(CHAMADO_VALIDO.replace("ALTA", "URGENTE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void criarComClienteInexistenteRetorna404() throws Exception {
        when(service.criar(any())).thenThrow(new RecursoNaoEncontradoException("Cliente 1 não encontrado"));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cliente 1 não encontrado"));
    }

    @Test
    void listarRepassaFiltrosEPaginacao() throws Exception {
        when(service.listar(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(resposta(StatusChamado.ABERTO, null)), PageRequest.of(1, 5), 6));

        mockMvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("prioridade", "ALTA")
                        .param("clienteId", "1")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "titulo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo").value("PC não liga"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(eq(StatusChamado.ABERTO), eq(Prioridade.ALTA), eq(1L), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("titulo")).isNotNull();
    }

    @Test
    void listarSemParametrosOrdenaPorDataAberturaDesc() throws Exception {
        when(service.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/chamados"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(isNull(), isNull(), isNull(), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("dataAbertura").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listarComStatusInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/chamados").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor inválido para o parâmetro status: QUALQUER"));

        verifyNoInteractions(service);
    }

    @Test
    void listarComOrdenacaoInvalidaRetorna400() throws Exception {
        when(service.listar(any(), any(), any(), any()))
                .thenThrow(new OrdenacaoInvalidaException("senha", Set.of("id")));

        mockMvc.perform(get("/chamados").param("sort", "senha"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Campo de ordenação inválido: senha. Permitidos: id"));
    }

    @Test
    void buscarRetornaDetalheComComentarios() throws Exception {
        when(service.buscar(1L)).thenReturn(new ChamadoDetalheResponse(
                1L, "PC não liga", "Computador não liga desde ontem", ABERTURA, null,
                Prioridade.ALTA, StatusChamado.EM_ATENDIMENTO,
                new ClienteResponse(1L, "Maria Silva", "maria@email.com", null),
                new CategoriaResponse(1L, "Hardware", null),
                new TecnicoResponse(5L, "Carlos Souza", "carlos@empresa.com", Perfil.TECNICO),
                List.of(
                        new ComentarioResponse(1L, "Verificando a fonte", ABERTURA.plusHours(1), "Carlos Souza", 1L),
                        new ComentarioResponse(2L, "Fonte trocada", ABERTURA.plusHours(2), "Carlos Souza", 1L)
                )
        ));

        mockMvc.perform(get("/chamados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.tecnico.nome").value("Carlos Souza"))
                .andExpect(jsonPath("$.comentarios", hasSize(2)))
                .andExpect(jsonPath("$.comentarios[0].texto").value("Verificando a fonte"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Chamado 999 não encontrado"));

        mockMvc.perform(get("/chamados/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Chamado 999 não encontrado"));
    }

    @Test
    void atribuirRetorna200() throws Exception {
        when(service.atribuir(eq(1L), any())).thenReturn(resposta(StatusChamado.ABERTO, 5L));

        mockMvc.perform(patch("/chamados/1/atribuir").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tecnicoId": 5}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoId").value(5));

        verify(service).atribuir(1L, new ChamadoAtribuirRequest(5L));
    }

    @Test
    void atribuirSemTecnicoRetorna400() throws Exception {
        mockMvc.perform(patch("/chamados/1/atribuir").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("tecnicoId: O técnico do chamado é obrigatório"));

        verifyNoInteractions(service);
    }

    @Test
    void atribuirEmChamadoFechadoRetorna409() throws Exception {
        when(service.atribuir(eq(1L), any())).thenThrow(new ChamadoFechadoException(1L));

        mockMvc.perform(patch("/chamados/1/atribuir").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tecnicoId": 5}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Chamado 1 está fechado e não pode ser alterado"));
    }

    @Test
    void alterarStatusRetorna200() throws Exception {
        when(service.alterarStatus(eq(1L), any())).thenReturn(resposta(StatusChamado.FECHADO, null));

        mockMvc.perform(patch("/chamados/1/status").contentType(MediaType.APPLICATION_JSON).content("""
                        {"status": "FECHADO"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"));

        verify(service).alterarStatus(1L, new ChamadoStatusRequest(StatusChamado.FECHADO));
    }

    private ChamadoResponse resposta(StatusChamado status, Long tecnicoId) {
        return new ChamadoResponse(1L, "PC não liga", "Computador não liga desde ontem", ABERTURA, null,
                Prioridade.ALTA, status, 1L, 1L, tecnicoId);
    }
}
