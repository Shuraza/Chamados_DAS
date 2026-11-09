package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TecnicoController.class)
class TecnicoControllerTest {

    private static final String TECNICO_VALIDO = """
            {"nome": "Carlos Souza", "email": "carlos@empresa.com", "perfil": "TECNICO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TecnicoService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new TecnicoResponse(1L, "Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tecnicos/1"))
                .andExpect(jsonPath("$.perfil").value("TECNICO"));

        verify(service).criar(new TecnicoRequest("Carlos Souza", "carlos@empresa.com", Perfil.TECNICO));
    }

    @Test
    void criarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "email": "invalido"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: O nome do técnico é obrigatório",
                        "email: E-mail inválido",
                        "perfil: O perfil do técnico é obrigatório"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComPerfilInexistenteRetorna400() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Carlos Souza", "email": "carlos@empresa.com", "perfil": "GERENTE"}
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe técnico com o e-mail carlos@empresa.com"));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já existe técnico com o e-mail carlos@empresa.com"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Técnico 999 não encontrado"));

        mockMvc.perform(get("/tecnicos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Técnico 999 não encontrado"));
    }
}
