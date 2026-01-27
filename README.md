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
    │       ├── AlbumController.java
    │       ├── ArtistaController.java
    │       └── dto
    │           ├── AlbumRequest.java
    │           ├── AlbumResponse.java
    │           ├── ArtistaRequest.java
    │           └── ArtistaResponse.java
    ├── domain
    │   ├── Album.java
    │   └── Artista.java
    ├── repository
    │   ├── AlbumRepository.java
    │   └── ArtistaRepository.java
    ├── service
    │   ├── AlbumService.java
    │   └── ArtistaService.java
    └── MusicApiApplication.java
src/main/resources
├── application.yml
├── application-local.yml
├── application-postgres.yml
└── db/migration
    └── V1__criar_schema_inicial.sql
src/test/java
└── br/gov/seplag/musicapi
    ├── ActuatorHealthTests.java
    └── api/v1
        ├── AlbumControllerTests.java
        └── ArtistaControllerTests.java
```

