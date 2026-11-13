   # Financial Management API

Projeto de gestão financeira composto por dois microsserviços Spring Boot, desenvolvido em arquitetura hexagonal (ports & adapters).

## Módulos

| Módulo | Responsabilidade |
|---|---|
| `financemanagement-ms1` | Cadastro e manutenção de usuários, autenticação (JWT), importação em massa por CSV |
| `financemanagement-ms2` | Transações financeiras, saldo e limites (via MockAPI), câmbio (via BrasilAPI), análise de despesas, relatório CSV |

Os dois serviços sobem juntos via Docker Compose, cada um com seu próprio banco PostgreSQL, e se comunicam por REST dentro da rede interna do Compose.

## Arquitetura hexagonal

Cada microsserviço segue o mesmo desenho de pacotes:

```
com.gabroliv.financemanagement
├── domain
│   ├── model        -> Entidades e regras de negócio puras (sem Spring, sem JPA, sem HTTP)
│   └── exception     -> Exceções de domínio
├── application
│   ├── ports
│   │   ├── in         -> Casos de uso (interfaces chamadas pelos adapters de entrada)
│   │   └── out        -> Portas de saída (interfaces implementadas pelos adapters de saída)
│   ├── service        -> Implementação dos casos de uso
│   ├── command/query  -> DTOs internos de entrada/saída dos casos de uso
│   └── exception      -> Exceções de aplicação (ex.: falha ao consultar gateway externo)
└── adapters
    ├── in
    │   ├── web         -> Controllers REST, DTOs HTTP, tratamento global de exceções
    │   └── security     -> Filtro JWT, configuração de segurança
    └── out
        ├── persistence -> Implementação JPA das portas de repositório
        ├── mockapi      -> Cliente HTTP para a MockAPI (saldo e limites) — MS2
        ├── brasilapi    -> Cliente HTTP para a BrasilAPI (câmbio) — MS2
        └── ms1          -> Cliente HTTP do MS2 para o MS1 (validação de usuário) — MS2
```

**Regra de dependência:** `domain` não depende de nada. `application` depende só de `domain`. `adapters` depende de `application` e `domain`, nunca o contrário. Isso é o que permite testar as regras de negócio com JUnit/Mockito sem subir Spring, banco ou HTTP.

## Requisitos

- Docker e Docker Compose
- Java 25 para desenvolvimento local

## Configuração de ambiente

Antes de subir o projeto, crie o arquivo de variáveis local:

```bash
cp .env.example .env
```

Preencha os valores em `.env` antes de iniciar os containers.

### Variáveis de ambiente

| Variável | Serviço | Descrição |
|---|---|---|
| `MS1_POSTGRES_DB` / `_USER` / `_PASSWORD` / `_PORT` | MS1 | Banco de dados do MS1 |
| `MS1_APP_PORT` | MS1 | Porta exposta do MS1 no host |
| `JWT_SECRET` | MS1 e MS2 | Chave Base64 (mín. 256 bits) para assinar/validar o JWT |
| `JWT_EXPIRATION_MS` | MS1 | Tempo de expiração do token, em milissegundos |
| `MS2_POSTGRES_DB` / `_USER` / `_PASSWORD` / `_PORT` | MS2 | Banco de dados do MS2 |
| `MS2_APP_PORT` | MS2 | Porta exposta do MS2 no host |
| `SPRING_PROFILES_ACTIVE` | Ambos | Perfil Spring ativo (`dev`) |
| `MOCK_API_ACCOUNTS_URL` | MS2 | URL base do recurso de contas no MockAPI (ver seção abaixo) |

`JWT_SECRET` precisa ser uma chave Base64 com pelo menos 256 bits (32 bytes). Se o valor for inválido, não-Base64 ou menor que 256 bits, o MS1 não inicia. Gere uma chave válida com:

```bash
openssl rand -base64 32
```

Nunca comite o arquivo `.env` nem exponha o segredo JWT.

### Configurando a MockAPI (saldo e limites)

