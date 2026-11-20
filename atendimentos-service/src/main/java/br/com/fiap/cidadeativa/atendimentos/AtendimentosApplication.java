package br.com.fiap.cidadeativa.atendimentos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(info = @Info(title = "CidadeAtiva | Atendimentos", version = "v1",
        description = "Atribuição de responsáveis e acompanhamento da resolução de ocorrências."),
        servers = @Server(url = "/"))
public class AtendimentosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtendimentosApplication.class, args);
    }
}
