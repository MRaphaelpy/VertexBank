# VertexBank

Projeto de estudo de arquitetura de microsserviços desenvolvido com Spring Boot. O objetivo é simular o backend de um banco digital, cobrindo os principais conceitos exigidos no mercado: autenticação com JWT, comunicação entre serviços, mensageria com Kafka, observabilidade e resiliência.

---

## Arquitetura

```
                 API Gateway
                      |
        .-------------|-------------.
        |             |             |
   Auth Service   User Service   Account Service
                                      |
                           Transaction Service
                                      |
                       .-------------|-------------.
                       |                           |
               Notification Service           Card Service
```

Cada serviço possui seu próprio banco de dados PostgreSQL, seguindo o princípio de isolamento de dados entre microsserviços.

---

## Serviços

### Auth Service `(porta 8081)`
Responsável por toda a camada de autenticação e autorização da plataforma.

- Cadastro e login de usuários
- Emissão de tokens JWT com claims de `userId` e `roles`
- Refresh token com expiração de 7 dias
- Logout com invalidação do refresh token
- Endpoint interno `GET /auth/validate` consumido pelo API Gateway
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Banco: `auth_db`

### User Service `(porta 8082)`
Responsável pelos dados pessoais dos usuários.

- Criação e atualização de perfil (nome, CPF, data de nascimento, endereço e telefones)
- Autenticação stateless via JWT — o `userId` é extraído do token sem consultar o Auth Service
- Banco: `user_db`

### Account Service *(planejado)*
Responsável pelas contas correntes.

- Criação de conta, saldo e bloqueio
- Comunicação com o User Service via OpenFeign
- Banco: `account_db`

### Transaction Service *(planejado)*
Responsável pelas movimentações financeiras.

- PIX, TED simulado, depósito, saque e extrato
- Publica eventos no Kafka ao concluir transferências
- Banco: `transaction_db`

### Notification Service *(planejado)*
Responsável pelo envio de notificações.

- Consome eventos do Kafka
- E-mail, push notification e SMS simulados

### Card Service *(planejado)*
Responsável pelo gerenciamento de cartões.

- Cartão físico e virtual, bloqueio e limite
- Banco: `card_db`

---

## Stack

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 4 | Base dos microsserviços |
| Spring Security + JWT | Autenticação e autorização |
| Spring Cloud Gateway | API Gateway |
| OpenFeign | Comunicação REST entre serviços |
| Apache Kafka | Mensageria assíncrona |
| PostgreSQL | Banco de dados de cada serviço |
| Docker Compose | Orquestração local |
| MapStruct | Mapeamento entre entidades e DTOs |
| Springdoc OpenAPI | Documentação dos endpoints |
| Prometheus + Grafana | Observabilidade e métricas |
| Loki | Agregação de logs |
| Resilience4j | Circuit breaker e retry |

---

## Como rodar

**Pré-requisitos:** Docker, Java 17 e Maven instalados.

```bash
# Em cada serviço, suba o banco e depois a aplicação
docker compose up -d
./mvnw spring-boot:run
```

### Auth Service `(porta 8081)`

```
http://localhost:8081/swagger-ui/index.html
```

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/auth_db` |
| `DB_USER` | `postgres` |
| `DB_PASS` | `postgres` |
| `JWT_SECRET` | *(chave de desenvolvimento)* |
| `JWT_EXPIRATION` | `3600000` (1 hora) |
| `JWT_REFRESH_EXPIRATION` | `604800000` (7 dias) |

### User Service `(porta 8082)`

```
http://localhost:8082/swagger-ui/index.html
```

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/user_db` |
| `DB_USER` | `postgres` |
| `DB_PASS` | `postgres` |
| `JWT_SECRET` | *(mesma do Auth Service)* |

---

## Endpoints

### Auth Service

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/signup` | Cadastro de novo usuário |
| `POST` | `/auth/login` | Login, retorna JWT e refresh token |
| `POST` | `/auth/refresh` | Renova o JWT a partir do refresh token |
| `POST` | `/auth/logout` | Invalida o refresh token |
| `GET` | `/auth/validate` | Valida o JWT (uso interno do Gateway) |

### User Service

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/users` | Cria o perfil do usuário autenticado |
| `GET` | `/users/me` | Retorna o perfil do usuário autenticado |
| `PUT` | `/users/me` | Atualiza o perfil do usuário autenticado |

---

## Testando com Postman

Uma collection completa está disponível na raiz do projeto:

```
VertexBank.postman_collection.json
```

Ela captura o token automaticamente após o login e já injeta o `Authorization` header em todas as requisições do User Service.

---

## Ordem de desenvolvimento

1. Auth Service ✓
2. User Service ✓
3. API Gateway
4. Account Service
5. Transaction Service
6. Kafka + Notification Service
7. Card Service
8. Observabilidade (Prometheus, Grafana, Loki)
9. Resilience4j (Circuit Breaker, Retry)