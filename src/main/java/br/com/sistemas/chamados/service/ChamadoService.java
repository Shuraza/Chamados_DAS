package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ChamadoAtribuirRequest;
import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ChamadoStatusRequest;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.OrdenacaoInvalidaException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChamadoService {

    private static final Set<String> CAMPOS_ORDENACAO = new LinkedHashSet<>(List.of(
            "id", "titulo", "dataAbertura", "dataFechamento", "prioridade", "status", "cliente.nome", "categoria.nome"
    ));

    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ComentarioRepository comentarioRepository;

    public ChamadoService(ChamadoRepository repository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository,
                          ComentarioRepository comentarioRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Chamado chamado = new Chamado(
                dto.titulo(),
                dto.descricao(),
                dto.prioridade(),
                buscarCliente(dto.clienteId()),
                buscarCategoria(dto.categoriaId()),
                buscarTecnico(dto.tecnicoId())
        );
        return ChamadoResponse.de(repository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable) {
        validarOrdenacao(pageable.getSort());
        return repository.filtrar(status, prioridade, clienteId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, StatusChamado status, Prioridade prioridade, Pageable pageable) {
        buscarCliente(clienteId);
        return listar(status, prioridade, clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        buscarCategoria(categoriaId);
        validarOrdenacao(pageable.getSort());
        return repository.findByCategoriaId(categoriaId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorTecnico(Long tecnicoId, Pageable pageable) {
        buscarTecnico(tecnicoId);
        validarOrdenacao(pageable.getSort());
        return repository.findByTecnicoId(tecnicoId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscar(Long id) {
        return ChamadoDetalheResponse.de(buscarEntity(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(buscarCliente(dto.clienteId()));
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse atribuir(Long id, ChamadoAtribuirRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, ChamadoStatusRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

        chamado.setStatus(dto.status());
        if (dto.status() == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        Chamado chamado = buscarEntity(id);
        if (comentarioRepository.existsByChamadoId(id)) {
            throw new RegraNegocioException("Chamado " + id + " possui comentários vinculados e não pode ser excluído");
        }
        repository.delete(chamado);
    }

    // O sort vai direto para o JPQL, então só aceita campos conhecidos.
    // tecnico.nome fica de fora porque o join esconderia os chamados sem técnico
    private void validarOrdenacao(Sort sort) {
        for (Sort.Order ordem : sort) {
            if (!CAMPOS_ORDENACAO.contains(ordem.getProperty())) {
                throw new OrdenacaoInvalidaException(ordem.getProperty(), CAMPOS_ORDENACAO);
            }
        }
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado " + id + " não encontrado"));
    }

    private Chamado buscarEntityAberto(Long id) {
        Chamado chamado = buscarEntity(id);
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new ChamadoFechadoException(id);
        }
        return chamado;
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente " + id + " não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria " + id + " não encontrada"));
    }

    private Tecnico buscarTecnico(Long id) {
        if (id == null) {
            return null;
        }
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Técnico " + id + " não encontrado"));
    }
}
