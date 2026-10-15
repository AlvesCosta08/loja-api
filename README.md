# 🛒 Loja API

> API REST de e-commerce construída com **Java 21** e **Spring Boot 3.4**, seguindo **TDD estrito** (Red → Green → Refactor). Cobre autenticação JWT, catálogo com cache Redis, carrinho persistente, checkout com snapshot de preços, cupons, frete, pagamentos Pix e cartão (Stripe mock), painel administrativo e dashboard de métricas.

<p align="left">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.4" />
  <img src="https://img.shields.io/badge/Maven-3.10-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/H2-tests-005571?style=for-the-badge&logo=databricks&logoColor=white" alt="H2" />
  <img src="https://img.shields.io/badge/Redis-cache-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
  <img src="https://img.shields.io/badge/Flyway-migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/JWT-HS256-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/Prometheus-metrics-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" alt="Prometheus" />
  <img src="https://img.shields.io/badge/Tests-116_passing-2ea44f?style=for-the-badge&logo=junit5&logoColor=white" alt="116 testes" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="MIT" />
</p>

---

## 📖 Sobre o projeto

Esta API implementa o **back-end completo de uma loja virtual** — pronto para ser consumido por qualquer frontend (web, mobile ou SPA). Foi construída com **foco em qualidade**:

- **TDD estrito** — todo código de produção nasceu de um teste que falhou primeiro.
- **Modelagem correta de dinheiro** (`BigDecimal`, nunca `double`).
- **Migrações versionadas** com Flyway (reprodutível em qualquer ambiente).
- **Domínio rico** — snapshot de preços em pedidos, cascade explícito, invariantes no banco.
- **Segurança** — JWT stateless, BCrypt, autorização por role.
- **Performance** — cache Redis no catálogo com invalidação em escrita.
- **Observabilidade** — Actuator + Micrometer + Prometheus com métricas customizadas.

O objetivo é servir como **referência de arquitetura** para projetos Spring Boot reais: sem mágica, sem atalhos, com decisões justificadas.

---

## ✨ Funcionalidades

- ✅ Cadastro e login de clientes com **JWT (HS256)**
- ✅ Autorização por **roles** (`CUSTOMER`, `ADMIN`) com `@PreAuthorize`
- ✅ CRUD de **produtos** com paginação, filtro por categoria e busca por nome
- ✅ **Carrinho de compras** persistente por usuário (com validação de estoque)
- ✅ **Checkout** com decremento de estoque e snapshot de preços
- ✅ **Cupons** percentual/fixo com validade, limite de usos e valor mínimo
- ✅ **Cálculo de frete** por região (prefixo do CEP) e frete grátis acima de R$ 200
- ✅ **Pagamento Pix** com payload copia-e-cola + QR Code base64
- ✅ **Pagamento com cartão** via gateway (implementação mock para Stripe)
- ✅ **Webhook de gateway** com idempotência
- ✅ **Painel administrativo** — dashboard, pedidos, usuários, produtos com estoque baixo, restock
- ✅ **Cache Redis** no catálogo (TTL 5 min) com invalidação em `create`/`update`/`delete`
- ✅ **Dashboard de métricas** — Micrometer + Prometheus (contadores customizados)
- ✅ **Tratamento de erros** centralizado (`@RestControllerAdvice`)
- ✅ **116 testes automatizados** (JUnit 5 + MockMvc + H2)

---

## 🏗️ Arquitetura

```
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  Cliente         │ ───► │  API Spring Boot │ ───► │  PostgreSQL      │
│  (Web / Mobile)  │ HTTP │  (Java 21)       │ JDBC │  + Flyway        │
└──────────────────┘      └────────┬─────────┘      └──────────────────┘
                                   │
                                   ├──► JWT (HS256)
                                   ├──► Redis (cache do catálogo)
                                   ├──► Gateway Pix (mock)
                                   ├──► Gateway Cartão (Stripe mock)
                                   └──► Prometheus (métricas)
```

**Fluxo de uma compra:**

```
/cart/items ──► /orders ──► /payments ──► /payments/{id}/confirm
  (carrinho)   (checkout)   (Pix ou cartão)  (ADMIN confirma Pix)
                                  ↓
                          Pedido vira PAID
```

**Fluxo de autenticação (JWT):**

