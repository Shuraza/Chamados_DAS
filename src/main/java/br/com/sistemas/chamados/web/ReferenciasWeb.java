package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.TecnicoService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.stream.Collectors;

// O ChamadoResponse só tem os ids; as tabelas de chamados usam estes mapas para mostrar os nomes
@Component
public class ReferenciasWeb {

    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ReferenciasWeb(ClienteService clienteService, CategoriaService categoriaService, TecnicoService tecnicoService) {
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    public void adicionarNomes(Model model) {
        model.addAttribute("nomesClientes", clienteService.listar().stream()
                .collect(Collectors.toMap(ClienteResponse::id, ClienteResponse::nome)));
        model.addAttribute("nomesCategorias", categoriaService.listar().stream()
                .collect(Collectors.toMap(CategoriaResponse::id, CategoriaResponse::nome)));
        model.addAttribute("nomesTecnicos", tecnicoService.listar().stream()
                .collect(Collectors.toMap(TecnicoResponse::id, TecnicoResponse::nome)));
    }
}
