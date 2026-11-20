package br.com.fiap.cidadeativa.ocorrencias.repository;

import br.com.fiap.cidadeativa.ocorrencias.entity.Ocorrencia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Ocorrencia o where o.id = :id")
    Optional<Ocorrencia> buscarParaAlteracao(@Param("id") Long id);
}
