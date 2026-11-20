"""Verifica as APIs CidadeAtiva pelo Gateway usando apenas a biblioteca padrão."""

import argparse
import json
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


OCORRENCIAS = "/api/v1/ocorrencias"
ATENDIMENTOS = "/api/v1/atendimentos"
OCORRENCIA = {
    "titulo": "Semáforo apagado",
    "descricao": "Os sinais estão apagados nos dois sentidos.",
    "categoria": "Mobilidade",
    "endereco": "Av. do Estado, 120, São Paulo - SP",
}
ATENDIMENTO = {
    "responsavel": "Equipe de Sinalização",
    "descricao": "Verificar alimentação elétrica do cruzamento.",
    "prioridade": "ALTA",
}


def requisitar(base, metodo, caminho, esperado=200, dados=None):
    headers = {"Accept": "application/json, application/problem+json, */*"}
    corpo = None
    if dados is not None:
        headers["Content-Type"] = "application/json"
        corpo = json.dumps(dados, ensure_ascii=False).encode("utf-8")
    request = Request(base.rstrip("/") + caminho, data=corpo, headers=headers, method=metodo)
    try:
        resposta = urlopen(request, timeout=12)
    except HTTPError as erro:
        resposta = erro
    with resposta:
        bruto = resposta.read()
        if resposta.code != esperado:
            raise AssertionError(f"{metodo} {caminho}: esperado {esperado}, recebido {resposta.code}: {bruto!r}")
        tipo = resposta.headers.get_content_type()
        conteudo = json.loads(bruto) if bruto and (tipo.endswith("json") or tipo.endswith("+json")) else bruto
        return conteudo, resposta.headers


def aguardar(condicao, segundos=120):
    fim = time.monotonic() + segundos
    while True:
        try:
            return condicao()
        except (AssertionError, URLError, TimeoutError):
            if time.monotonic() >= fim:
                raise
            time.sleep(2)


def validar_registro(eureka, instancias):
    for servico in ("OCORRENCIAS-SERVICE", "ATENDIMENTOS-SERVICE"):
        registro, _ = requisitar(eureka, "GET", f"/eureka/apps/{servico}")
        ativas = {
            (i["ipAddr"], i["port"]["$"])
            for i in registro["application"]["instance"]
            if i["status"] == "UP" and i["port"]["$"] > 0
        }
        assert len(ativas) == instancias, (servico, ativas)
        print(f"{servico} tem {len(ativas)} instância(s) UP no Eureka.")


def validar_documentacao(gateway):
    for recurso in ("ocorrencias", "atendimentos"):
        docs = f"/docs/{recurso}"
        contrato, _ = requisitar(gateway, "GET", docs + "/openapi")
        assert contrato["servers"][0]["url"] == "/", contrato["servers"]
        assert f"/api/v1/{recurso}" in contrato["paths"]
        assert all("/interno/" not in path for path in contrato["paths"])
        swagger, _ = requisitar(gateway, "GET", docs + "/swagger.html")
        assert b"Swagger UI" in swagger
        config, _ = requisitar(gateway, "GET", docs + "/openapi/swagger-config")
        assert config["url"] == docs + "/openapi", config
        requisitar(gateway, "GET", docs + "/swagger-ui/swagger-initializer.js")
    print("Os dois contratos OpenAPI e as interfaces Swagger respondem pelo Gateway.")