```
POST /auth/register ──► User criado (BCrypt)
POST /auth/login    ──► TokenResponse (access_token, token_type, expires_in)
                          │
                          ▼
        ┌────────────────────────────────────┐
        │ Authorization: Bearer <access_token>│
        └──────────────────┬─────────────────┘
                           ▼
                    JwtAuthFilter
                           │
                           ▼
                  SecurityContext populado
                           │
                           ▼
                    @PreAuthorize decide
```

**Fluxo de cache no catálogo (Redis):**

```
GET /products/{id} ──► @Cacheable("products") ──► cache hit? ──► devolve
                                        │
                                        └─► cache miss ──► DB ──► grava no cache

POST/PUT/DELETE /products ──► @CacheEvict("products", "productLists")
                                     └─► invalida ambos os caches
```

---

## 🛠️ Stack

| Camada | Tecnologia |
|--------|------------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.4 (Web, Security, Data JPA, Validation, Cache, Actuator) |
| **Autenticação** | JWT (JJWT 0.12.6) + BCrypt |
| **Banco (produção)** | PostgreSQL 16 |
| **Banco (testes)** | H2 em memória (`MODE=PostgreSQL`) |
| **Cache (produção)** | Redis (TTL 5 min) |
| **Cache (testes)** | `ConcurrentMapCacheManager` (`simple`) |
| **Métricas** | Micrometer + Prometheus |
| **Migrações** | Flyway (V1..V9) |
| **Boilerplate** | Lombok 1.18.36 |
| **Build** | Maven (com Wrapper) |
| **Testes** | JUnit 5 + MockMvc + AssertJ |
| **Encoding de erros** | `@RestControllerAdvice` + JSON `{status, error, message}` |

---

## 🚀 Como rodar

### Pré-requisitos

- **JDK 21** (`java -version` deve mostrar 21.x)
- **Maven** (ou use o Wrapper `./mvnw`)
- **PostgreSQL 16** (para produção)
- **Redis 7** (para produção — cache)
- **Git**

### 1. Clonar o repositório

```bash
git clone https://github.com/AlvesCosta08/loja-api.git
cd loja-api
```

### 2. Configurar o PostgreSQL (produção)

```bash
createdb loja
# opcional: exportar credenciais
export DB_USER=postgres
export DB_PASS=postgres
export JWT_SECRET=$(openssl rand -base64 48)
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

O schema é criado automaticamente pelas migrações Flyway em `src/main/resources/db/migration/` (V1 a V9).

### 3. Rodar a API

```bash
./mvnw spring-boot:run
```

A aplicação sobe em **http://localhost:8080**.

### 4. Rodar os testes

```bash
./mvnw test            # 116 testes (JUnit + MockMvc + H2)
./mvnw clean test      # limpa + roda tudo (recomendado após mudanças de schema)
```

---

## ⚙️ Variáveis de ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_USER` | Usuário do PostgreSQL | `postgres` |
| `DB_PASS` | Senha do PostgreSQL | `postgres` |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `JWT_SECRET` | Chave HMAC do JWT (**≥ 32 caracteres**) | valor de desenvolvimento |
| `JWT_EXPIRATION_MS` | Expiração do JWT em milissegundos | `3600000` (1h) |

> ⚠️ O segredo de desenvolvimento (`change-me-in-production-with-at-least-32-chars`) é apenas para uso local. **Em produção, sempre defina `JWT_SECRET`.**

---

## 📚 Endpoints

### Autenticação

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/auth/register` | Cadastro de cliente | público |
| `POST` | `/auth/login` | Login → `access_token` | público |
| `GET`  | `/users/me` | Dados do usuário logado | usuário |

### Catálogo

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `GET`  | `/products` | Lista paginada (`?category=&search=&page=&size=`) | público |
| `GET`  | `/products/{id}` | Detalhes do produto (cached) | público |
| `POST` | `/products` | Cria produto | ADMIN |
| `PUT`  | `/products/{id}` | Atualiza produto | ADMIN |
| `DELETE` | `/products/{id}` | Remove produto | ADMIN |

### Carrinho

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST`   | `/cart/items` | Adiciona/soma item | usuário |
| `GET`    | `/cart` | Carrinho atual + total | usuário |
| `PUT`    | `/cart/items/{id}` | Atualiza quantidade | usuário |
| `DELETE` | `/cart/items/{id}` | Remove item | usuário |
| `DELETE` | `/cart` | Limpa carrinho | usuário |

