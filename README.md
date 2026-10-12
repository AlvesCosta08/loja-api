# 🛒 Loja API

> API REST de e-commerce construída com **Java 21** e **Spring Boot 3.4**, seguindo **TDD estrito** (Red → Green → Refactor). Cobre autenticação JWT, catálogo de produtos, carrinho persistente, checkout com snapshot de preços, cupons, frete e pagamentos (Pix).

<p align="left">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.4" />
  <img src="https://img.shields.io/badge/Maven-3.10-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/H2-tests-005571?style=for-the-badge&logo=databricks&logoColor=white" alt="H2" />
  <img src="https://img.shields.io/badge/Flyway-migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/JWT-HS256-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/Tests-80_passing-2ea44f?style=for-the-badge&logo=junit5&logoColor=white" alt="80 testes" />
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

O objetivo é servir como **referência de arquitetura** para projetos Spring Boot reais: sem mágica, sem atalhos, com decisões justificadas.

---

## ✨ Funcionalidades

- ✅ Cadastro e login de clientes com **JWT (HS256)**
- ✅ Autorização por **roles** (`CUSTOMER`, `ADMIN`) com `@PreAuthorize`
- ✅ CRUD de **produtos** com paginação, filtro por categoria e busca por nome
- ✅ **Carrinho de compras** persistente por usuário (com validação de estoque)
- ✅ **Checkout** com decremento de estoque e snapshot de preços
- ✅ **Cupons** percentual/fixo com validade, limite de usos e valor mínimo
- ✅ **Cálculo de frete** por região (prefixo do CEP) e frete grátis
- ✅ **Pagamento Pix** com payload copia-e-cola + QR Code base64
- ✅ **Tratamento de erros** centralizado (`@RestControllerAdvice`)
- ✅ **80 testes automatizados** (JUnit 5 + MockMvc + H2)
- 🔜 Pagamento por cartão (Stripe) — em desenvolvimento
- 🔜 Painel administrativo, cache Redis e dashboard de métricas

---

## 🏗️ Arquitetura

```
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  Cliente         │ ───► │  API Spring Boot │ ───► │  PostgreSQL      │
│  (Web / Mobile)  │ HTTP │  (Java 21)       │ JDBC │  + Flyway        │
└──────────────────┘      └────────┬─────────┘      └──────────────────┘
                                   │
                                   ├──► JWT (HS256)
                                   ├──► Gateway Pix (mock)
                                   └──► (futuro) Stripe · Redis
```

**Fluxo de uma compra:**

```
/cart/items ──► /orders ──► /payments ──► /payments/{id}/confirm
  (carrinho)   (checkout)   (gera Pix)      (ADMIN confirma)
                                  ↓
                          Pedido vira PAID
```

---

## 🛠️ Stack

| Camada | Tecnologia |
|--------|------------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.4 (Web, Security, Data JPA, Validation, Actuator) |
| **Autenticação** | JWT (JJWT 0.12.6) + BCrypt |
| **Banco (produção)** | PostgreSQL 16 |
| **Banco (testes)** | H2 em memória (`MODE=PostgreSQL`) |
| **Migrações** | Flyway 10 |
| **Boilerplate** | Lombok |
| **Build** | Maven (com Wrapper) |
| **Testes** | JUnit 5 + MockMvc + AssertJ |
| **Encoding de erros** | `@RestControllerAdvice` + `ProblemDetail`-like JSON |

---

## 🚀 Como rodar

### Pré-requisitos

- **JDK 21** (`java -version` deve mostrar 21.x)
- **Maven** (ou use o Wrapper `./mvnw`)
- **PostgreSQL 16** (só para rodar em produção; testes usam H2)
- **Git**

### 1. Clonar o repositório

```bash
git clone https://github.com/seuusuario/loja-api.git
cd loja-api
```

### 2. Configurar o PostgreSQL (produção)

```bash
createdb loja
# opcional: exportar credenciais
export DB_USER=postgres
export DB_PASS=postgres
export JWT_SECRET=$(openssl rand -base64 48)
```

O schema é criado automaticamente pelas migrações Flyway em `src/main/resources/db/migration/`.

### 3. Rodar a API

```bash
./mvnw spring-boot:run
```

A aplicação sobe em **http://localhost:8080**.

### 4. Rodar os testes

```bash
./mvnw test            # 80 testes (JUnit + MockMvc + H2)
./mvnw clean test      # limpa + roda tudo (recomendado após mudanças de schema)
```

---

## ⚙️ Variáveis de ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_USER` | Usuário do PostgreSQL | `postgres` |
| `DB_PASS` | Senha do PostgreSQL | `postgres` |
| `JWT_SECRET` | Chave HMAC do JWT (**≥ 32 caracteres**) | valor de desenvolvimento |
| `JWT_EXPIRATION_MS` | Expiração do JWT em milissegundos | `3600000` (1h) |

---

## 📚 Endpoints principais

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
| `GET`  | `/products/{id}` | Detalhes do produto | público |
| `POST` | `/products` | Cria produto | ADMIN |
| `PUT`  | `/products/{id}` | Atualiza produto | ADMIN |
| `DELETE` | `/products/{id}` | Remove produto | ADMIN |

