package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Comentários", description = "Comentários de um chamado")
@RequestMapping("/chamados/{chamadoId}/comentarios")
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @Operation(summary = "Adiciona um comentário ao chamado")
    @PostMapping
    public ResponseEntity<ComentarioResponse> criar(
            @PathVariable Long chamadoId,
            @Valid @RequestBody ComentarioRequest dto) {
        ComentarioResponse criado = service.criar(chamadoId, dto);
        return ResponseEntity.created(URI.create("/chamados/" + chamadoId + "/comentarios/" + criado.id())).body(criado);
    }

    @Operation(summary = "Lista os comentários do chamado")
    @GetMapping
    public List<ComentarioResponse> listar(@PathVariable Long chamadoId) {
        return service.listar(chamadoId);
    }
}
