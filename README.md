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

### 2. Validar rapidamente

- Ping:
  - `GET http://localhost:8080/v1/ping`
- Health checks (Actuator):
  - `GET http://localhost:8080/actuator/health`
  - `GET http://localhost:8080/actuator/health/liveness`
  - `GET http://localhost:8080/actuator/health/readiness`

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
    └── MusicApiApplication.java
```