### Carrinho

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST`   | `/cart/items` | Adiciona/soma item |
| `GET`    | `/cart` | Carrinho atual + total |
| `PUT`    | `/cart/items/{id}` | Atualiza quantidade |
| `DELETE` | `/cart/items/{id}` | Remove item |
| `DELETE` | `/cart` | Limpa carrinho |

### Pedidos, cupons, frete e pagamento

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/orders?couponCode=&cep=` | Checkout | usuário |
| `GET`  | `/orders` | Lista pedidos do usuário | usuário |
| `GET`  | `/orders/{id}` | Detalhe (dono ou ADMIN) | usuário |
| `PATCH` | `/orders/{id}/status` | Altera status | ADMIN |
| `POST` | `/coupons` | Cria cupom | ADMIN |
| `GET`  | `/coupons/{code}` | Consulta cupom | público |
| `POST` | `/shipping/quote` | Cotações de frete | público |
| `POST` | `/payments?orderId=` | Gera Pix | dono |
| `GET`  | `/payments/{id}` | Consulta pagamento | dono |
| `POST` | `/payments/{id}/confirm` | Confirma pagamento | ADMIN |
| `GET`  | `/actuator/health` | Health check | público |

### Exemplo — fluxo completo de compra

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

## 🧪 Testes

A suíte cobre **80 cenários** distribuídos por contexto de negócio:

| Suite | Testes | Cobertura |
|-------|-------:|-----------|
| `AuthControllerTest` | 6 | Cadastro, login, e-mail duplicado, senha curta |
| `UserControllerTest` | 3 | Autenticação, `/users/me` |
| `ProductControllerTest` | 14 | CRUD completo + filtros + paginação + roles |
| `CartControllerTest` | 14 | Adicionar/atualizar/remover + estoque + carrinho alheio |
| `OrderControllerTest` | 17 | Checkout, snapshot de preços, frete, status |
| `CouponControllerTest` | 12 | Criação, expiração, limite de uso, valor mínimo |
| `ShippingControllerTest` | 6 | Cotações por região + frete grátis + CEP inválido |
| `PaymentControllerTest` | 8 | Pix, confirmação, idempotência, autorização |
| **Total** | **80** | ✅ todos passando |

```bash
# Roda a suíte inteira
./mvnw clean test

# Roda uma suíte específica
./mvnw test -Dtest=OrderControllerTest

# Roda um teste específico
./mvnw test -Dtest=OrderControllerTest#deveAplicarFreteGratisAcimaDe200
```

**Como funciona o TDD aqui:** cada fase começou com um teste que **falhava** (`🔴 RED`), seguido da implementação mínima que o fazia passar (`🟢 GREEN`) e, por fim, refatoração (`🔵 REFACTOR`). O histórico está documentado no `Resumo.md`.

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
    │   │   ├── config/                      # SecurityConfig
    │   │   ├── domain/                      # Entidades JPA + enums
    │   │   ├── dto/                         # Records de request/response
    │   │   ├── exception/                   # Exceções + handler global
    │   │   ├── repository/                  # Interfaces Spring Data
    │   │   ├── security/                    # JWT, filtro, entry point
    │   │   ├── service/                     # Regras de negócio
    │   │   └── controller/                  # Endpoints REST
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/                # V1..V8 Flyway
    └── test/
        ├── java/com/alvescosta/loja/        # 8 suítes JUnit 5
        └── resources/application-test.yml   # H2 em memória
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
| **11** | **Pagamento cartão / Stripe** | 🚧 em desenvolvimento |
| 12 | Painel administrativo | ⏳ |
| 13 | Cache Redis no catálogo | ⏳ |
| 14 | Dashboard de métricas | ⏳ |

**Progresso:** 11/14 fases (79%) · 80 testes verdes

---

## 🧠 Decisões técnicas

| Decisão | Por quê |
|---------|---------|
| **`BigDecimal`** para dinheiro | Evita erro de arredondamento de `double` |
| **JWT stateless** | API escala horizontalmente sem sessão |
| **Flyway** em produção, **H2** em testes | Schema real auditável + testes rápidos |
| **Snapshot de preços** em `OrderItem` | Se o produto mudar, o pedido mantém o valor histórico |
| **`@OnDelete(CASCADE)`** explícito | FKs consistentes entre H2 e PostgreSQL |
| **`record`** para DTOs | Imutabilidade e menos boilerplate |
| **`@EnableMethodSecurity`** | Sem isso, `@PreAuthorize` é ignorado silenciosamente |
| **`username = user.getId()`** no `UserDetailsServiceImpl` | O subject do JWT é o ID do usuário |
| **`total = subtotal − discount + shippingCost`** | Fórmula canônica, centralizada em `OrderService` |

Mais decisões e lições estão registradas em `Resumo.md`.

---

## 🔒 Segurança

- Senhas com **BCrypt** (hash + salt)
- Tokens **JWT HS256** com expiração configurável
- **CSRF desabilitado** (API stateless — correto para JWT)
- **`RestAuthenticationEntryPoint`** customizado → 401 em vez de 403 default do Spring
- Autorização por **`@PreAuthorize("hasRole('ADMIN')")`** em endpoints sensíveis
- **Validação de entrada** via Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Positive`)
- **Dados sensíveis** apenas em variáveis de ambiente

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

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/seuperfil)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com/seuusuario)
[![Email](https://img.shields.io/badge/Email-D14836?style=flat&logo=gmail&logoColor=white)](mailto:seuemail@exemplo.com)

---

<div align="center">

Se este projeto te ajudou, considere dar uma ⭐!

</div># loja-api
