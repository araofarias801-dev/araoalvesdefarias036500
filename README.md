# 📘 API - Artistas e Álbuns (music-api)

API REST desenvolvida em Java (Spring Boot) para cadastro e consulta de artistas e álbuns.

O enunciado completo está em `prova.txt` na raiz do projeto.

## 🚀 Tecnologias utilizadas (atual)

- Java 21
- Spring Boot 3.5.8
- Spring Web
- Spring Boot Actuator (Health / Liveness / Readiness)
- Maven
- JUnit 5 (testes)

---

## 🧱 Como executar o projeto

### 1. Subir a aplicação

```bash
mvn spring-boot:run
```

A aplicação sobe por padrão em `http://localhost:8080`.

Configurações via variáveis de ambiente (opcionais):

```bash
JWT_SECRET=uma-chave-com-mais-de-32-caracteres
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=musicapi
```

### 2. Banco de dados (perfis)

- Padrão: profile `local` (H2 em memória)
- Para usar PostgreSQL:

```bash
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

### 3. Endpoints liberados

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
  - `POST http://localhost:8080/v1/albuns/{id}/capa` (multipart, campo `arquivo`)
  - `GET http://localhost:8080/v1/albuns/{id}/capa/url`

Os endpoints `/v1/**` exigem `Authorization: Bearer <accessToken>`.

---

## ✅ Como executar os testes

```bash
mvn clean test
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
    │       └── dto
    │           ├── CadastroUsuarioRequest.java
    │           ├── AlbumRequest.java
    │           ├── AlbumResponse.java
    │           ├── CapaUrlResponse.java
    │           ├── LoginRequest.java
    │           ├── RenovarTokenRequest.java
    │           ├── TokenResponse.java
    │           ├── ArtistaRequest.java
    │           └── ArtistaResponse.java
    ├── config
    │   ├── MinioConfig.java
    │   ├── RateLimitFilter.java
    │   └── SegurancaConfig.java
    ├── domain
    │   ├── Album.java
    │   ├── Artista.java
    │   ├── CapaAlbum.java
    │   ├── RefreshToken.java
    │   └── Usuario.java
    ├── repository
    │   ├── AlbumRepository.java
    │   ├── ArtistaRepository.java
    │   ├── CapaAlbumRepository.java
    │   ├── RefreshTokenRepository.java
    │   └── UsuarioRepository.java
    ├── service
    │   ├── AutenticacaoService.java
    │   ├── AlbumService.java
    │   ├── CapaAlbumService.java
    │   └── ArtistaService.java
    └── MusicApiApplication.java
src/main/resources
├── application.yml
├── application-local.yml
├── application-postgres.yml
└── db/migration
    ├── V1__criar_schema_inicial.sql
    ├── V2__criar_tabelas_usuario_e_refresh_token.sql
    └── V3__criar_tabela_album_capa.sql
src/test/java
└── br/gov/seplag/musicapi
    ├── ActuatorHealthTests.java
    └── api/v1
        ├── AutenticacaoControllerTests.java
        ├── AlbumControllerTests.java
        └── ArtistaControllerTests.java
```

