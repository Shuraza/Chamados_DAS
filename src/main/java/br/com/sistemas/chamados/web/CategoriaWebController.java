package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.web.form.CategoriaForm;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/categorias")
public class CategoriaWebController {

    private final CategoriaService service;
    private final ChamadoService chamadoService;
    private final ReferenciasWeb referencias;

    public CategoriaWebController(CategoriaService service, ChamadoService chamadoService, ReferenciasWeb referencias) {
        this.service = service;
        this.chamadoService = chamadoService;
        this.referencias = referencias;
    }

    @GetMapping
    public String listar(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CategoriaForm());
        }
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") CategoriaForm form,
                        BindingResult result,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return devolverComErros(form, result, redirect, "redirect:/web/categorias");
        }
        try {
            CategoriaResponse criada = service.criar(form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Categoria cadastrada");
            return "redirect:/web/categorias/" + criada.id();
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            redirect.addFlashAttribute("form", form);
            return "redirect:/web/categorias";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id,
                         @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            CategoriaResponse categoria = service.buscar(id);
            model.addAttribute("categoria", categoria);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", CategoriaForm.de(categoria));
            }
            model.addAttribute("pagina", chamadoService.listarPorCategoria(id, pageable));
        } catch (RecursoNaoEncontradoException | OrdenacaoInvalidaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/categorias";
        }
        referencias.adicionarNomes(model);
        return "categorias/detalhe";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CategoriaForm form,
                            BindingResult result,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return devolverComErros(form, result, redirect, "redirect:/web/categorias/" + id);
        }
        try {
            service.atualizar(id, form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Categoria atualizada");
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            redirect.addFlashAttribute("form", form);
        }
        return "redirect:/web/categorias/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("sucesso", "Categoria excluída");
            return "redirect:/web/categorias";
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/categorias/" + id;
        }
    }

    // Mantém o que foi digitado e as mensagens de validação depois do redirect
    private String devolverComErros(CategoriaForm form, BindingResult result, RedirectAttributes redirect, String destino) {
        redirect.addFlashAttribute("org.springframework.validation.BindingResult.form", result);
        redirect.addFlashAttribute("form", form);
        return destino;
    }
}