### Pedidos, cupons, frete

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/orders?couponCode=&cep=` | Checkout | usuário |
| `GET`  | `/orders` | Lista pedidos do usuário | usuário |
| `GET`  | `/orders/{id}` | Detalhe (dono ou ADMIN) | usuário |
| `PATCH` | `/orders/{id}/status` | Altera status | ADMIN |
| `POST` | `/coupons` | Cria cupom | ADMIN |
| `GET`  | `/coupons/{code}` | Consulta cupom | público |
| `POST` | `/shipping/quote` | Cotações de frete | público |

### Pagamentos

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/payments?orderId=` | Gera Pix | dono |
| `GET`  | `/payments/{id}` | Consulta pagamento | dono |
| `POST` | `/payments/{id}/confirm` | Confirma pagamento Pix | ADMIN |
| `POST` | `/payments/card?orderId=` | Gera cobrança no cartão (Stripe mock) | dono |
| `POST` | `/webhooks/stripe` | Webhook do gateway (idempotente) | público |

### Painel administrativo

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `GET`  | `/admin/dashboard` | Totais (pedidos, receita, ticket médio) | ADMIN |
| `GET`  | `/admin/orders` | Lista todos os pedidos | ADMIN |
| `GET`  | `/admin/users` | Lista usuários com métricas | ADMIN |
| `PATCH` | `/admin/users/{id}/role` | Altera role do usuário | ADMIN |
| `GET`  | `/admin/products/low-stock` | Produtos com estoque baixo | ADMIN |
| `POST` | `/admin/products/{id}/restock` | Repõe estoque | ADMIN |

### Actuator / Observabilidade

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `GET`  | `/actuator/health` | Health check | público |
| `GET`  | `/actuator/info` | Informações da app | público |
| `GET`  | `/actuator/metrics` | Métricas | autenticado |
| `GET`  | `/actuator/prometheus` | Scrape Prometheus | autenticado |

### Exemplo — fluxo completo de compra (Pix)

```bash
# 1) Cadastro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"cli@exemplo.com","name":"Cliente","password":"senha12345"}'

# 2) Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cli@exemplo.com","password":"senha12345"}' | jq -r .access_token)

# 3) Adiciona produto ao carrinho
curl -X POST http://localhost:8080/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 4) Checkout com frete para SP
curl -X POST "http://localhost:8080/orders?cep=01310100" \
  -H "Authorization: Bearer $TOKEN"

# 5) Gera Pix
curl -X POST "http://localhost:8080/payments?orderId=1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📐 Contrato da API — formatos principais

### Erro (formato único)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente para o produto X"
}
```

Mapeamento centralizado em `GlobalExceptionHandler`:

| Exceção | HTTP | `error` |
|---------|:----:|---------|
| `BusinessException` | 400 | `Bad Request` |
| `MethodArgumentNotValidException` | 400 | `Validation Failed` |
| `UnauthorizedException` | 401 | `Unauthorized` |
| `NotFoundException` | 404 | `Not Found` |

### Token

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Carrinho

```json
{
  "items": [
    {
      "id": 1, "productId": 10, "productName": "Camiseta",
      "imageUrl": "https://...", "unitPrice": 79.90,
      "quantity": 2, "subtotal": 159.80
    }
  ],
  "total": 159.80,
  "itemCount": 1
}
```

### Pedido

```json
{
  "id": 1, "userId": 1, "status": "PENDING",
  "total": 179.80, "discount": 0, "shippingCost": 20.00,
  "couponCode": null, "createdAt": "2026-01-15T10:00:00Z",
  "items": [
    { "id": 1, "productId": 10, "productName": "Camiseta",
      "unitPrice": 79.90, "quantity": 2, "subtotal": 159.80 }
  ]
}
```

### Pagamento

```json
{
  "id": 1, "orderId": 1,
  "method": "PIX", "status": "PENDING", "amount": 179.80,
  "pixCopyPaste": "00020126...",
  "pixQrCodeBase64": "iVBORw0KG...",
  "gatewayRef": null, "clientSecret": null,
  "createdAt": "...", "expiresAt": "...", "confirmedAt": null
}
```

Para cartão, `method = "CREDIT_CARD"` e os campos `gatewayRef` e `clientSecret` são preenchidos (mock).

---

