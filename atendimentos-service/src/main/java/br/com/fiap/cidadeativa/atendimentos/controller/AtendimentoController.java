package br.com.fiap.cidadeativa.atendimentos.controller;

import br.com.fiap.cidadeativa.atendimentos.dto.request.AlterarStatusRequest;
import br.com.fiap.cidadeativa.atendimentos.dto.request.AtualizarAtendimentoRequest;
import br.com.fiap.cidadeativa.atendimentos.dto.request.CriarAtendimentoRequest;
import br.com.fiap.cidadeativa.atendimentos.dto.response.AtendimentoResponse;
import br.com.fiap.cidadeativa.atendimentos.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Atendimentos")
@RestController
@RequestMapping(AtendimentoController.RECURSO)
public class AtendimentoController {

    static final String RECURSO = "/api/v1/atendimentos";

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar atendimentos por ID crescente")
    @ApiResponse(responseCode = "200", description = "Registros encontrados", useReturnTypeSchema = true)
    public ResponseEntity<List<AtendimentoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar atendimento pelo ID")
    @ApiResponse(responseCode = "200", description = "Registro encontrado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "ID inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AtendimentoResponse> buscar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Criar atendimento", description = "Vincula o atendimento e encaminha a ocorrência.")
    @ApiResponse(responseCode = "201", description = "Registro criado", useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI do recurso criado",
                    schema = @Schema(type = "string", example = RECURSO + "/3")))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Ocorrência não encontrada",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Já existe atendimento ou a ocorrência não permite a operação",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "Serviço de ocorrências indisponível",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AtendimentoResponse> criar(@Valid @RequestBody CriarAtendimentoRequest request) {
        var response = service.criar(request);
        return ResponseEntity.created(URI.create(RECURSO + "/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar dados de atendimento")
    @ApiResponse(responseCode = "200", description = "Dados atualizados", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Dados ou ID inválidos",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AtendimentoResponse> atualizar(@PathVariable @Positive Long id,
            @Valid @RequestBody AtualizarAtendimentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Iniciar ou concluir atendimento",
            description = "Envie EM_ANDAMENTO para iniciar e CONCLUIDO para concluir. A transição atualiza a ocorrência.")
    @ApiResponse(responseCode = "200", description = "Status sincronizado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Status ou ID inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Atendimento ou ocorrência não encontrados",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Transição inválida",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "Serviço de ocorrências indisponível",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AtendimentoResponse> alterarStatus(@PathVariable @Positive Long id,
            @Valid @RequestBody AlterarStatusRequest request) {
        return ResponseEntity.ok(service.alterarStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir atendimento")
    @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content)
    @ApiResponse(responseCode = "400", description = "ID inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Já existe atendimento ou a ocorrência não permite a operação",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "Serviço de ocorrências indisponível",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
