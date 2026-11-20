package br.com.fiap.cidadeativa.ocorrencias;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "CidadeAtiva | Ocorrências", version = "v1",
        description = "Registro e acompanhamento de problemas de infraestrutura urbana."),
        servers = @Server(url = "/"))
public class OcorrenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcorrenciasApplication.class, args);
    }
}