## 🧪 Testes

A suíte cobre **116 cenários** distribuídos em 12 classes:

| Suite | Testes | Cobertura |
|-------|-------:|-----------|
| `AuthControllerTest` | 6 | Cadastro, login, e-mail duplicado, senha curta |
| `UserControllerTest` | 3 | Autenticação, `/users/me` |
| `ProductControllerTest` | 14 | CRUD completo + filtros + paginação + roles |
| `ProductCacheTest` | 8 | `@Cacheable`, `@CacheEvict`, invalidação |
| `CartControllerTest` | 14 | Adicionar/atualizar/remover + estoque + carrinho alheio |
| `OrderControllerTest` | 17 | Checkout, snapshot de preços, frete, status |
| `CouponControllerTest` | 12 | Criação, expiração, limite de uso, valor mínimo |
| `ShippingControllerTest` | 6 | Cotações por região + frete grátis + CEP inválido |
| `PaymentControllerTest` | 8 | Pix, confirmação, idempotência, autorização |
| `CardPaymentControllerTest` | 9 | Cartão, webhook, idempotência |
| `AdminControllerTest` | 12 | Dashboard, users, roles, low-stock, restock |
| `MetricsTest` | 7 | Contadores customizados (`loja.orders.created`, `loja.payments.confirmed`) |
| **Total** | **116** | ✅ todos passando |

```bash
# Roda a suíte inteira
./mvnw clean test

# Roda uma suíte específica
./mvnw test -Dtest=OrderControllerTest

# Roda um teste específico
./mvnw test -Dtest=OrderControllerTest#deveAplicarFreteGratisAcimaDe200
```

**Como funciona o TDD aqui:** cada fase começou com um teste que **falhava** (`🔴 RED`), seguido da implementação mínima que o fazia passar (`🟢 GREEN`) e, por fim, refatoração (`🔵 REFACTOR`).

---

## 📁 Estrutura do projeto

```
loja-api/
├── pom.xml
├── README.md
├── Resumo.md                                # Documento de contexto entre sessões
└── src/
    ├── main/
    │   ├── java/com/alvescosta/loja/
    │   │   ├── LojaApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── CacheConfig.java
    │   │   │   └── PrometheusMetricsConfig.java
    │   │   ├── domain/                      # 11 entidades + enums
    │   │   │   ├── User.java  Product.java  CartItem.java
    │   │   │   ├── Order.java  OrderItem.java  Coupon.java  Payment.java
    │   │   │   └── Role.java  OrderStatus.java  PaymentStatus.java
    │   │   │       PaymentMethod.java  DiscountType.java  ShippingType.java
    │   │   ├── dto/                         # 22 records
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── BusinessException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   └── NotFoundException.java
    │   │   ├── repository/                  # 6 interfaces Spring Data
    │   │   ├── security/
    │   │   │   ├── JwtService.java
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   ├── UserDetailsServiceImpl.java
    │   │   │   └── RestAuthenticationEntryPoint.java
    │   │   ├── service/
    │   │   │   ├── UserService  ProductService  CartService  CartCalculator
    │   │   │   ├── OrderService  CouponService  ShippingService
    │   │   │   ├── PaymentService  PixGenerator
    │   │   │   ├── PaymentGateway  StripePaymentGateway
    │   │   │   ├── AdminService  MetricsService
    │   │   └── controller/                  # 10 controllers
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/                # V1..V9 Flyway
    └── test/
        ├── java/com/alvescosta/loja/        # 12 suítes + 1 config
        └── resources/application-test.yml   # H2 em memória + cache simple
```

---

## 🗺️ Roadmap

| # | Fase | Status |
|---|------|:------:|
| 0 | Setup do projeto (Maven, Spring Boot, H2) | ✅ |
| 1 | Cadastro de usuário (`POST /auth/register`) | ✅ |
| 2 | Login + JWT (`POST /auth/login`) | ✅ |
| 3 | Usuário logado (`GET /users/me`) | ✅ |
| 4 | CRUD de produtos (`/products`) | ✅ |
| 5 | Categorias + busca | ✅ |
| 6 | Carrinho (`/cart`) | ✅ |
| 7 | Checkout / Pedidos (`/orders`) | ✅ |
| 8 | Cupons de desconto (`/coupons`) | ✅ |
| 9 | Cálculo de frete (`/shipping`) | ✅ |
| 10 | Pagamento Pix (`/payments`) | ✅ |
| 11 | Pagamento cartão / Stripe (mock) | ✅ |
| 12 | Painel administrativo (`/admin`) | ✅ |
| 13 | Cache Redis no catálogo | ✅ |
| 14 | Dashboard de métricas (Micrometer + Prometheus) | ✅ |

