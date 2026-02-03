# 📘 API - Artistas e Álbuns (music-api)

API REST desenvolvida em Java (Spring Boot) para cadastro e consulta de artistas e álbuns.


## 🪪 Dados de inscrição e vaga

- Perfil de projeto escolhido: Back-End (Java)
- Processo seletivo: Processo Seletivo Conjunto Nº 001/2026/SEPLAG e demais Órgãos
- Perfil do processo: Engenheiro da Computação - Sênior
- Cargo: Analista de Tecnologia da Informação
- Nome: ARAO ALVES DE FARIAS
- Nº inscrição: 16370
- Email: arao.alves7@gmail.com
- CPF: 036.500.893-19
- RG: 0276300620049
- Celular: (98) 98246-8103
- Data da inscrição: 23/01/2026 10:44:06
- Cidade: Cuiabá
- Local: Secretaria de Estado de Planejamento e Gestão

## 🚀 Tecnologias utilizadas

- Java 21
- Spring Boot 3.5.8
- Spring Web
- Spring WebSocket (STOMP)
- Spring Security (JWT + Refresh Token)
- Spring Boot Actuator (Health / Liveness / Readiness)
- Spring Data JPA
- Spring Cloud OpenFeign
- Flyway Migrations
- PostgreSQL (profile `postgres`) / H2 (profile `local`)
- MinIO (armazenamento S3)
- OpenAPI/Swagger UI (Springdoc)
- Maven
- JUnit 5 (testes)

---

## 🏗️ Decisões e arquitetura

- Camadas:
  - API (controllers + DTOs) em `br.gov.seplag.musicapi.api`
  - Regras/coordenação em `br.gov.seplag.musicapi.service`
  - Persistência em `br.gov.seplag.musicapi.repository` (Spring Data JPA)
  - Entidades JPA em `br.gov.seplag.musicapi.domain`
- Banco de dados:
  - Relacionamento Artista–Álbum N:N via tabela `artista_album`
  - Migrações com Flyway, separadas em `common` + específicas por banco (`h2` / `postgresql`)
  - Carga inicial via migration (`V6__popular_dados_iniciais.sql`)
- Segurança:
  - Endpoints versionados em `/v1/**`
  - JWT com expiração curta (access token) e renovação via refresh token persistido
  - CORS configurável via `CORS_ALLOWED_ORIGINS` (para restringir domínios permitidos)
- Upload e recuperação de capas (MinIO):
  - Upload de uma ou mais imagens de capa por álbum com armazenamento do arquivo no MinIO
  - Metadados persistidos em `album_capa`
  - Recuperação via links pré-assinados com expiração (30 min)
  - Endpoints:
    - `POST /v1/albuns/{id}/capa` (multipart: `arquivo` ou `arquivos`)
    - `GET /v1/albuns/{id}/capa/url` (capa mais recente)
    - `GET /v1/albuns/{id}/capa/urls` (todas as capas do álbum)
- WebSocket:
  - STOMP em `/ws`, tópico `/topic/albuns` notificado a cada novo álbum criado
- Rate limit:
  - Limite por usuário configurável (`app.ratelimit.*`), padrão 10 requisições/minuto
- Regionais (integrador):
  - Importação e sincronização a partir do endpoint do integrador via OpenFeign
  - Versionamento simples de alteração: inativa registro antigo e cria novo

---

## 🧱 Como executar o projeto

### 1. Deploy com Docker (API + Postgres + MinIO + Adminer)

```bash
docker compose up -d --build
```

- API: `http://localhost:8080`
- Postgres: `localhost:5432`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`
- Adminer (visualizar tabelas/dados do Postgres): `http://localhost:5050`
  - System: `PostgreSQL`
  - Server: `postgres`
  - Username: `postgres`
  - Password: `postgres`
  - Database: `musicapi`

O `docker-compose.yml` já define as variáveis de ambiente necessárias (DB, MinIO, JWT e CORS). Para um deploy real, ajuste principalmente:

- `JWT_SECRET` (obrigatório ter 32+ caracteres)
- `CORS_ALLOWED_ORIGINS` (domínios permitidos para chamadas HTTP e WebSocket)
- credenciais do Postgres/MinIO (se necessário)

Comandos úteis:

