package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ClienteWebController.class)
@Import(ReferenciasWeb.class)
class ClienteWebControllerTest {

    private static final ClienteResponse MARIA = new ClienteResponse(1L, "Maria Silva", "maria@email.com", "47999998888");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private ChamadoService chamadoService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private TecnicoService tecnicoService;

    @Test
    void listarMostraClientes() throws Exception {
        when(service.listar()).thenReturn(List.of(MARIA));

        mockMvc.perform(get("/web/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/lista"))
                .andExpect(content().string(containsString("Maria Silva")))
                .andExpect(content().string(containsString("/web/clientes/1")));
    }

    @Test
    void detalheMostraChamadosDoCliente() throws Exception {
        when(service.buscar(1L)).thenReturn(MARIA);
        when(service.listar()).thenReturn(List.of(MARIA));
        when(chamadoService.listarPorCliente(eq(1L), any(), any(), any())).thenReturn(new PageImpl<>(List.of(
                new ChamadoResponse(3L, "Sistema lento", "Demora para abrir", LocalDateTime.of(2026, 11, 9, 8, 0), null,
                        Prioridade.MEDIA, StatusChamado.EM_ATENDIMENTO, 1L, 1L, null)
        ), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/web/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/detalhe"))
                .andExpect(content().string(containsString("Sistema lento")))
                .andExpect(content().string(containsString("/web/chamados/3")))
                .andExpect(content().string(containsString("/web/chamados/novo?clienteId=1")));
    }

    @Test
    void criarRedirecionaParaODetalhe() throws Exception {
        when(service.criar(any())).thenReturn(MARIA);

        mockMvc.perform(post("/web/clientes")
                        .param("nome", "Maria Silva")
                        .param("email", "maria@email.com")
                        .param("telefone", "47999998888"))
                .andExpect(redirectedUrl("/web/clientes/1"));

        verify(service).criar(new ClienteRequest("Maria Silva", "maria@email.com", "47999998888"));
    }

    @Test
    void criarComEmailInvalidoNaoChamaOService() throws Exception {
        mockMvc.perform(post("/web/clientes").param("nome", "Maria Silva").param("email", "invalido"))
                .andExpect(redirectedUrl("/web/clientes"));

        verifyNoInteractions(service);
    }

    @Test
    void excluirComChamadosMostraErro() throws Exception {
        doThrow(new RegraNegocioException("Cliente 1 possui chamados vinculados e não pode ser excluído"))
                .when(service).excluir(1L);

        mockMvc.perform(post("/web/clientes/1/excluir"))
                .andExpect(redirectedUrl("/web/clientes/1"))
                .andExpect(flash().attribute("erro", "Cliente 1 possui chamados vinculados e não pode ser excluído"));
    }
}
