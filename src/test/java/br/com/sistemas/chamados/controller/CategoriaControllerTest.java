package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    private static final String CATEGORIA_VALIDA = """
            {"nome": "Hardware", "descricao": "Problemas de equipamento"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new CategoriaResponse(1L, "Hardware", "Problemas de equipamento"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categorias/1"))
                .andExpect(jsonPath("$.nome").value("Hardware"));

        verify(service).criar(new CategoriaRequest("Hardware", "Problemas de equipamento"));
    }

    @Test
    void criarSemNomeRetorna400() throws Exception {
        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": " "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("nome: O nome da categoria é obrigatório"));

        verifyNoInteractions(service);
    }

    @Test
    void criarComNomeDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe categoria com o nome Hardware"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já existe categoria com o nome Hardware"));
    }

    @Test
    void listarRetornaTodas() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new CategoriaResponse(1L, "Hardware", null),
                new CategoriaResponse(2L, "Software", null)
        ));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].nome").value("Software"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Categoria 999 não encontrada"));

        mockMvc.perform(get("/categorias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Categoria 999 não encontrada"));
    }

    @Test
    void excluirComChamadosVinculadosRetorna409() throws Exception {
        doThrow(new RegraNegocioException("Categoria 1 possui chamados vinculados e não pode ser excluída"))
                .when(service).excluir(1L);

        mockMvc.perform(delete("/categorias/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Categoria 1 possui chamados vinculados e não pode ser excluída"));
    }
}
