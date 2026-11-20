package br.com.fiap.cidadeativa.atendimentos.repository;

import br.com.fiap.cidadeativa.atendimentos.entity.Atendimento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    boolean existsByOcorrenciaId(Long ocorrenciaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Atendimento a where a.id = :id")
    Optional<Atendimento> buscarParaAlteracao(@Param("id") Long id);
}