**Progresso:** 14/14 fases · 116 testes verdes · 9 migrations

---

## 🧠 Decisões técnicas

| Decisão | Por quê |
|---------|---------|
| **`BigDecimal`** para dinheiro | Evita erro de arredondamento de `double` |
| **JWT stateless** | API escala horizontalmente sem sessão |
| **Flyway** em produção, **H2 create-drop** em testes | Schema real auditável + testes rápidos |
| **Snapshot de preços** em `OrderItem` | Se o produto mudar, o pedido mantém o valor histórico |
| **`@OnDelete(CASCADE)`** explícito | FKs consistentes entre H2 e PostgreSQL |
| **`record`** para DTOs | Imutabilidade e menos boilerplate |
| **`@EnableMethodSecurity`** | Sem isso, `@PreAuthorize` é ignorado silenciosamente |
| **`username = user.getId()`** no `UserDetailsServiceImpl` | O subject do JWT é o ID do usuário |
| **`total = subtotal − discount + shippingCost`** | Fórmula canônica, centralizada em `OrderService` |
| **Cache Redis com TTL 5 min** | Reduz carga em consultas repetidas do catálogo |
| **Dois caches separados** (`products`, `productLists`) | Invalidação cirúrgica sem derrubar listas |
| **`PrometheusMetricsConfig` explícito** | Contorna bug de auto-config do Spring Boot 3.4 com injeção precoce do `MeterRegistry` |
| **Stripe mockado** (`StripePaymentGateway`) | Testa o fluxo sem depender do SDK/API externo |
| **Coluna `discount_value`** (não `value`) | `value` é palavra reservada em SQL |
| **`RestAuthenticationEntryPoint`** | Retorna 401 em vez do 403 default do Spring |

---

## 🔒 Segurança

- Senhas com **BCrypt** (hash + salt)
- Tokens **JWT HS256** com expiração configurável
- **CSRF desabilitado** (API stateless — correto para JWT)
- **`RestAuthenticationEntryPoint`** customizado → 401 em vez de 403 default
- Autorização por **`@PreAuthorize("hasRole('ADMIN')")`** em endpoints sensíveis
- **Validação de entrada** via Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Positive`)
- **Dados sensíveis** apenas em variáveis de ambiente

---

## ⚠️ Pendências conhecidas

- **CORS ainda não configurado.** O `SecurityConfig` não chama `.cors(...)` e não há `CorsConfig`. Isso **bloqueia o desenvolvimento do frontend** — o navegador vai rejeitar requisições de `http://localhost:5173`.
- **`JWT_EXPIRATION_MS`** está hardcoded em `application.yml` (não lê env var, apesar do README antigo sugerir isso).
- **Stripe é mock.** Para produção com Stripe real, é preciso adicionar `com.stripe:stripe-java` ao `pom.xml` e configurar `stripe.api-key` + `stripe.webhook-secret`.
- **`GlobalExceptionHandler`** não cobre `AccessDeniedException` (403), `Exception` genérica (500), `HttpRequestMethodNotSupportedException` (405) nem `DataIntegrityViolationException` (409).

---

## 🤝 Contribuindo

1. Faça um **fork** do projeto
2. Crie uma branch: `git checkout -b feature/minha-feature`
3. Escreva **primeiro o teste** (TDD)
4. Implemente até o teste passar
5. Rode `./mvnw clean test` (tudo verde antes de commitar)
6. Commit no padrão **Conventional Commits**: `git commit -m 'feat: adiciona X'`
7. Push e abra um **Pull Request**

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja [LICENSE](./LICENSE) para mais detalhes.

---

## 👤 Autor

**Alves Costa**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/SEU-PERFIL-AQUI)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com/AlvesCosta08)
[![Email](https://img.shields.io/badge/Email-D14836?style=flat&logo=gmail&logoColor=white)](mailto:SEU-EMAIL-AQUI)

---

<div align="center">

Se este projeto te ajudou, considere dar uma ⭐!

</div>