O MS2 não armazena saldo nem limites no próprio banco — esses dados vêm de um serviço externo simulado no [MockAPI.io](https://mockapi.io):

1. Crie um projeto no MockAPI.io.
2. Crie um recurso (ex.: `account`) com o schema:
   ```json
   { "userId": 1, "balance": 5000.00, "dailyLimit": 2000.00, "transactionLimit": 500.00 }
   ```
3. Cadastre um registro para cada usuário existente no MS1.
4. Copie a URL do recurso (ex.: `https://xxxx.mockapi.io/account`) para `MOCK_API_ACCOUNTS_URL` no `.env`.

Se a MockAPI estiver indisponível, o MS2 não aprova a transação às cegas: ela é rejeitada com o motivo `ACCOUNT_SERVICE_UNAVAILABLE`.

### Câmbio (BrasilAPI)

O MS2 consulta a [BrasilAPI](https://brasilapi.com.br) (`/api/cambio/v1/cotacao/{moeda}/{data}`) no momento da criação da transação, para USD e EUR, usando a cotação de fechamento PTAX da data da transação. Não é necessária nenhuma configuração — o endpoint é público.

**Decisão de design:** a taxa é **gravada na transação no momento da criação**, não consultada a cada leitura. Motivo: garante que o valor histórico da conversão nunca muda, independente de disponibilidade futura da BrasilAPI, e reflete a cotação real do instante da operação. Se a BrasilAPI estiver fora do ar no momento da criação (ou for fim de semana/feriado), o gateway tenta os 3 dias anteriores; se ainda assim não encontrar cotação, a transação é salva normalmente com os campos de câmbio nulos, e a consulta subsequente sinaliza a ausência da conversão.

## Subindo com Docker

```bash
docker compose up --build
```

Isso sobe 4 containers: `postgres-ms1`, `ms1`, `postgres-ms2`, `ms2`. Na primeira subida, o Flyway aplica as migrations de cada serviço, incluindo **dados de demonstração**:

- **MS1**: 5 usuários (`ana.silva`, `bruno.costa`, `carla.souza`, `diego.pereira`, `elisa.martins`), senha `user{N}123` (ex.: `user1123` para `ana.silva`).
- **MS2**: 5 transações por usuário (depósitos, compras em categorias variadas, retiradas, transferências), incluindo exemplos de rejeição (`INSUFFICIENT_BALANCE`, `TRANSACTION_LIMIT_EXCEEDED`) para demonstrar as regras de negócio.

Derrubar e subir novamente mantém o ambiente íntegro (os dados ficam em volumes nomeados: `postgres_ms1_data`, `postgres_ms2_data`).

Os serviços ficam disponíveis em:

| Serviço | URL |
|---|---|
| MS1 | `http://localhost:<MS1_APP_PORT>` |
| MS2 | `http://localhost:<MS2_APP_PORT>` |

## Fluxo de autenticação

1. O cliente se autentica no MS1 (`POST /auth/login`).
2. O MS1 valida as credenciais e retorna um JWT.
3. O cliente envia o JWT no header `Authorization` para acessar recursos protegidos, em ambos os serviços.

```
Authorization: Bearer <jwt-token>
```

O MS1 é responsável por gerar e validar o JWT. O MS2 apenas valida o token (mesmo segredo compartilhado) e extrai o `userId` do claim `sub` — ele não reautentica o usuário a cada chamada.

### Como o MS2 confirma que o usuário existe no MS1

Ao criar uma **transferência**, o MS2 chama `GET {MS1_URL}/users/{id}/exists` para confirmar que o usuário de **destino** existe antes de aprovar a operação. O usuário **remetente** já é garantido pelo próprio JWT válido (se o token foi emitido pelo MS1, o usuário existe). Se o destino não existir, a transação é rejeitada com `ACCOUNT_NOT_FOUND`.

## Comunicação entre serviços

O Docker Compose cria uma rede interna. O MS2 recebe a URL base do MS1 pela variável `MS1_URL`:

```
MS1_URL=http://ms1:8080
```

Dentro da rede do Compose, o MS2 acessa o MS1 pelo nome do serviço (`http://ms1:8080`). Da máquina host, ambos são acessados via `localhost` nas portas expostas. O hostname `ms1` só resolve entre containers.

## Documentação da API (Swagger)

| Serviço | Swagger UI | OpenAPI JSON |
|---|---|---|
| MS1 | `http://localhost:<MS1_APP_PORT>/swagger-ui/index.html` | `http://localhost:<MS1_APP_PORT>/v3/api-docs` |
| MS2 | `http://localhost:<MS2_APP_PORT>/swagger-ui/index.html` | `http://localhost:<MS2_APP_PORT>/v3/api-docs` |

## Endpoints

### MS1 — Usuários

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/users` | Pública | Criar usuário |
| `GET` | `/users` | JWT | Listar usuários |
| `GET` | `/users/{id}` | JWT | Buscar usuário por ID |
| `PUT` | `/users/{id}` | JWT | Atualizar usuário |
| `DELETE` | `/users/{id}` | JWT | Remover usuário |
| `GET` | `/users/{id}/exists` | Pública | Verifica se um usuário existe (uso interno do MS2) |
| `POST` | `/users/import/csv` | Pública | Importação em massa via CSV (multipart) |
| `POST` | `/auth/login` | Pública | Autenticação, retorna JWT |

CSV de exemplo para importação: [`examples/usuarios-exemplo.csv`](examples/usuarios-exemplo.csv).

### MS2 — Transações, contas, câmbio, despesas

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/transactions` | JWT | Registrar depósito, retirada, transferência ou compra |
| `GET` | `/transactions/{id}` | JWT | Consultar transação por ID, com conversão em USD/EUR |
| `GET` | `/transactions?type=&startDate=&endDate=` | JWT | Listar transações do usuário autenticado, com filtros opcionais |
| `GET` | `/transactions/report?startDate=&endDate=` | JWT | Download do relatório CSV de transações |
| `GET` | `/accounts/balance` | JWT | Saldo e limites do usuário autenticado, vindos da MockAPI |
| `GET` | `/expenses/summary?startDate=&endDate=` | JWT | Resumo de despesas por categoria, dia e mês (padrão: mês atual) |

Um usuário autenticado só pode consultar e criar transações em seu próprio nome; tentar acessar transação de outro usuário retorna `403`.

## Validação e tratamento de erros

Ambos os serviços validam a entrada com Bean Validation e retornam erros padronizados no mesmo formato (`ApiError`): timestamp, status, erro, mensagem, path e, quando aplicável, mapa de campo → mensagem.

Códigos de status comuns:

| Status | Descrição |
|---:|---|
| `200` | Requisição concluída com sucesso |
| `201` | Recurso criado |
| `204` | Recurso removido |
| `400` | Dados de requisição inválidos |
| `401` | Token ausente, inválido ou expirado |
| `403` | Acesso negado a recurso de outro usuário |
| `404` | Recurso não encontrado |
| `409` | Email ou username já cadastrado |
| `503` | Serviço externo (MockAPI) indisponível |

## Testes e cobertura

Cada serviço usa JUnit 5 e Mockito para testar as regras críticas de negócio isoladas de Spring, JPA e HTTP:

- **MS1**: `UserService` (cadastro, validações, importação CSV), `JwtService`.
- **MS2**: `TransactionService` (aprovação/rejeição, limites, verificação de usuário, câmbio), `Transaction` (invariantes de domínio).

Rodar os testes e gerar o relatório de cobertura (JaCoCo):

```bash
cd financemanagement-ms1
./mvnw test
# Relatório em financemanagement-ms1/target/site/jacoco/index.html

cd financemanagement-ms2
./mvnw test
# Relatório em financemanagement-ms2/target/site/jacoco/index.html
```

O relatório HTML mostra a cobertura por pacote e classe. As classes de domínio e de serviço (`TransactionService`, `Transaction`, `UserService`) concentram a cobertura mais alta do projeto, por conterem as regras de negócio testadas unitariamente.

## Segredos e configuração

Nenhum segredo fica versionado no repositório. Senhas, URLs de serviços externos e chaves JWT vêm exclusivamente de variáveis de ambiente, com o arquivo `.env.example` versionado como referência de quais variáveis existem.

## Logs

Cada microsserviço grava logs em arquivo, além do console, usando Logback (`logback-spring.xml` em `src/main/resources`):

| Arquivo | Nível | Retenção |
|---|---|---|
| `debug.log` | DEBUG e acima (tudo) | 7 dias, rotação diária |
| `error.log` | ERROR e acima (só falhas) | 30 dias, rotação diária |

No Docker Compose, cada serviço monta sua pasta de logs como volume, persistindo no host:

```yaml
ms1:
  volumes:
    - ./logs/ms1:/app/logs
ms2:
  volumes:
    - ./logs/ms2:/app/logs
```

Os arquivos ficam disponíveis em `logs/ms1/` e `logs/ms2/` na raiz do repositório, mesmo depois de um `docker compose down`.

**O que efetivamente cai no `error.log`:**

| Origem do erro | Vai para `error.log`? |
|---|---|
| Exceção de negócio tratada por um `@ExceptionHandler` do `GlobalExceptionHandler` (ex.: validação, transação não encontrada, acesso negado) | **Não.** O handler só converte a exceção numa resposta HTTP (400/403/404/503); não há `log.error` dentro dele. |
| Exceção sem handler (bug real, `NullPointerException`, erro inesperado) | **Sim.** O Spring Boot/Tomcat captura e loga automaticamente como ERROR, com stack trace completo, antes de responder 500 ao cliente. |
| Falha de integração registrada explicitamente no código (ex.: `MockApiAccountGateway`, `TransactionService.creditBalance`, `Ms1UserGateway`) | **Sim.** Esses pontos chamam `log.error(...)` manualmente dentro do `catch`. |

Ou seja: hoje o `error.log` captura falhas de infraestrutura/integração e bugs não tratados, mas **não** registra rejeições de negócio esperadas (saldo insuficiente, limite excedido, etc.) — essas aparecem só como resposta HTTP ao cliente, por design.

### Detalhes de implementação do container

Como os containers rodam com um usuário não-root (`spring`), o `Dockerfile` de cada serviço copia um `entrypoint.sh` que:
1. Ajusta a permissão da pasta `/app/logs` (necessário porque o volume montado pelo Compose chega com o dono padrão do host, não do usuário `spring`);
2. Troca para o usuário `spring` via `su`, exportando `PATH=/opt/java/openjdk/bin:$PATH` explicitamente (o `su` dessa imagem não preserva o `PATH` original mesmo com a flag `-p`);
3. Só então inicia a aplicação (`java -jar app.jar`).

