package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.ChamadoAtribuirRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ChamadoStatusRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import br.com.sistemas.chamados.web.form.ChamadoForm;
import br.com.sistemas.chamados.web.form.ComentarioForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/web/chamados")
public class ChamadoWebController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ComentarioService comentarioService;
    private final ReferenciasWeb referencias;

    public ChamadoWebController(ChamadoService chamadoService,
                                ClienteService clienteService,
                                CategoriaService categoriaService,
                                TecnicoService tecnicoService,
                                ComentarioService comentarioService,
                                ReferenciasWeb referencias) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.comentarioService = comentarioService;
        this.referencias = referencias;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusChamado status,
                         @RequestParam(required = false) Prioridade prioridade,
                         @RequestParam(required = false) Long clienteId,
                         @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {
        Page<ChamadoResponse> pagina;
        try {
            pagina = chamadoService.listar(status, prioridade, clienteId, pageable);
        } catch (OrdenacaoInvalidaException ex) {
            model.addAttribute("erro", ex.getMessage());
            pagina = Page.empty(pageable);
        }

        model.addAttribute("pagina", pagina);
        model.addAttribute("status", status);
        model.addAttribute("prioridade", prioridade);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("clientes", clienteService.listar());
        referencias.adicionarNomes(model);
        model.addAttribute("statusOpcoes", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        return "chamados/lista";
    }

    // Os parâmetros opcionais vêm dos links "abrir chamado" nas páginas de cliente, categoria e técnico
    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId,
                       @RequestParam(required = false) Long categoriaId,
                       @RequestParam(required = false) Long tecnicoId,
                       Model model) {
        ChamadoForm form = new ChamadoForm();
        form.setClienteId(clienteId);
        form.setCategoriaId(categoriaId);
        form.setTecnicoId(tecnicoId);
        model.addAttribute("form", form);
        adicionarOpcoesFormulario(model);
        return "chamados/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ChamadoForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            adicionarOpcoesFormulario(model);
            return "chamados/form";
        }
        try {
            ChamadoResponse criado = chamadoService.criar(form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Chamado #" + criado.id() + " aberto com sucesso");
            return "redirect:/web/chamados/" + criado.id();
        } catch (RecursoNaoEncontradoException ex) {
            model.addAttribute("erro", ex.getMessage());
            adicionarOpcoesFormulario(model);
            return "chamados/form";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("chamado", chamadoService.buscar(id));
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/chamados";
        }
        if (!model.containsAttribute("comentario")) {
            model.addAttribute("comentario", new ComentarioForm());
        }
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("statusOpcoes", StatusChamado.values());
        return "chamados/detalhe";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusChamado status,
                                RedirectAttributes redirect) {
        try {
            chamadoService.alterarStatus(id, new ChamadoStatusRequest(status));
            redirect.addFlashAttribute("sucesso", "Status alterado para " + status);
        } catch (RecursoNaoEncontradoException | ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam(required = false) Long tecnicoId,
                           RedirectAttributes redirect) {
        if (tecnicoId == null) {
            redirect.addFlashAttribute("erro", "Selecione um técnico");
            return "redirect:/web/chamados/" + id;
        }
        try {
            chamadoService.atribuir(id, new ChamadoAtribuirRequest(tecnicoId));
            redirect.addFlashAttribute("sucesso", "Técnico atribuído ao chamado");
        } catch (RecursoNaoEncontradoException | ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @Valid @ModelAttribute("comentario") ComentarioForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.comentario", result);
            redirect.addFlashAttribute("comentario", form);
            return "redirect:/web/chamados/" + id;
        }
        try {
            comentarioService.criar(id, form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Comentário adicionado");
        } catch (RecursoNaoEncontradoException | ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            chamadoService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Chamado #" + id + " excluído");
            return "redirect:/web/chamados";
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/chamados/" + id;
        }
    }

    private void adicionarOpcoesFormulario(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("prioridades", Prioridade.values());
    }
}
