package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.service.ComentarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
class ComentarioControllerTest {

    private static final String COMENTARIO_VALIDO = """
            {"texto": "Verificando a fonte", "autor": "Carlos Souza"}
            """;
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 11, 9, 9, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComentarioService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(eq(1L), any())).thenReturn(new ComentarioResponse(10L, "Verificando a fonte", DATA, "Carlos Souza", 1L));

        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON).content(COMENTARIO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/chamados/1/comentarios/10"))
                .andExpect(jsonPath("$.texto").value("Verificando a fonte"))
                .andExpect(jsonPath("$.data").value("2026-11-09T09:00:00"));

        verify(service).criar(1L, new ComentarioRequest("Verificando a fonte", "Carlos Souza"));
    }

    @Test
    void criarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON).content("""
                        {"texto": "", "autor": " "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "texto: O texto do comentário é obrigatório",
                        "autor: O autor do comentário é obrigatório"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarEmChamadoInexistenteRetorna404() throws Exception {
        when(service.criar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Chamado 999 não encontrado"));

        mockMvc.perform(post("/chamados/999/comentarios").contentType(MediaType.APPLICATION_JSON).content(COMENTARIO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Chamado 999 não encontrado"));
    }

    @Test
    void criarEmChamadoFechadoRetorna409() throws Exception {
        when(service.criar(eq(1L), any())).thenThrow(new ChamadoFechadoException(1L));

        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON).content(COMENTARIO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Chamado 1 está fechado e não pode ser alterado"));
    }

    @Test
    void listarRetornaComentariosDoChamado() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(
                new ComentarioResponse(10L, "Verificando a fonte", DATA, "Carlos Souza", 1L),
                new ComentarioResponse(11L, "Fonte trocada", DATA.plusHours(1), "Carlos Souza", 1L)
        ));

        mockMvc.perform(get("/chamados/1/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].texto").value("Fonte trocada"));
    }
}
