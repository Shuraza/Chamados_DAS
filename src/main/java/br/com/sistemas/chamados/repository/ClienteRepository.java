package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
