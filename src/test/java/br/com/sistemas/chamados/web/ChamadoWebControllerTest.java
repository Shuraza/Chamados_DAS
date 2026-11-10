package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChamadoWebController.class)
@Import(ReferenciasWeb.class)
class ChamadoWebControllerTest {

    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 11, 9, 8, 0);
    private static final ClienteResponse MARIA = new ClienteResponse(1L, "Maria Silva", "maria@email.com", null);
    private static final CategoriaResponse HARDWARE = new CategoriaResponse(1L, "Hardware", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService chamadoService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private TecnicoService tecnicoService;

    @MockitoBean
    private ComentarioService comentarioService;

    @Test
    void listarMostraChamadosComNomes() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(MARIA));
        when(categoriaService.listar()).thenReturn(List.of(HARDWARE));
        when(chamadoService.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(
                new ChamadoResponse(1L, "PC não liga", "Não liga desde ontem", ABERTURA, null,
                        Prioridade.ALTA, StatusChamado.ABERTO, 1L, 1L, null)
        ), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/web/chamados"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/lista"))
                .andExpect(content().string(containsString("PC não liga")))
                .andExpect(content().string(containsString("Maria Silva")))
                .andExpect(content().string(containsString("Hardware")));
    }

    @Test
    void novoPreSelecionaCliente() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(MARIA));

        mockMvc.perform(get("/web/chamados/novo").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/form"))
                .andExpect(model().attribute("form", org.hamcrest.Matchers.hasProperty("clienteId", org.hamcrest.Matchers.is(1L))));
    }

    @Test
    void criarComCamposInvalidosVoltaParaOFormulario() throws Exception {
        mockMvc.perform(post("/web/chamados"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/form"))
                .andExpect(model().attributeHasFieldErrors("form", "titulo", "descricao", "prioridade", "clienteId", "categoriaId"))
                .andExpect(content().string(containsString("O título do chamado é obrigatório")));

        verifyNoInteractions(chamadoService);
    }

    @Test
    void criarRedirecionaParaODetalhe() throws Exception {
        when(chamadoService.criar(any())).thenReturn(new ChamadoResponse(7L, "PC não liga", "Não liga desde ontem",
                ABERTURA, null, Prioridade.ALTA, StatusChamado.ABERTO, 1L, 1L, null));

        mockMvc.perform(post("/web/chamados")
                        .param("titulo", "PC não liga")
                        .param("descricao", "Não liga desde ontem")
                        .param("prioridade", "ALTA")
                        .param("clienteId", "1")
                        .param("categoriaId", "1"))
                .andExpect(redirectedUrl("/web/chamados/7"))
                .andExpect(flash().attribute("sucesso", "Chamado #7 aberto com sucesso"));

        verify(chamadoService).criar(new ChamadoRequest("PC não liga", "Não liga desde ontem", Prioridade.ALTA, 1L, 1L, null));
    }

    @Test
    void detalheMostraComentariosELinks() throws Exception {
        when(chamadoService.buscar(1L)).thenReturn(new ChamadoDetalheResponse(
                1L, "PC não liga", "Não liga desde ontem", ABERTURA, null, Prioridade.ALTA, StatusChamado.ABERTO,
                MARIA, HARDWARE, null,
                List.of(new ComentarioResponse(1L, "Verificando a fonte", ABERTURA.plusHours(1), "Carlos Souza", 1L))
        ));

        mockMvc.perform(get("/web/chamados/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/detalhe"))
                .andExpect(content().string(containsString("Verificando a fonte")))
                .andExpect(content().string(containsString("/web/clientes/1")))
                .andExpect(content().string(containsString("/web/categorias/1")));
    }

    @Test
    void detalheDeChamadoInexistenteVoltaParaALista() throws Exception {
        when(chamadoService.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Chamado 999 não encontrado"));

        mockMvc.perform(get("/web/chamados/999"))
                .andExpect(redirectedUrl("/web/chamados"))
                .andExpect(flash().attribute("erro", "Chamado 999 não encontrado"));
    }

    @Test
    void alterarStatusDeChamadoFechadoMostraErro() throws Exception {
        when(chamadoService.alterarStatus(eq(1L), any())).thenThrow(new ChamadoFechadoException(1L));

        mockMvc.perform(post("/web/chamados/1/status").param("status", "ABERTO"))
                .andExpect(redirectedUrl("/web/chamados/1"))
                .andExpect(flash().attribute("erro", "Chamado 1 está fechado e não pode ser alterado"));
    }

    @Test
    void comentarSemTextoNaoChamaOService() throws Exception {
        mockMvc.perform(post("/web/chamados/1/comentarios").param("autor", "Carlos Souza"))
                .andExpect(redirectedUrl("/web/chamados/1"));

        verifyNoInteractions(comentarioService);
    }
}
