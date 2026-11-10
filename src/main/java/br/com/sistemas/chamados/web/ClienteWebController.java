package br.com.sistemas.chamados.web;

import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.web.form.ClienteForm;
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
@RequestMapping("/web/clientes")
public class ClienteWebController {

    private final ClienteService service;
    private final ChamadoService chamadoService;
    private final ReferenciasWeb referencias;

    public ClienteWebController(ClienteService service, ChamadoService chamadoService, ReferenciasWeb referencias) {
        this.service = service;
        this.chamadoService = chamadoService;
        this.referencias = referencias;
    }

    @GetMapping
    public String listar(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ClienteForm());
        }
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form,
                        BindingResult result,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return devolverComErros(form, result, redirect, "redirect:/web/clientes");
        }
        try {
            ClienteResponse criado = service.criar(form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Cliente cadastrado");
            return "redirect:/web/clientes/" + criado.id();
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            redirect.addFlashAttribute("form", form);
            return "redirect:/web/clientes";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id,
                         @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            ClienteResponse cliente = service.buscar(id);
            model.addAttribute("cliente", cliente);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", ClienteForm.de(cliente));
            }
            model.addAttribute("pagina", chamadoService.listarPorCliente(id, null, null, pageable));
        } catch (RecursoNaoEncontradoException | OrdenacaoInvalidaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/clientes";
        }
        referencias.adicionarNomes(model);
        return "clientes/detalhe";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult result,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return devolverComErros(form, result, redirect, "redirect:/web/clientes/" + id);
        }
        try {
            service.atualizar(id, form.paraRequest());
            redirect.addFlashAttribute("sucesso", "Cliente atualizado");
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            redirect.addFlashAttribute("form", form);
        }
        return "redirect:/web/clientes/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("sucesso", "Cliente excluído");
            return "redirect:/web/clientes";
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/clientes/" + id;
        }
    }

    // Mantém o que foi digitado e as mensagens de validação depois do redirect
    private String devolverComErros(ClienteForm form, BindingResult result, RedirectAttributes redirect, String destino) {
        redirect.addFlashAttribute("org.springframework.validation.BindingResult.form", result);
        redirect.addFlashAttribute("form", form);
        return destino;
    }
}
