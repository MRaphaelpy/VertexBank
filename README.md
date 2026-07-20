# VertexBank

Projeto de estudo de arquitetura de microsserviços desenvolvido com Spring Boot. O objetivo é simular o backend de um banco digital, cobrindo os principais conceitos exigidos no mercado: arquitetura limpa (SOLID, CQRS), autenticação com JWT, comunicação síncrona entre serviços via OpenFeign, API Gateway, mensageria com Kafka, observabilidade e resiliência.

---

## Arquitetura

```
                 API Gateway (8080)
                       |
        .--------------+--------------.
        |              |              |
   Auth Service   User Service   Account Service
     (8081)         (8082)         (8083)
                                      |
                           Transaction Service (planejado)
                                      |
                       .--------------+--------------.
                       |                             |
               Notification Service             Card Service
                   (planejado)                  (planejado)
```

Cada serviço possui seu próprio banco de dados PostgreSQL, seguindo o princípio de isolamento de dados entre microsserviços (Database-per-Service).

---

## Serviços

### API Gateway `(porta 8080)`
Porta de entrada única para o sistema.
- Roteamento inteligente para os microsserviços (`/auth/**`, `/users/**`, `/accounts/**`).
- Filtro de autenticação global interceptando requisições e validando tokens JWT diretamente com o Auth Service.

### Auth Service `(porta 8081)`
Responsável por toda a camada de autenticação e autorização da plataforma.
- Cadastro e login de usuários.
- Emissão de tokens JWT com claims de `userId` e `roles`.
- Refresh token com expiração de 7 dias e logout com invalidação.
- Endpoint interno `GET /auth/validate` consumido pelo API Gateway.
- Banco: `auth_db`

### User Service `(porta 8082)`
Responsável pelos dados pessoais dos usuários.
- Criação e atualização de perfil (nome, CPF, data de nascimento, endereço e telefones).
- Autenticação stateless via JWT.
- Disponibiliza endpoint interno `GET /users/{userId}` para outros microsserviços.
- Banco: `user_db`

### Account Service `(porta 8083)`
Responsável pelo motor de contas correntes (Ledger Core).
- **Arquitetura Avançada**: Implementa CQRS (Segregação de Responsabilidade de Comando e Consulta) e padrões SOLID (Single Responsibility, Dependency Inversion, Open/Closed Principle).
- Criação de conta bancária vinculada ao `userId` validando a existência do usuário via OpenFeign no User Service.
- Geração automatizada e abstrata de Agência e Conta.
- Gestão atômica de saldo (Depósitos, Saques, por enquanto) via Polimorfismo.
- Inativação e reativação de contas.
- Banco: `account_db`

### Transaction Service *(planejado)*
Responsável pelas movimentações financeiras.
- PIX, TED simulado, depósito, saque e extrato.
- Orquestrador de transações chamando o Account Service para mutações atômicas de saldo.
- Publica eventos no Kafka ao concluir transferências.
- Banco: `transaction_db`

---

## Stack

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 4 | Base dos microsserviços |
| Spring Security + JWT | Autenticação e autorização |
| Spring Cloud Gateway | API Gateway |
| Spring Cloud OpenFeign | Comunicação REST síncrona entre serviços |
| Apache Kafka | Mensageria assíncrona (planejado) |
| PostgreSQL | Banco de dados de cada serviço |
| Docker Compose | Orquestração local |
| MapStruct | Mapeamento entre entidades e DTOs |
| Springdoc OpenAPI | Documentação dos endpoints |
| Prometheus + Grafana | Observabilidade e métricas (planejado) |
| Loki | Agregação de logs (planejado) |
| Resilience4j | Circuit breaker e retry (planejado) |

---

## Como Rodar

**Pré-requisitos:** Banco de Dados PostgreSQL (rodando nas portas 5432 e 5433 conforme os `application.properties`), Docker (opcional), Java 17 e Maven.

Para rodar os serviços, navegue até a pasta de cada um e inicie com o Maven:

```bash
cd AuthService && ./mvnw spring-boot:run
cd UserService && ./mvnw spring-boot:run
cd AccountService && ./mvnw spring-boot:run
cd APIGateway && ./mvnw spring-boot:run
```

### Variáveis de Ambiente
Certifique-se de exportar ou configurar as variáveis requeridas, como a chave de assinatura JWT compartilhada (`JWT_SECRET`) e as variáveis de conexão com o banco de dados (`DB_URL`, `DB_USER`, `DB_PASS`), garantindo que a mesma secret JWT seja utilizada em todos os serviços.

---

## Endpoints Principais

### Auth Service
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/signup` | Cadastro de novo usuário e credenciais |
| `POST` | `/auth/login` | Login, retorna JWT e refresh token |
| `POST` | `/auth/refresh` | Renova o JWT a partir do refresh token |

### User Service
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/users` | Cria o perfil detalhado do usuário |
| `GET` | `/users/me` | Retorna o perfil do usuário autenticado |
| `GET` | `/users/{userId}` | (Interno) Retorna o perfil de um usuário por ID |

### Account Service
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/accounts` | Criação de conta (valida usuário no User Service) |
| `GET` | `/accounts/me` | Retorna dados da própria conta |
| `GET` | `/accounts/{id}/balance` | Consulta saldo atômico |
| `PUT` | `/accounts/{id}/balance` | (Interno) Executa Depósito ou Saque na conta |
| `PUT` | `/accounts/{id}/deactivate` | Desativa a conta bancária |

---

## Testando com Postman

Uma collection interativa e completa está disponível na raiz do projeto:

```
VertexBank.postman_collection.json
```

A collection conta com **scripts de automação robustos**. Ao fazer o login, o Token JWT é salvo automaticamente nas variáveis globais da coleção. Ao criar o perfil do usuário ou a conta, o `userId` e `accountId` também são capturados, permitindo testar o fluxo de ponta a ponta sem precisar copiar e colar IDs manualmente!

---

## Ordem de Desenvolvimento

1. Auth Service ✓
2. User Service ✓
3. API Gateway ✓
4. Account Service ✓ *(Refatorado para arquitetura Limpa e CQRS)*
5. Integrações OpenFeign ✓
6. Transaction Service
7. Kafka + Notification Service
8. Card Service
9. Observabilidade (Prometheus, Grafana, Loki)
10. Resilience4j (Circuit Breaker, Retry)
