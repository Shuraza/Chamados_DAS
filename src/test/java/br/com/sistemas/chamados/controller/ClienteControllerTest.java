package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String CLIENTE_VALIDO = """
            {"nome": "Maria Silva", "email": "maria@email.com", "telefone": "47999998888"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private ChamadoService chamadoService;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new ClienteResponse(1L, "Maria Silva", "maria@email.com", "47999998888"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/clientes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));

        verify(service).criar(new ClienteRequest("Maria Silva", "maria@email.com", "47999998888"));
    }

    @Test
    void criarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "email": "invalido"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: O nome do cliente é obrigatório",
                        "email: E-mail inválido"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe cliente com o e-mail maria@email.com"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Já existe cliente com o e-mail maria@email.com"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado"));

        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Cliente 999 não encontrado"));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        when(service.atualizar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado"));

        mockMvc.perform(put("/clientes/999").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());

        verify(service).excluir(1L);
    }

    @Test
    void listarChamadosDoClienteRetornaPagina() throws Exception {
        ChamadoResponse chamado = new ChamadoResponse(1L, "PC não liga", "Não liga desde ontem",
                LocalDateTime.of(2026, 11, 9, 8, 0), null, Prioridade.ALTA, StatusChamado.ABERTO, 1L, 1L, null);
        when(chamadoService.listarPorCliente(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(chamado), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/clientes/1/chamados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("PC não liga"))
                .andExpect(jsonPath("$.content[0].clienteId").value(1));
    }

    @Test
    void listarChamadosDeClienteInexistenteRetorna404() throws Exception {
        when(chamadoService.listarPorCliente(eq(999L), any(), any(), any()))
                .thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado"));

        mockMvc.perform(get("/clientes/999/chamados"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cliente 999 não encontrado"));
    }
}
