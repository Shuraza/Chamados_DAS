package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    // Filtro nulo é ignorado, então dá para combinar qualquer um deles
    @EntityGraph(attributePaths = {"cliente", "categoria", "tecnico"})
    @Query("""
            select c from Chamado c
            where (:status is null or c.status = :status)
              and (:prioridade is null or c.prioridade = :prioridade)
              and (:clienteId is null or c.cliente.id = :clienteId)
            """)
    Page<Chamado> filtrar(StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "categoria", "tecnico"})
    Page<Chamado> findByCategoriaId(Long categoriaId, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "categoria", "tecnico"})
    Page<Chamado> findByTecnicoId(Long tecnicoId, Pageable pageable);

    boolean existsByClienteId(Long clienteId);
    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByTecnicoId(Long tecnicoId);
}