```bash
docker compose ps
docker compose logs -f api
docker compose down
docker compose down -v
```

Validação rápida:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`

### 2. Subir a aplicação localmente (sem Docker)

```bash
mvn spring-boot:run
```

A aplicação sobe por padrão em `http://localhost:8080`.

Configurações via variáveis de ambiente (opcionais), quando rodando local:

```bash
JWT_SECRET=uma-chave-com-mais-de-32-caracteres
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=musicapi
APP_INTEGRADOR_REGIONAIS_URL=https://integrador-argus-api.geia.vip/v1/regionais
```

### 3. Banco de dados (perfis)

- Padrão: profile `postgres` (PostgreSQL via docker-compose)
- Para usar H2 em memória:

```bash
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

### 3.1 Flyway (migrations)

As migrations estão separadas por tipo de banco para evitar conflitos de versão:

- `src/main/resources/db/migration/common`: migrations comuns (Postgres + H2)
- `src/main/resources/db/migration/postgresql`: migrations específicas do Postgres
- `src/main/resources/db/migration/h2`: migrations específicas do H2

Os profiles `postgres` e `local` configuram `spring.flyway.locations` para apontar para as pastas corretas.

### 4. Swagger (OpenAPI)

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Para testar endpoints protegidos no Swagger:

1) Execute `POST /v1/autenticacao/login`
2) Copie o valor de `accessToken` do response
3) Clique em **Authorize** e cole somente o JWT (sem o prefixo `Bearer`)

### 5. Endpoints liberados

- Ping:
  - `GET http://localhost:8080/v1/ping`
- Health checks (Actuator):
  - `GET http://localhost:8080/actuator/health`
  - `GET http://localhost:8080/actuator/health/liveness`
  - `GET http://localhost:8080/actuator/health/readiness`
- Autenticação:
  - `POST http://localhost:8080/v1/autenticacao/cadastrar`
  - `POST http://localhost:8080/v1/autenticacao/login`
  - `POST http://localhost:8080/v1/autenticacao/renovar`
- Artistas:
  - `POST http://localhost:8080/v1/artistas`
  - `PUT http://localhost:8080/v1/artistas/{id}`
  - `GET http://localhost:8080/v1/artistas/{id}`
  - `GET http://localhost:8080/v1/artistas?nome=Mike&ordem=asc&pagina=0&tamanho=20`
- Álbuns:
  - `POST http://localhost:8080/v1/albuns`
  - `PUT http://localhost:8080/v1/albuns/{id}`
  - `GET http://localhost:8080/v1/albuns/{id}`
  - `GET http://localhost:8080/v1/albuns?titulo=Post&artistaNome=Mike&artistaId=1&ordem=asc&pagina=0&tamanho=20`
  - `POST http://localhost:8080/v1/albuns/{id}/capa` (multipart, campo `arquivo` ou `arquivos`)
  - `GET http://localhost:8080/v1/albuns/{id}/capa/url`
  - `GET http://localhost:8080/v1/albuns/{id}/capa/urls`
- Regionais:
  - `POST http://localhost:8080/v1/regionais/sincronizar`
  - `GET http://localhost:8080/v1/regionais?ativo=true&nome=Regional`
- WebSocket:
  - Endpoint STOMP: `/ws`
  - Tópico: `/topic/albuns` (notifica a cada novo álbum cadastrado)

Os endpoints `/v1/**` exigem `Authorization: Bearer <accessToken>`.

---

## 🔔 WebSocket (notificações)

A API publica uma mensagem no tópico `/topic/albuns` sempre que um álbum é cadastrado via `POST /v1/albuns`.

- Endpoint STOMP: `ws://localhost:8080/ws`
- Tópico: `/topic/albuns`
- Payload: mesmo formato de `AlbumResponse` retornado no `POST /v1/albuns`

O endpoint WebSocket respeita `CORS_ALLOWED_ORIGINS` (mesma variável usada no HTTP).

### Testar com o HTML (teste_websocket.html)

Pré-requisito: a API deve estar rodando na porta 8080.

1) Sirva o arquivo HTML com um servidor estático (na raiz do projeto):

```powershell
jwebserver -p 5500 -d (Get-Location).Path
```

2) Abra no navegador:

- `http://localhost:5500/teste_websocket.html`

