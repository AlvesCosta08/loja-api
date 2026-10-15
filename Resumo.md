# Loja API — Contexto do Projeto

Documento de referência entre sessões de desenvolvimento.
Atualizado ao final de cada fase concluída.

---

## Sumário

1. [Informações gerais](#informações-gerais)
2. [Status geral](#status-geral)
3. [Objetivo do projeto](#objetivo-do-projeto)
4. [Roadmap — Backend](#roadmap--backend)
5. [Roadmap — Frontend](#roadmap--frontend)
6. [Fases concluídas — Backend](#fases-concluídas--backend)
7. [Fases concluídas — Frontend](#fases-concluídas--frontend)
8. [Pré-requisitos para iniciar o frontend](#pré-requisitos-para-iniciar-o-frontend)
9. [Estrutura prevista do projeto frontend](#estrutura-prevista-do-projeto-frontend)
10. [Contrato da API (referência para o frontend)](#contrato-da-api-referência-para-o-frontend)
11. [Decisões técnicas — Backend](#decisões-técnicas--backend)
12. [Decisões técnicas — Frontend](#decisões-técnicas--frontend)
13. [Pontos de atenção herdados do backend](#pontos-de-atenção-herdados-do-backend)
14. [Bugs encontrados e correções — Backend](#bugs-encontrados-e-correções--backend)
15. [Bugs encontrados e correções — Frontend](#bugs-encontrados-e-correções--frontend)
16. [Como executar — Backend](#como-executar--backend)
17. [Como executar — Frontend](#como-executar--frontend)
18. [Guia de implementação React — passo a passo](#guia-de-implementação-react--passo-a-passo)
19. [Possíveis extensões futuras](#possíveis-extensões-futuras)
20. [Como atualizar este documento](#como-atualizar-este-documento)

---

## Informações gerais

### Backend

| Campo | Valor |
|-------|-------|
| Projeto | loja-api |
| Descrição | API REST de e-commerce com autenticação, catálogo, carrinho, pedidos, cupons, frete, pagamentos Pix/cartão, admin e métricas |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.0 |
| Versão atual | **0.10.0** (⚠️ desatualizada — deveria ser 0.14.0) |
| Abordagem | TDD estrito (Red → Green → Refactor) |
| Autenticação | JWT HS256 (JJWT 0.12.6) |
| Banco — produção | PostgreSQL 16 |
| Banco — testes | H2 em memória (`MODE=PostgreSQL`) |
| Cache — produção | Redis (TTL 5 min) |
| Cache — testes | `ConcurrentMapCacheManager` (`simple`) |
| Métricas | Micrometer + Prometheus |
| Migrações | Flyway (V1..V9) |
| Build | Maven |
| Boilerplate | Lombok 1.18.36 |
| Pacote base | `com.alvescosta.loja` |
| Repositório backend | https://github.com/AlvesCosta08/loja-api |
| Última atualização backend | Fase 14 — Dashboard de métricas |

### Frontend

| Campo | Valor |
|-------|-------|
| Projeto | loja-web |
| Descrição | SPA de e-commerce que consome a `loja-api` |
| Framework | React 19 + Vite |
| Linguagem | TypeScript 5.6 |
| Estilização | Tailwind CSS 4 (plugin `@tailwindcss/vite`) |
| Estado | Zustand (global) + TanStack Query (server state) |
| HTTP | Axios + interceptors (JWT e erro) |
| Rotas | React Router 7 + wrappers `ProtectedRoute` / `AdminRoute` / `GuestRoute` |
| Formulários | React Hook Form + Zod |
| Testes | Vitest + Testing Library (unit) + Playwright (E2E) |
| Build | Vite |
| Deploy previsto | Vercel / Netlify |
| Repositório frontend | https://github.com/AlvesCosta08/loja-web *(a criar)* |
| Última atualização frontend | Setup inicial |

### Autor

| Campo | Valor |
|-------|-------|
| Autor | Alves Costa |
| GitHub | https://github.com/AlvesCosta08 |
| Timezone | America/Fortaleza |

---

## Status geral

### Backend

**Roadmap completo: 14 de 14 fases concluídas.**

- **116 testes** automatizados, todos verdes.
- **12 classes de teste** + 1 config (`PrometheusTestConfig`).
- **77 arquivos** de produção.
- **9 migrations** Flyway (V1 a V9).

### Frontend

**Roadmap: 0 de 11 fases concluídas.**

Projeto a iniciar. Stack: React 19 + Vite + Tailwind CSS 4.

⚠️ **Bloqueio atual:** CORS não configurado no backend.

---

## Objetivo do projeto

Construir um e-commerce full-stack cobrindo:

**Backend** (concluído)

- Autenticação e autorização com JWT e roles
- Catálogo de produtos com filtros, paginação e cache Redis
- Carrinho persistente
- Checkout com snapshot de preços
- Integração com gateways de pagamento (Pix + cartão Stripe mock)
- Cupons, frete e painel administrativo
- Webhook de gateway com idempotência
- Dashboard de métricas (Micrometer + Prometheus)

**Frontend** (a construir)

- Autenticação com JWT (login, cadastro, guards de rota)
- Vitrine de produtos com busca, filtros e paginação
- Página de detalhe com add ao carrinho
- Carrinho persistente com atualização de quantidade
- Checkout completo (endereço, cupom, frete, pagamento)
- Histórico de pedidos
- Pagamento Pix (QR Code) e cartão
- Painel admin (produtos, pedidos, usuários, dashboard)
- Design responsivo (mobile-first)

---

## Roadmap — Backend

| # | Fase | Status |
|---|------|--------|
| 0 | Setup do projeto | ✅ Concluído |
| 1 | Cadastro de usuário (`POST /auth/register`) | ✅ Concluído |
| 2 | Login + JWT (`POST /auth/login`) | ✅ Concluído |
| 3 | Usuário logado (`GET /users/me`) | ✅ Concluído |
| 4 | CRUD de produtos (`/products`) | ✅ Concluído |
| 5 | Categorias e busca | ✅ Concluído (absorvido pela Fase 4) |
| 6 | Carrinho (`/cart`) | ✅ Concluído |
| 7 | Checkout e pedidos (`/orders`) | ✅ Concluído |
| 8 | Cupons de desconto (`/coupons`) | ✅ Concluído |
| 9 | Cálculo de frete (`/shipping`) | ✅ Concluído |
| 10 | Pagamento Pix (`/payments`) | ✅ Concluído |
| 11 | Pagamento com cartão (Stripe mock) | ✅ Concluído |
| 12 | Painel administrativo (`/admin`) | ✅ Concluído |
| 13 | Cache Redis no catálogo | ✅ Concluído |
| 14 | Dashboard de métricas | ✅ Concluído |

---

## Roadmap — Frontend

| # | Fase | Status |
|---|------|--------|
| 0 | Setup Vite + React + Tailwind + **CORS no backend** + layout base | Pendente |
| 1 | Roteamento + guards + interceptor JWT (Axios) | Pendente |
| 2 | Autenticação (login, cadastro, `/users/me`) | Pendente |
| 3 | Catálogo de produtos (lista, busca, filtros, paginação) | Pendente |
| 4 | Detalhe do produto + adicionar ao carrinho | Pendente |
| 5 | Carrinho (add, update, remove, total) | Pendente |
| 6 | Checkout (endereço, cupom, frete, criação de pedido) | Pendente |
| 7 | Pagamento (Pix com QR, cartão com Stripe) | Pendente |
| 8 | Meus pedidos (lista + detalhe) | Pendente |
| 9 | Painel admin (produtos, pedidos, usuários, dashboard) | Pendente |
| 10 | Deploy + integração com backend em produção | Pendente |

---

## Fases concluídas — Backend

### Fase 0 — Setup

Estrutura de pacotes em `com.alvescosta.loja`: `config`, `domain`, `dto`, `exception`, `repository`, `security`, `service`, `controller`.

- Spring Boot 3.4.0 + Java 21
- Dependências: Web, Security, Data JPA, Cache, Redis, Validation, Actuator, Micrometer Prometheus, Flyway, JJWT 0.12.6, PostgreSQL, H2, Lombok 1.18.36
- `application.yml` (produção) e `application-test.yml` (teste)
- `@EnableMethodSecurity` habilitado

---

### Fase 1 — Cadastro de usuário

**Endpoint:** `POST /auth/register` → 201

**Testes:** 3
**Implementação:** `User`, `Role`, `UserCreateRequest` (senha mín. 8), `UserResponse`, `UserService.register()` com BCrypt, `AuthController`.

---

### Fase 2 — Login + JWT

**Endpoint:** `POST /auth/login` → 200 com `access_token` (snake_case)

**Testes:** 3
**Implementação:** `JwtService` (HS256), `LoginRequest`, `TokenResponse.bearer()`, `UnauthorizedException`.

---

### Fase 3 — Usuário logado

**Endpoint:** `GET /users/me` → 200

**Testes:** 3
**Implementação:** `JwtAuthFilter`, `UserDetailsServiceImpl` (username = ID), `RestAuthenticationEntryPoint`, `UserController`.

---

### Fase 4 — CRUD de Produtos

**Endpoints:**
- `GET /products` (paginado, `?category=&search=&page=&size=`)
- `GET /products/{id}`
- `POST /products` (ADMIN)
- `PUT /products/{id}` (ADMIN)
- `DELETE /products/{id}` (ADMIN)

**Testes:** 14
**Implementação:** `Product` com `BigDecimal`, `ProductService`, `ProductController` com `@PreAuthorize`, `@PageableDefault(size=10, sort="name")`.

---

### Fase 6 — Carrinho de Compras

**Endpoints:**
- `POST /cart/items`
- `GET /cart`
- `PUT /cart/items/{id}`
- `DELETE /cart/items/{id}`
- `DELETE /cart`

**Testes:** 14
**Implementação:** `CartItem` com unique `(user_id, product_id)`, `@OnDelete(CASCADE)`, `CartService`, `CartCalculator`, `CartController`, `AddToCartRequest`, `UpdateCartItemRequest`, `CartResponse`, `CartItemResponse`.

---

### Fase 7 — Checkout e Pedidos

**Endpoints:**
- `POST /orders?couponCode=&cep=`
- `GET /orders`
- `GET /orders/{id}`
- `PATCH /orders/{id}/status` (ADMIN)

**Testes:** 17
**Implementação:** `Order`, `OrderItem` com snapshot (`productName`, `unitPrice`), `OrderStatus`, `OrderService` validando estoque antes de mutar, `OrderResponse`, `OrderItemResponse`, `UpdateOrderStatusRequest`.

---

### Fase 8 — Cupons de Desconto

**Endpoints:**
- `POST /coupons` (ADMIN)
- `GET /coupons/{code}`

**Testes:** 12
**Implementação:** `Coupon` com coluna `discount_value` (`value` é palavra reservada em SQL), `DiscountType`, `CouponService` (expiração, limite de uso, valor mínimo), `CouponRequest`, `CouponResponse`.

---

### Fase 9 — Cálculo de Frete

**Endpoints:**
- `POST /shipping/quote`
- `POST /orders?cep=...`

**Testes:** 6
**Implementação:** `ShippingService` com tabela por prefixo de CEP, frete grátis acima de R$ 200, `ShippingType`, `ShippingOption`, `ShippingQuoteRequest`, `ShippingQuoteResponse`.

---

### Fase 10 — Pagamento Pix

**Endpoints:**
- `POST /payments?orderId=...`
- `GET /payments/{id}`
- `POST /payments/{id}/confirm` (ADMIN)

**Testes:** 8
**Implementação:** `Payment`, `PaymentMethod`, `PaymentStatus`, `PixGenerator` (mock EMV + QR base64), `PaymentService`, `PaymentResponse`.

---

### Fase 11 — Pagamento com cartão (Stripe mock)

**Endpoints:**
- `POST /payments/card?orderId=...`
- `POST /webhooks/stripe`

**Testes:** 9
**Implementação:** `PaymentGateway` (interface), `StripePaymentGateway` (mock), `WebhookController`, idempotência no webhook, migration **V9** (`alter_payments_add_gateway_fields`).

⚠️ **Nota:** o SDK oficial do Stripe **não está** no `pom.xml`. Para produção real, será necessário adicionar `com.stripe:stripe-java`.

---

### Fase 12 — Painel administrativo

**Endpoints (todos ADMIN):**
- `GET /admin/dashboard`
- `GET /admin/orders`
- `GET /admin/users`
- `PATCH /admin/users/{id}/role`
- `GET /admin/products/low-stock`
- `POST /admin/products/{id}/restock`

**Testes:** 12
**Implementação:** `AdminService`, queries agregadas com `@Query`, guarda-corpo contra auto-rebaixamento, `DashboardResponse`, `AdminUserResponse`, `UpdateRoleRequest`, `RestockRequest`.

---

### Fase 13 — Cache Redis no catálogo

**Testes:** 8
**Implementação:** `spring-boot-starter-cache` + `spring-boot-starter-data-redis`, `CacheConfig` com `@EnableCaching`, `@Cacheable` em `findById` e `list`, `@CacheEvict` em `create`/`update`/`delete`, dois caches (`products`, `productLists`), TTL 5 min, `cache-null-values: false`. Testes com `simple` cache.

---

### Fase 14 — Dashboard de métricas

**Endpoints Actuator:**
- `GET /actuator/health` (público)
- `GET /actuator/info` (público)
- `GET /actuator/metrics` (autenticado)
- `GET /actuator/prometheus` (autenticado)

**Métricas customizadas:**
- `loja.orders.created` — incrementa em cada checkout
- `loja.payments.confirmed` — incrementa em cada confirmação

**Testes:** 7
**Implementação:** Micrometer + Prometheus, `MetricsService`, `PrometheusMetricsConfig` (contorna bug de auto-config do Spring Boot 3.4 com injeção precoce do `MeterRegistry`), instrumentação em `OrderService` e `PaymentService`, tag `application=loja-api`.

---

## Fases concluídas — Frontend

*(Nenhuma ainda. Primeira fase prevista: Setup Vite + React + Tailwind + CORS no backend.)*

---

## Pré-requisitos para iniciar o frontend

Antes da Fase 0 do frontend, é necessário:

1. **Configurar CORS no backend.**
   ⚠️ **Estado atual:** o `SecurityConfig` **não** chama `.cors(...)` e **não existe** `CorsConfig`. Isso bloqueia totalmente o frontend.
2. **Node.js** versão 20 LTS ou superior.
3. **Nenhum CLI global necessário** — usar `npm create vite@latest`.
4. **Estratégia de autenticação:** `localStorage` (simples) — decisão provisória, revisar antes do deploy.

### CORS — implementação recomendada

Adicionar **duas coisas** (a segunda é frequentemente esquecida):

**1. Classe `CorsConfig`** (não existe hoje):

```java
package com.alvescosta.loja.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**2. `.cors(...)` no `SecurityConfig`** (obrigatório — sem isso o Spring Security bloqueia antes do MVC):

No `securityFilterChain`, adicionar logo após `.csrf(...)`:

```java
.cors(Customizer.withDefaults())
```

E importar:

```java
import org.springframework.security.config.Customizer;
```

**Sem esse passo 2, a configuração de CORS do passo 1 é ignorada.**

---

## Estrutura prevista do projeto frontend

```
loja-web/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.ts
├── index.html
├── .env.example                            # VITE_API_URL
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── router.tsx
│   ├── api/
│   │   ├── client.ts                       # Axios + interceptors
│   │   ├── auth.ts
│   │   ├── products.ts
│   │   ├── cart.ts
│   │   ├── orders.ts
│   │   ├── coupons.ts
│   │   ├── shipping.ts
│   │   ├── payments.ts
│   │   └── admin.ts
│   ├── stores/
│   │   ├── authStore.ts                    # Zustand
│   │   └── cartStore.ts
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useProducts.ts
│   │   ├── useCart.ts
│   │   └── useOrders.ts
│   ├── schemas/                            # Zod
│   │   ├── auth.ts
│   │   ├── product.ts
│   │   └── checkout.ts
│   ├── pages/
│   │   ├── Home.tsx
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   ├── Catalog.tsx
│   │   ├── ProductDetail.tsx
│   │   ├── Cart.tsx
│   │   ├── Checkout.tsx
│   │   ├── Payment.tsx
│   │   ├── Orders.tsx
│   │   ├── OrderDetail.tsx
│   │   └── admin/
│   │       ├── Dashboard.tsx
│   │       ├── Products.tsx
│   │       ├── Orders.tsx
│   │       └── Users.tsx
│   ├── components/
│   │   ├── layout/
│   │   ├── ui/
│   │   └── route-guards/
│   │       ├── ProtectedRoute.tsx
│   │       ├── AdminRoute.tsx
│   │       └── GuestRoute.tsx
│   ├── lib/
│   │   ├── format.ts                       # formatação de dinheiro
│   │   └── errors.ts
│   └── types/
│       └── api.ts                          # tipos gerados dos DTOs
└── tests/
    ├── unit/
    └── e2e/
```

**Fluxo de navegação previsto:**

```
Home ──► Catalog ──► ProductDetail ──► Cart ──► Checkout ──► Payment ──► Orders
  │                                                                        │
  ├──► Login / Register                                                    │
  └──► (se ADMIN) AdminDashboard ──► AdminProducts / AdminOrders / AdminUsers
```

---

## Contrato da API (referência para o frontend)

Base URL: `http://localhost:8080` (dev)

### Formato de erro (todos os endpoints)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente para o produto X"
}
```

| Código | Quando |
|:------:|--------|
| 400 | Validação ou `BusinessException` |
| 401 | Token ausente/inválido/expirado |
| 403 | Acesso negado (sem tratamento customizado — formato default do Spring) |
| 404 | Recurso não encontrado |
| 500 | Erro inesperado (sem tratamento customizado) |

### Autenticação

**`POST /auth/register`** — público — 201
```json
// request
{ "email": "cli@exemplo.com", "name": "Cliente", "password": "senha12345" }
// response
{ "id": 1, "email": "cli@exemplo.com", "name": "Cliente", "role": "CUSTOMER" }
```

**`POST /auth/login`** — público — 200
```json
// request
{ "email": "cli@exemplo.com", "password": "senha12345" }
// response
{ "access_token": "eyJ...", "token_type": "Bearer", "expires_in": 3600 }
```

**`GET /users/me`** — autenticado — 200
```json
{ "id": 1, "email": "cli@exemplo.com", "name": "Cliente", "role": "CUSTOMER" }
```

### Produtos

**`GET /products?category=&search=&page=&size=`** — público
```json
// Spring Page
{
  "content": [
    { "id": 1, "name": "Camiseta", "price": 79.90, "stock": 10,
      "category": "Roupas", "imageUrl": "https://..." }
  ],
  "totalElements": 1, "totalPages": 1, "size": 10, "number": 0
}
```

**`GET /products/{id}`** — público — cached

**`POST /products`** — ADMIN — 201
```json
{ "name": "Camiseta", "price": 79.90, "stock": 10,
  "category": "Roupas", "imageUrl": "https://..." }
```

**`PUT /products/{id}`** — ADMIN — 200
**`DELETE /products/{id}`** — ADMIN — 204

### Carrinho

**`POST /cart/items`** — autenticado
```json
{ "productId": 1, "quantity": 2 }
```

**`GET /cart`** — autenticado
```json
{
  "items": [
    { "id": 1, "productId": 1, "productName": "Camiseta",
      "imageUrl": "https://...", "unitPrice": 79.90,
      "quantity": 2, "subtotal": 159.80 }
  ],
  "total": 159.80,
  "itemCount": 1
}
```

**`PUT /cart/items/{id}`** — `{ "quantity": 3 }`
**`DELETE /cart/items/{id}`** — 204
**`DELETE /cart`** — 204

### Pedidos

**`POST /orders?couponCode=&cep=`** — autenticado
```json
{
  "id": 1, "userId": 1, "status": "PENDING",
  "total": 179.80, "discount": 0, "shippingCost": 20.00,
  "couponCode": null, "createdAt": "2026-01-15T10:00:00Z",
  "items": [
    { "id": 1, "productId": 1, "productName": "Camiseta",
      "unitPrice": 79.90, "quantity": 2, "subtotal": 159.80 }
  ]
}
```

**`GET /orders`** — autenticado — lista
**`GET /orders/{id}`** — dono ou ADMIN
**`PATCH /orders/{id}/status`** — ADMIN
```json
{ "status": "PAID" }
```

### Cupons

**`POST /coupons`** — ADMIN
```json
{
  "code": "BEMVINDO10", "type": "PERCENTAGE",
  "value": 10, "minOrderValue": 100,
  "maxUses": 100, "expiresAt": "2026-12-31T23:59:59Z"
}
```

**`GET /coupons/{code}`** — público

### Frete

**`POST /shipping/quote`** — público
```json
// request
{ "cep": "01310100", "subtotal": 159.80 }
// response
{
  "cep": "01310100",
  "options": [
    { "type": "PAC", "label": "PAC (5-8 dias)", "price": 20.00 },
    { "type": "SEDEX", "label": "SEDEX (2-3 dias)", "price": 35.00 }
  ]
}
```

⚠️ **Atenção:** o cliente envia o `subtotal`. Em produção isso deveria ser calculado no servidor para evitar fraude (frete grátis indevido).

### Pagamentos

**`POST /payments?orderId=1`** — dono — gera Pix
```json
{
  "id": 1, "orderId": 1, "method": "PIX", "status": "PENDING",
  "amount": 179.80,
  "pixCopyPaste": "00020126...", "pixQrCodeBase64": "iVBORw0KG...",
  "gatewayRef": null, "clientSecret": null,
  "createdAt": "...", "expiresAt": "...", "confirmedAt": null
}
```

**`GET /payments/{id}`** — dono
**`POST /payments/{id}/confirm`** — ADMIN
**`POST /payments/card?orderId=1`** — dono
**`POST /webhooks/stripe`** — público

### Admin

**`GET /admin/dashboard`** — ADMIN
```json
{ "totalOrders": 42, "totalRevenue": 8540.50, "averageTicket": 203.35 }
```

**`GET /admin/orders`** — ADMIN
**`GET /admin/users`** — ADMIN
```json
[
  { "id": 1, "email": "cli@exemplo.com", "name": "Cliente",
    "role": "CUSTOMER", "orderCount": 5, "totalSpent": 1230.00 }
]
```

**`PATCH /admin/users/{id}/role`** — ADMIN — `{ "role": "ADMIN" }`
**`GET /admin/products/low-stock`** — ADMIN
**`POST /admin/products/{id}/restock`** — ADMIN — `{ "quantity": 50 }`

### Actuator

- `GET /actuator/health` — público
- `GET /actuator/info` — público
- `GET /actuator/metrics` — autenticado
- `GET /actuator/prometheus` — autenticado

---

## Decisões técnicas — Backend

| Decisão | Por quê |
|---------|---------|
| `BigDecimal` para dinheiro | Evita erro de arredondamento de `double` |
| JWT stateless | API escala horizontalmente sem sessão |
| Flyway em produção, H2 `create-drop` em testes | Schema real auditável + testes rápidos |
| Snapshot de preços em `OrderItem` | Preço histórico fica gravado mesmo se produto mudar |
| `@OnDelete(CASCADE)` explícito | FKs consistentes entre H2 e PostgreSQL |
| `record` para DTOs | Imutabilidade e menos boilerplate |
| `@EnableMethodSecurity` | Sem isso, `@PreAuthorize` é silenciosamente ignorado |
| `username = user.getId()` | O subject do JWT é o ID do usuário, não o e-mail |
| `total = subtotal − discount + shippingCost` | Fórmula canônica em `OrderService` |
| Cache Redis TTL 5 min, dois caches (`products`, `productLists`) | Invalidação cirúrgica |
| `@CacheEvict` em `create`/`update`/`delete` | Consistência após mutação |
| `PrometheusMetricsConfig` explícito | Contorna bug de auto-config do Spring Boot 3.4 com injeção precoce do `MeterRegistry` |
| `StripePaymentGateway` mock | Testa fluxo sem SDK/API externo |
| Coluna `discount_value` (não `value`) | `value` é palavra reservada em SQL |
| `RestAuthenticationEntryPoint` | Retorna 401 em vez do 403 default do Spring |
| `open-in-view: false` | Evita lazy loading fora da transação |
| `ddl-auto: validate` em produção | Flyway controla o schema, Hibernate só valida |
| Token em **snake_case** (`access_token`, `token_type`, `expires_in`) | Compatível com spec OAuth2 |
| `MethodArgumentNotValidException` → mensagem concatenada `campo: erro` | Feedback único e legível |

---

## Decisões técnicas — Frontend

*(A confirmar durante a Fase 0)*

Decisões provisórias:

| Decisão | Justificativa provisória |
|---------|--------------------------|
| **React 19 + Vite** | Build rápido, HMR, ecossistema atual |
| **TypeScript 5.6** | Tipagem forte no contrato da API |
| **Tailwind CSS 4** (plugin Vite) | Sem PostCSS separado, config em CSS |
| **Zustand** para estado global | Menos boilerplate que Redux; só auth e carrinho |
| **TanStack Query** para server state | Cache, refetch, retry, invalidação |
| **Axios** com interceptors | Centraliza JWT e tratamento de erro |
| **React Router 7** | Padrão da comunidade, loaders/actions |
| **React Hook Form + Zod** | Performance + validação tipada compartilhada |
| **JWT em `localStorage`** | Simples; ⚠️ revisar antes do deploy (XSS) |
| **Vitest + Testing Library** | Integração com Vite |
| **Playwright** para E2E | Multi-browser, mais robusto que Cypress |

---

## Pontos de atenção herdados do backend

Coisas que o frontend precisa saber para não quebrar:

1. **Token em snake_case** — `access_token`, `token_type`, `expires_in`. Não é camelCase como o resto.
2. **Erros sempre no formato** `{ status, error, message }` — exceto 403 e 500, que caem no default do Spring (formato diferente).
3. **`GET /products` retorna Spring Page** — campos `content`, `totalElements`, `totalPages`, `size`, `number`. Não é array puro.
4. **Paginação é 0-indexed** (`number: 0` é a primeira página).
5. **`POST /orders` recebe `cep` e `couponCode` como query params**, não no body.
6. **`POST /shipping/quote` recebe `subtotal` do cliente** — o backend confia no valor enviado. O frontend deve calcular e enviar corretamente.
7. **`POST /payments` recebe `orderId` como query param**.
8. **`POST /payments/card` também recebe `orderId` como query param**.
9. **`PATCH /orders/{id}/status`** — usar PATCH, não PUT.
10. **`PATCH /admin/users/{id}/role`** — usar PATCH.
11. **`DELETE` retorna 204** (sem body) — não tentar fazer `.json()` na resposta.
12. **`POST /auth/register` retorna 201**, não 200.
13. **Cache Redis no catálogo** — após `POST/PUT/DELETE /products`, o cache é invalidado automaticamente. O frontend não precisa fazer nada especial, mas se exibir dados em cache local, deve invalidar via TanStack Query.
14. **`/actuator/metrics` e `/actuator/prometheus` exigem token** — não são públicos.
15. **CORS ainda não está configurado** — bloqueio total até a Fase 0 do frontend ser feita.
16. **JWT expira em 1h** (`expires_in: 3600`) — o interceptor precisa lidar com 401 e redirecionar para login.
17. **`PaymentResponse` mistura campos de Pix e cartão** — campos não usados vêm `null`.
18. **`UserResponse` não retorna senha nem timestamps** — só `id`, `email`, `name`, `role`.
19. **`OrderItemResponse.productId` pode ser `null`** se o produto foi removido — o snapshot preserva `productName` e `unitPrice`.
20. **Webhook `/webhooks/stripe` é público** — não enviar token.

---

## Bugs encontrados e correções — Backend

| # | Bug | Correção |
|---|-----|----------|
| 1 | `@PreAuthorize` ignorado silenciosamente | Habilitar `@EnableMethodSecurity` no `SecurityConfig` |
| 2 | 403 em vez de 401 para token ausente | Criar `RestAuthenticationEntryPoint` customizado |
| 3 | Coluna `value` reservada em SQL (tabela `coupons`) | Renomear para `discount_value` |
| 4 | FKs inconsistentes entre H2 e PostgreSQL | Usar `@OnDelete(CASCADE)` explícito |
| 5 | `MeterRegistry` injetado cedo quebrava auto-config do Spring Boot 3.4 | Criar `PrometheusMetricsConfig` explícito |
| 6 | Warnings do Mockito/ByteBuddy no JDK 21 | Adicionar `-XX:+EnableDynamicAgentLoading` no Surefire `argLine` |
| 7 | Nomes de parâmetros perdidos em runtime | Ativar `<parameters>true</parameters>` no compilador Maven |
| 8 | Cache servia dados obsoletos após update | `@CacheEvict` em `create`/`update`/`delete` |

*(Adicionar novos bugs aqui conforme forem encontrados.)*

---

## Bugs encontrados e correções — Frontend

*(Nenhum ainda — projeto a iniciar.)*

---

## Como executar — Backend

### Pré-requisitos

- JDK 21
- Maven (ou `./mvnw`)
- PostgreSQL 16 (produção)
- Redis 7 (produção)

### Passos

```bash
# 1. Clonar
git clone https://github.com/AlvesCosta08/loja-api.git
cd loja-api

# 2. Criar banco
createdb loja

# 3. Variáveis (opcional — há defaults)
export DB_USER=postgres
export DB_PASS=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=$(openssl rand -base64 48)

# 4. Rodar
./mvnw spring-boot:run

# 5. Testes
./mvnw clean test
```

### Variáveis de ambiente

| Variável | Padrão |
|----------|--------|
| `DB_USER` | `postgres` |
| `DB_PASS` | `postgres` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `JWT_SECRET` | `change-me-in-production-with-at-least-32-chars` |
| `JWT_EXPIRATION_MS` | `3600000` (⚠️ **hardcoded** — não lê env) |

---

## Como executar — Frontend

*(A definir na Fase 0.)*

Previsto:

```bash
npm create vite@latest loja-web -- --template react-ts
cd loja-web
npm install
npm install -D tailwindcss @tailwindcss/vite
npm install axios zustand @tanstack/react-query react-router-dom react-hook-form zod
npm run dev
```

Variável de ambiente prevista:

```
VITE_API_URL=http://localhost:8080
```

---

## Guia de implementação React — passo a passo

> Este guia assume que a **Fase 0 (CORS no backend)** já foi concluída.

### Fase 0 — Setup + layout base

1. `npm create vite@latest loja-web -- --template react-ts`
2. Instalar Tailwind 4 com plugin Vite
3. Configurar `vite.config.ts` com o plugin `@tailwindcss/vite`
4. Criar layout base: `Header`, `Footer`, `Layout` com `<Outlet />`
5. Criar `.env.example` com `VITE_API_URL`
6. Configurar `axios` client (`src/api/client.ts`) com `baseURL`

### Fase 1 — Roteamento + guards + interceptor

1. Instalar `react-router-dom`
2. Definir rotas públicas, autenticadas e admin
3. Criar `ProtectedRoute`, `AdminRoute`, `GuestRoute`
4. Adicionar interceptor de request no Axios: adiciona `Authorization: Bearer <token>`
5. Adicionar interceptor de response: em 401, limpa token e redireciona para `/login`
6. Criar `authStore` com Zustand (`token`, `user`, `login()`, `logout()`)

### Fase 2 — Autenticação

1. Página `Login` com React Hook Form + Zod
2. Página `Register`
3. Hook `useAuth()` com TanStack Query (`/auth/login`, `/auth/register`, `/users/me`)
4. Redirecionar para a home após login
5. Header mostra usuário logado ou botão "Entrar"

### Fase 3 — Catálogo

1. Página `Catalog` consumindo `GET /products`
2. Filtro por `category`, busca por `search`
3. Paginação (baseada em `content`, `totalElements`, `number`)
4. Card de produto com link para detalhe

### Fase 4 — Detalhe + adicionar ao carrinho

1. Página `ProductDetail` com `GET /products/{id}`
2. Botão "Adicionar ao carrinho" → `POST /cart/items`
3. Invalidação de cache do carrinho no TanStack Query

### Fase 5 — Carrinho

1. Página `Cart` com `GET /cart`
2. Aumentar/diminuir quantidade → `PUT /cart/items/{id}`
3. Remover item → `DELETE /cart/items/{id}`
4. Limpar carrinho → `DELETE /cart`

### Fase 6 — Checkout

1. Formulário de endereço (CEP) com Zod
2. Aplicar cupom → `GET /coupons/{code}`
3. Cotar frete → `POST /shipping/quote` (enviar subtotal calculado)
4. Criar pedido → `POST /orders?cep=&couponCode=`

### Fase 7 — Pagamento

1. Escolher Pix ou cartão
2. Pix: `POST /payments?orderId=` → renderizar `pixQrCodeBase64` e `pixCopyPaste`
3. Cartão: `POST /payments/card?orderId=` → usar `clientSecret` (mock)

### Fase 8 — Meus pedidos

1. Página `Orders` com `GET /orders`
2. Página `OrderDetail` com `GET /orders/{id}`

### Fase 9 — Painel admin

1. `AdminRoute` bloqueia não-admins
2. `Dashboard`, `Products`, `Orders`, `Users`
3. Usar `/admin/dashboard`, `/admin/products/low-stock`, `/admin/users`, etc.

### Fase 10 — Deploy

1. Build `npm run build`
2. Deploy Vercel/Netlify
3. Configurar `VITE_API_URL` para o backend em produção
4. Atualizar CORS no backend para aceitar o domínio de produção

---

## Possíveis extensões futuras

### Backend
- Stripe real (adicionar SDK + chaves)
- Paginação e filtros em `/admin/orders` e `/admin/users`
- Refresh token (hoje só access token)
- Rate limiting (Bucket4j)
- Auditoria (`@CreatedBy`, `@LastModifiedDate`)
- Soft delete em produtos
- Upload de imagens (S3 / Cloudinary)
- Documentação OpenAPI/Swagger
- Testes de contrato (Pact)
- Docker Compose com PostgreSQL + Redis
- CI/CD com GitHub Actions
- Tratamento de 403, 405, 409, 500 no `GlobalExceptionHandler`

### Frontend
- Modo escuro
- Internacionalização (i18n)
- PWA
- Skeleton loaders
- Testes E2E completos

---

## Como atualizar este documento

Ao final de **cada fase concluída**:

1. Marcar a fase como ✅ no roadmap.
2. Adicionar seção na parte de "Fases concluídas" com:
   - Endpoints afetados
   - Nº de testes
   - Arquivos principais
   - Decisões tomadas
3. Atualizar a tabela "Informações gerais" se stack/versão mudar.
4. Adicionar bugs encontrados na seção apropriada.
5. Atualizar "Pontos de atenção herdados" se o contrato da API mudar.
6. Atualizar "Última atualização" no topo.
7. Bump da versão no `pom.xml` e em `application.yml` (`info.app.version`).

---

## 📌 Pendências conhecidas (antes de considerar o projeto 100% pronto)

| # | Pendência | Bloqueia |
|---|-----------|:--------:|
| 1 | `CorsConfig` não existe | Frontend |
| 2 | `.cors(...)` ausente no `SecurityConfig` | Frontend |
| 3 | Versão `0.10.0` desatualizada (deveria ser `0.14.0`) | — |
| 4 | `<email>SEU-EMAIL-REAL</email>` no `pom.xml` | Publicação |
| 5 | `JWT_EXPIRATION_MS` hardcoded no `application.yml` | Config |
| 6 | Stripe é mock (sem SDK) | Produção |
| 7 | `GlobalExceptionHandler` não cobre 403/405/409/500 | Consistência |
| 8 | README do repositório `loja-web` ainda não criado | Frontend |