package br.com.fiap.cidadeativa.ocorrencias.controller;

import br.com.fiap.cidadeativa.ocorrencias.dto.request.OcorrenciaRequest;
import br.com.fiap.cidadeativa.ocorrencias.dto.response.OcorrenciaResponse;
import br.com.fiap.cidadeativa.ocorrencias.service.OcorrenciaService;
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

@Tag(name = "Ocorrências")
@RestController
@RequestMapping(OcorrenciaController.RECURSO)
public class OcorrenciaController {

    static final String RECURSO = "/api/v1/ocorrencias";

    private final OcorrenciaService service;

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar ocorrências por ID crescente")
    @ApiResponse(responseCode = "200", description = "Registros encontrados", useReturnTypeSchema = true)
    public ResponseEntity<List<OcorrenciaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ocorrência pelo ID")
    @ApiResponse(responseCode = "200", description = "Registro encontrado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "ID inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<OcorrenciaResponse> buscar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Criar ocorrência")
    @ApiResponse(responseCode = "201", description = "Registro criado", useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI do recurso criado",
                    schema = @Schema(type = "string", example = RECURSO + "/3")))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<OcorrenciaResponse> criar(@Valid @RequestBody OcorrenciaRequest request) {
        var response = service.criar(request);
        return ResponseEntity.created(URI.create(RECURSO + "/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar dados de ocorrência")
    @ApiResponse(responseCode = "200", description = "Dados atualizados", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Dados ou ID inválidos",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<OcorrenciaResponse> atualizar(@PathVariable @Positive Long id,
            @Valid @RequestBody OcorrenciaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ocorrência")
    @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content)
    @ApiResponse(responseCode = "400", description = "ID inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Exclua o atendimento vinculado primeiro",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