3) Crie um álbum via `POST /v1/albuns` (com JWT) e veja a mensagem chegar no tópico `/topic/albuns` na própria página.

Observações:

- Se o navegador bloquear por CORS, ajuste `CORS_ALLOWED_ORIGINS` para incluir `http://localhost:5500` (e reinicie a API).
- Logs 404 para `/@vite/client` no terminal do servidor estático podem ser ignorados.

Rate limit:

- Padrão: 10 requisições/minuto por usuário
- Configuração: `app.ratelimit.enabled` e `app.ratelimit.requests-per-minute`

---

## ✅ Como executar os testes

```bash
mvn clean test
```

Os testes rodam com profile `local` (H2) por padrão.

Para uma execução mais estável (sem forking) e com stacktrace completo:

```bash
mvn -DforkCount=0 -DtrimStackTrace=false test
```

Para rodar um teste (ou classe) específica:

```bash
mvn -Dtest=AlbumServiceUnitTests test
```

Para gerar o JAR sem rodar testes:

```bash
mvn -DskipTests verify
```

---

## 🗃️ Estrutura de diretórios (atual)

```bash
src/main/java
└── br/gov/seplag/musicapi
    ├── api
    │   ├── PingController.java
    │   └── v1
    │       ├── AutenticacaoController.java
    │       ├── AlbumController.java
    │       ├── ArtistaController.java
    │       ├── RegionalController.java
    │       └── dto
    │           ├── AlbumRequest.java
    │           ├── AlbumResponse.java
    │           ├── ArtistaRequest.java
    │           ├── ArtistaResponse.java
    │           ├── ArtistaResumoResponse.java
    │           ├── CadastroUsuarioRequest.java
    │           ├── CapaUrlResponse.java
    │           ├── CapaUrlsResponse.java
    │           ├── LoginRequest.java
    │           ├── RegionalResponse.java
    │           ├── RenovarTokenRequest.java
    │           ├── SincronizarRegionaisResponse.java
    │           └── TokenResponse.java
    ├── config
    │   ├── IntegradorFeignConfig.java
    │   ├── MinioConfig.java
    │   ├── RateLimitFilter.java
    │   ├── SegurancaConfig.java
    │   └── WebSocketConfig.java
    ├── domain
    │   ├── Album.java
    │   ├── Artista.java
    │   ├── CapaAlbum.java
    │   ├── RefreshToken.java
    │   ├── Regional.java
    │   └── Usuario.java
    ├── repository
    │   ├── AlbumRepository.java
    │   ├── ArtistaRepository.java
    │   ├── CapaAlbumRepository.java
    │   ├── RefreshTokenRepository.java
    │   ├── RegionalRepository.java
    │   └── UsuarioRepository.java
    ├── service
    │   ├── AutenticacaoService.java
    │   ├── AlbumService.java
    │   ├── CapaAlbumService.java
    │   ├── ArtistaService.java
    │   ├── RegionaisIntegradorClient.java
    │   └── RegionalService.java
    └── MusicApiApplication.java
src/main/resources
├── application.yml
├── application-local.yml
├── application-postgres.yml
└── db/migration
    ├── common
    │   ├── V1__criar_schema_inicial.sql
    │   ├── V2__criar_tabelas_usuario_e_refresh_token.sql
    │   ├── V3__criar_tabela_album_capa.sql
    │   ├── V4__criar_tabela_regional.sql
    │   ├── V6__popular_dados_iniciais.sql
    │   └── V7__permitir_multiplas_capas_por_album.sql
    ├── h2
    │   └── V5__corrigir_tipo_titulo_album.sql
    └── postgresql
        └── V5__corrigir_tipo_titulo_album.sql
src/test/java
└── br/gov/seplag/musicapi
    ├── ActuatorHealthTests.java
    ├── MusicApiApplicationTests.java
    ├── api/v1
    │   ├── AutenticacaoControllerTests.java
    │   ├── AlbumControllerTests.java
    │   ├── ArtistaControllerTests.java
    │   └── RegionalControllerTests.java
    └── service
        ├── AlbumServiceUnitTests.java
        ├── ArtistaServiceUnitTests.java
        ├── AutenticacaoServiceUnitTests.java
        ├── CapaAlbumServiceUnitTests.java
        └── RegionalServiceUnitTests.java
```