def validar_fluxo(gateway):
    ocorrencias, _ = requisitar(gateway, "GET", OCORRENCIAS)
    atendimentos, _ = requisitar(gateway, "GET", ATENDIMENTOS)
    assert any(o["id"] == 1 for o in ocorrencias), ocorrencias
    assert any(a["id"] == 1 and a["ocorrenciaId"] == 2 for a in atendimentos), atendimentos
    print("Cargas iniciais do Flyway disponíveis nas duas APIs.")

    rejeicao, _ = requisitar(gateway, "POST", OCORRENCIAS, 400, {})
    assert "titulo" in rejeicao["campos"]
    requisitar(gateway, "GET", OCORRENCIAS + "/0", 400)
    requisitar(gateway, "POST", ATENDIMENTOS, 400, dict(ATENDIMENTO, prioridade="INEXISTENTE"))
    requisitar(gateway, "POST", ATENDIMENTOS, 404, dict(ATENDIMENTO, ocorrenciaId=9223372036854775807))

    ocorrencia, headers = requisitar(gateway, "POST", OCORRENCIAS, 201, OCORRENCIA)
    ocorrencia_url = f"{OCORRENCIAS}/{ocorrencia['id']}"
    assert headers["Location"] == ocorrencia_url
    assert ocorrencia["status"] == "ABERTA"
    obtida, _ = requisitar(gateway, "GET", ocorrencia_url)
    assert obtida == ocorrencia, obtida
    lista, _ = requisitar(gateway, "GET", OCORRENCIAS)
    assert ocorrencia in lista

    novos_dados = dict(OCORRENCIA, descricao="Os sinais seguem apagados após vistoria.")
    atualizada, _ = requisitar(gateway, "PUT", ocorrencia_url, dados=novos_dados)
    assert atualizada["descricao"] == novos_dados["descricao"]

    dados_atendimento = dict(ATENDIMENTO, ocorrenciaId=ocorrencia["id"])
    atendimento, headers = requisitar(gateway, "POST", ATENDIMENTOS, 201, dados_atendimento)
    atendimento_url = f"{ATENDIMENTOS}/{atendimento['id']}"
    assert headers["Location"] == atendimento_url
    assert atendimento["status"] == "ABERTO"
    obtido, _ = requisitar(gateway, "GET", atendimento_url)
    assert obtido == atendimento, obtido
    lista, _ = requisitar(gateway, "GET", ATENDIMENTOS)
    assert atendimento in lista
    assert requisitar(gateway, "GET", ocorrencia_url)[0]["status"] == "ENCAMINHADA"
    requisitar(gateway, "POST", ATENDIMENTOS, 409, dados_atendimento)
    requisitar(gateway, "DELETE", ocorrencia_url, 409)

    editado, _ = requisitar(gateway, "PUT", atendimento_url, dados=dict(ATENDIMENTO, prioridade="URGENTE"))
    assert editado["prioridade"] == "URGENTE"
    status_url = atendimento_url + "/status"
    requisitar(gateway, "PUT", status_url, 409, {"status": "CONCLUIDO"})
    for status_atendimento, status_ocorrencia in (("EM_ANDAMENTO", "EM_ATENDIMENTO"), ("CONCLUIDO", "RESOLVIDA")):
        alterado, _ = requisitar(gateway, "PUT", status_url, dados={"status": status_atendimento})
        assert alterado["status"] == status_atendimento
        assert requisitar(gateway, "GET", ocorrencia_url)[0]["status"] == status_ocorrencia

    requisitar(gateway, "PUT", status_url, dados={"status": "CONCLUIDO"})
    requisitar(gateway, "DELETE", atendimento_url, 204)
    requisitar(gateway, "GET", atendimento_url, 404)
    assert requisitar(gateway, "GET", ocorrencia_url)[0]["status"] == "RESOLVIDA"
    requisitar(gateway, "DELETE", ocorrencia_url, 204)
    requisitar(gateway, "GET", ocorrencia_url, 404)
    requisitar(gateway, "PUT", f"/api/v1/interno/ocorrencias/{ocorrencia['id']}/atendimento", 404,
               {"atendimentoId": atendimento["id"], "acao": "ATRIBUIR"})
    print("CRUDs, validações, conflitos e sincronização completa verificados.")


def validar_fallback(gateway, ocorrencia_id, atendimento_id):
    url = f"{ATENDIMENTOS}/{atendimento_id}"
    assert requisitar(gateway, "GET", url)[0]["status"] == "ABERTO"
    dados = dict(ATENDIMENTO, ocorrenciaId=ocorrencia_id)
    for _ in range(9):
        erro, headers = requisitar(gateway, "POST", ATENDIMENTOS, 503, dados)
        assert erro["status"] == 503 and "indisponível" in erro["detail"]
        assert headers.get_content_type() == "application/problem+json"
    requisitar(gateway, "PUT", url + "/status", 503, {"status": "EM_ANDAMENTO"})
    requisitar(gateway, "DELETE", url, 503)
    assert requisitar(gateway, "GET", url)[0]["status"] == "ABERTO"
    atendimentos, _ = requisitar(gateway, "GET", ATENDIMENTOS)
    assert all(a["ocorrenciaId"] != ocorrencia_id for a in atendimentos), atendimentos
    print("Fallback retornou 503. Criação, alteração e exclusão falhas sofreram rollback.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gateway", default="http://localhost:8082")
    parser.add_argument("--eureka", default="http://localhost:8761")
    parser.add_argument("--instancias", type=int, choices=range(1, 11), default=1)
    parser.add_argument("--fallback", nargs=2, type=int, metavar=("OCORRENCIA_ID", "ATENDIMENTO_ID"),
                        help="Com Ocorrências parado, informe uma ocorrência sem vínculo e um atendimento ABERTO.")
    args = parser.parse_args()
    if args.fallback:
        validar_fallback(args.gateway, *args.fallback)
        return
    aguardar(lambda: validar_registro(args.eureka, args.instancias))
    aguardar(lambda: requisitar(args.gateway, "GET", OCORRENCIAS))
    aguardar(lambda: requisitar(args.gateway, "GET", ATENDIMENTOS))
    validar_documentacao(args.gateway)
    validar_fluxo(args.gateway)


if __name__ == "__main__":
    main()
