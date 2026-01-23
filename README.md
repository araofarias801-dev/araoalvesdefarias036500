# music-api

API REST para cadastro e consulta de artistas e álbuns.

## Requisitos

- Java 21
- Maven 3.6+

## Como executar

```bash
mvn spring-boot:run
```

A aplicação sobe por padrão em `http://localhost:8080`.

## Endpoints disponíveis (atual)

- `GET /v1/ping`
- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

## Como testar

```bash
mvn clean test
```

