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
8. [Arquitetura (diagramas)](#arquitetura-diagramas)
9. [Contrato da API (referência para o frontend)](#contrato-da-api-referência-para-o-frontend)
10. [Decisões técnicas — Backend](#decisões-técnicas--backend)
11. [Decisões técnicas — Frontend](#decisões-técnicas--frontend)
12. [Pontos de atenção](#pontos-de-atenção)
13. [Bugs encontrados e correções](#bugs-encontrados-e-correções)
14. [Como executar — Backend](#como-executar--backend)
15. [Como executar — Frontend](#como-executar--frontend)
16. [Próximos passos](#próximos-passos)
17. [Possíveis extensões futuras](#possíveis-extensões-futuras)
18. [Como atualizar este documento](#como-atualizar-este-documento)

---

## Informações gerais

### Backend

| Campo | Valor |
|-------|-------|
| Projeto | loja-api |
| Descrição | API REST de e-commerce com autenticação, catálogo, carrinho, pedidos, cupons, frete, pagamentos Pix/cartão, admin, métricas e fundação de IA (RAG com TDD) |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.0 |
| Versão atual | **0.15.0-SNAPSHOT** |
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
| Última atualização backend | **Fase 16 — Fundação do RAG (7 ciclos TDD)** |

### Frontend

| Campo | Valor |
|-------|-------|
| Projeto | loja-web |
| Descrição | SPA de e-commerce que consome a `loja-api` |
| Framework | React 19 + Vite 6 |
| Linguagem | TypeScript 5.6 |
| Estilização | Tailwind CSS 4 (plugin `@tailwindcss/vite`) |
| Arquitetura | **Feature-Sliced Design (FSD)** — `app/`, `shared/`, `features/` |
| Estado | Zustand (global) + TanStack Query (server state) |
| HTTP | Axios + interceptors (JWT e 401 global) |
| Rotas | React Router 7 + `ProtectedRoute` / `GuestRoute` / `AdminRoute` |
| Formulários | React Hook Form + Zod |
| Toasts | Sonner |
| Testes | Vitest + Testing Library (unit) |
| Build | Vite |
| Deploy previsto | Vercel / Netlify |
| Repositório frontend | https://github.com/AlvesCosta08/loja-web |
| Última atualização frontend | **Fase 9 — Painel Admin (Dashboard, Pedidos, Produtos, Usuários)** |

### Autor

| Campo | Valor |
|-------|-------|
| Autor | Alves Costa |
| GitHub | https://github.com/AlvesCosta08 |
| Timezone | America/Fortaleza |

---

## Status geral

### Backend

**Roadmap completo: 16 de 16 fases concluídas.**

- **150 testes** automatizados, todos verdes.
- **21 classes de teste** + 1 config (`PrometheusTestConfig`).
- **~95 arquivos** de produção.
- **9 migrations** Flyway (V1 a V9).
- **Fundação do RAG completa e blindada por testes** (unidade + integração).
- **Infra Dev** (CORS, `.env`, Docker Compose, `run-dev.sh`) operacional.

### Frontend

**Roadmap: 9 de 11 fases concluídas.**

- ✅ Fases 0, 1, 2, 3, 4, 5, 6, 7, 8 e 9 (setup, guards, auth, catálogo, detalhe, carrinho, checkout, pagamento, histórico e painel admin).
- 🔴 Fase 10 pendente (deploy).
- **37 testes** verdes (Vitest + Testing Library).
- **F5-safe payment** implementado (recupera o Pix após reload sem gerar duplicata).

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
- Infraestrutura de desenvolvimento (Docker Compose, `.env`, script de automação)
- **Fundação do RAG**: embedding, vetores, vector store, indexação por eventos, busca com threshold

**Frontend** (9/11 fases concluídas)

- ✅ Autenticação com JWT (login, cadastro, logout)
- ✅ Vitrine de produtos com busca, filtros e paginação
- ✅ Página de detalhe com add ao carrinho
- ✅ Carrinho persistente com atualização de quantidade
- ✅ Checkout completo (CEP, frete, criação de pedido)
- ✅ Histórico de pedidos com status e detalhes
- ✅ **Pagamento Pix (QR Code) e cartão**
- ✅ **Painel admin (produtos, pedidos, usuários, dashboard)**
- ✅ **F5-safe payment** (recupera Pix em andamento)
- 🔴 Deploy
- ✅ Design responsivo (mobile-first)

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
| 15 | Infra Dev: CORS, Env Vars, Docker, Flyway Script | ✅ Concluído |
| 16 | Fundação do RAG (7 ciclos TDD) | ✅ Concluído |

### Roadmap de IA (visão macro)

| Fase | Descrição | Status |
|:---:|---|:---:|
| RAG | Embedding + VectorStore + Indexer + Retriever + AssistantService | Em progresso (fundação concluída) |
| MCP | Servidor MCP com tools (search, cart, order, payment) | Pendente |
| Fine-tuning | Classificador de intenção + gerador de descrições | Pendente |

---

## Roadmap — Frontend

| # | Fase | Status |
|---|------|--------|
| 0 | Setup Vite + React + Tailwind + **CORS no backend** + layout base | ✅ Concluído |
| 1 | Roteamento + guards + interceptor JWT (Axios) | ✅ Concluído |
| 2 | Autenticação (login, cadastro, `/users/me`) | ✅ Concluído |
| 3 | Catálogo de produtos (lista, busca, filtros, paginação) | ✅ Concluído |
| 4 | Detalhe do produto + adicionar ao carrinho | ✅ Concluído |
| 5 | Carrinho (add, update, remove, total) | ✅ Concluído |
| 6 | Checkout (endereço, cupom, frete, criação de pedido) | ✅ Concluído |
| 7 | Pagamento (Pix com QR, cartão com Stripe) | ✅ Concluído |
| 8 | Meus pedidos (lista + detalhe) | ✅ Concluído |
| 9 | Painel admin (produtos, pedidos, usuários, dashboard) | ✅ Concluído |
| 10 | Deploy + integração com backend em produção | 🔴 Pendente |

**Progresso:** 9/11 fases concluídas · 37 testes verdes.

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

### Fase 15 — Infraestrutura de Desenvolvimento

**Testes:** +3 (`CorsConfigTest`).

**Implementação:**
- **CORS Resolvido:** Criado `CorsConfig` (WebMvcConfigurer) e adicionado `.cors(Customizer.withDefaults())` no `SecurityConfig`. Permite `http://localhost:5173`.
- **Variáveis de Ambiente:** `application.yml` parametrizado com `${VAR:default}` para banco, Redis, JWT, porta, etc.
- **Dockerização de Dev:** `docker-compose.yml` configurado para PostgreSQL 16 e Redis 7 com healthchecks e redes.
- **Automação com Script:** Criado `run-dev.sh` que carrega `.env`, sobe o docker, espera o Postgres, roda as migrações Flyway (adicionado `flyway-maven-plugin` no `pom.xml`) e inicia o Spring Boot. Suporta subcomandos: `up`, `down`, `reset`, `db <cmd>`, `mvn <args>`.
- **Dockerfile:** Atualizado para Java 21 (estava 17) e otimizado com multi-stage build.
- **Configuração Local:** `.env.example` criado e `.env` ignorado via `.gitignore`.

---

### Fase 16 — Fundação do RAG (7 ciclos TDD)

**Branch:** `feature/rag-foundation`
**Testes:** +28 (de 119 para 147)
**Estrutura criada:** pacote `com.alvescosta.loja.assistant` com subpacotes `embedding`, `vector`, `indexer`, `retrieval`, `llm`, `dto`, `service`.

**Ciclos concluídos:**

#### Ciclo 1 — `EmbeddingProvider` (6 testes)
- Interface `EmbeddingProvider` com `embed()` e `embedAll()`.
- `FakeEmbeddingProvider`: hash SHA-256 como seed do `Random`, gera vetor de 1536 dimensões, normaliza L2. Determinístico.

#### Ciclo 2 — `Vectors` (6 testes)
- Utilitário `Vectors.normalizarL2()` e `Vectors.similaridadeCosseno()`.
- Refatoração do `FakeEmbeddingProvider` para reutilizar `Vectors.normalizarL2()` (DRY).

#### Ciclo 3 — `VectorStore` (6 testes)
- Interface `VectorStore` (`upsert`, `search`, `delete`, `clear`).
- Record `SearchResult(id, vector, metadata, score)`.
- `InMemoryVectorStore` baseado em `ConcurrentHashMap`. Busca ordenada por similaridade cosseno decrescente.

#### Ciclo 4 — `ProductIndexer` (3 testes)
- Record `ProductEvent(id, name, category, active)`.
- `ProductIndexer` escuta eventos e atualiza o `VectorStore`.
- Criação (`active=true`): gera embedding + upsert com metadados.
- Deleção (`active=false`): remove do índice sem gerar embedding.

#### Ciclo 5 — `VectorRetriever` (3 testes)
- `VectorRetriever(embeddingProvider, vectorStore, threshold)`.
- `retrieve(query, topK)`: gera embedding, busca `topK * 2` no store, filtra por threshold, limita ao `topK`.

#### Ciclo 6 — Integração `ProductService` ↔ `ProductIndexer` (2 testes)
- `ProductService` injeta `ApplicationEventPublisher`.
- `create()` e `update()` publicam `ProductEvent(active=true)`.
- `delete()` publica `ProductEvent(active=false)`.

#### Ciclo 7 — Correção de bug crítico do `ProductIndexer` (2 testes de integração)
- **Bug encontrado:** dois `@EventListener` no mesmo tipo de evento. O Spring chamava ambos para todos os eventos, apagando produtos recém-criados do índice.
- **Correção:** consolidar em um único `@EventListener onProductEvent()` que despacha por `active`.
- `ProductIndexerIntegrationTest` usa `ApplicationEventPublisher` real e prova o comportamento end-to-end.

**Decisões técnicas da fase:**
- `FakeEmbeddingProvider` em `src/main` (não em test) para ser reutilizável.
- `AssistantConfig` registra os beans `EmbeddingProvider` e `VectorStore` (sem isso, o `@Component ProductIndexer` quebrava o contexto de todos os `@SpringBootTest`).
- `InMemoryVectorStore` como implementação padrão até a migration V10 do pgvector.
- Testes unitários para o `ProductIndexer` **e** testes de integração com `ApplicationEventPublisher` real — o bug crítico só apareceu na integração.

---

## Fases concluídas — Frontend

### Fase 0 — Setup

- Vite 6 + React 19 + TypeScript 5.6 + Tailwind CSS 4 (`@tailwindcss/vite`).
- Estrutura **Feature-Sliced Design (FSD)**: `app/`, `shared/`, `features/`.
- Alias `@/` configurado no `vite.config.ts` e `tsconfig.json`.
- `Layout` (Header, Footer, Outlet) + `HomePage` (landing).
- Providers globais: `QueryClientProvider` + `Toaster` (Sonner).
- Lazy Loading de páginas com `Suspense` + `Spinner`.
- **CORS no backend** já habilitado (Fase 15 do backend) — sem bloqueio.

---

### Fase 1 — Roteamento + guards + interceptor JWT

- Rotas com `createBrowserRouter` (React Router 7).
- Guards: `ProtectedRoute`, `GuestRoute`, `AdminRoute`.
- **Interceptor Axios:**
  - Request: injeta `Authorization: Bearer <token>`.
  - Response: trata `401` globalmente — limpa token + emite evento `auth:logout`.
- Bootstrapper no `App.tsx`: valida token via `GET /users/me` no reload antes de renderizar rotas.

---

### Fase 2 — Autenticação

- Telas de **Login** e **Cadastro** com React Hook Form + Zod.
- `useAuthStore` (Zustand) — estado `user`, `token`, `isAuthenticated`, `isAdmin`.
- Persistência do token em `localStorage`.
- **Sessão persistente**: reload → `GET /users/me` revalida; inválido → redireciona para `/login`.
- Logout limpa token + cache de queries do TanStack Query.

---

### Fase 3 — Catálogo de produtos

- `ProductListPage` com paginação (`page`, `size`) e busca (`search`) **sincronizadas com a URL** (query params).
- Filtro por categoria.
- React Query (`useQuery`) com cache e `staleTime`.
- Loading (Spinner) e error states.
- Lazy loading das imagens dos produtos.

---

### Fase 4 — Detalhe do produto

- `ProductDetailPage` com imagem, descrição, estoque e preço.
- Botão **Adicionar ao carrinho** integrado à API.
- Tratamento de erro quando fora de estoque.

---

### Fase 5 — Carrinho

- Persistência **server-side** via `GET /cart`.
- Página `CartPage`:
  - Listagem de itens com imagem e subtotal.
  - Atualização de quantidade (`PUT /cart/items/{id}`).
  - Remoção individual (`DELETE /cart/items/{id}`).
  - Total geral e contagem de itens.
  - Botão para checkout.

---

### Fase 6 — Checkout

- Página `CheckoutPage` com:
  - Endereço (CEP obrigatório).
  - **Cálculo de frete** via `POST /shipping/quote`.
  - Criação do pedido (`POST /orders?cep=`) e **redirecionamento direto para `/orders/:id/pay`**.
  - Cálculo do total com frete.

---

### Fase 7 — Pagamento (Pix + Cartão)

- `features/payments/api/api.ts`: `createPixPayment`, `createCardPayment`, `getPayment`, `getLatestByOrder`.
- `usePaymentPolling`: polling a cada 3s enquanto `PENDING`, para em `CONFIRMED`.
- `PayOrderPage`:
  - Tabs **Pix** e **Cartão**.
  - Pix: QR Code (base64) + código copiável + polling.
  - Cartão: processa gateway mockado e exibe `gatewayRef`.
  - **F5-safe:** ao recarregar, `useQuery(getLatestByOrder)` reidrata o Pix sem criar duplicata.
- `OrderDetailPage`: botão "Pagar agora" visível apenas quando `PENDING`.

---

### Fase 8 — Histórico de pedidos

- Página `OrderListPage` com lista de pedidos do usuário (`GET /orders`).
- Cada pedido exibe status, data, valor total e quantidade de itens.
- Detalhe do pedido (`GET /orders/{id}`) com itens, snapshot de preços e status.
- Acesso apenas para usuários autenticados (`ProtectedRoute`).

---

### Fase 9 — Painel Administrativo

- `AdminRoute`: bloqueia `CUSTOMER`, libera `ADMIN`; redireciona não-logados para `/login`.
- `AdminLayout`: sidebar com links (Dashboard, Pedidos, Produtos, Usuários) + `Outlet`.
- **Dashboard** (`/admin`):
  - Cards: pedidos totais, receita, ticket médio, pendentes, estoque baixo.
  - Consome `GET /admin/dashboard`.
- **Pedidos** (`/admin/orders`):
  - Tabela com todos os pedidos (`GET /admin/orders`).
  - Alteração de status via select (`PATCH /orders/{id}/status`).
- **Produtos** (`/admin/products`):
  - Tabela com destaque de estoque baixo (`GET /products`).
  - Reposição de estoque (`POST /admin/products/{id}/restock`).
- **Usuários** (`/admin/users`):
  - Tabela com usuários do sistema (`GET /admin/users`).
  - Alteração de role (`PATCH /admin/users/{id}/role`).

---

## Arquitetura (diagramas)

### Fluxo de autenticação (JWT)

```text
Login (LoginPage)
    │
    ▼
POST /auth/login ──► TokenResponse
    │
    ▼
useAuthStore.setAuth() ──► localStorage.setItem('loja_token')
    │
    ▼
Interceptor Axios ──► Authorization: Bearer <token>
    │
    ▼
Reload da página ──► useAuthBootstrap
    │
    ▼
GET /users/me ──► Revalida o token
    │
    ├── Válido ──► Mantém sessão
    └── Inválido ──► clearAuth() → redireciona para /login
```

### Fluxo de uma compra (Frontend)

```text
Carrinho (CartPage)
    │
    ▼
Checkout (CheckoutPage) ──► POST /orders (cria pedido)
    │
    ▼
PayOrder (PayOrderPage) ──► POST /payments (gera Pix)
    │                        │
    │                        ▼
    │                  QR Code + código copiável
    │                        │
    │                        ▼
    │                  Polling (usePaymentPolling)
    │                        │
    │                        ▼
    │                  GET /payments/{id} (a cada 3s)
    │                        │
    │                        ▼
    │                  Status = CONFIRMED?
    │                        │
    │                        ▼
    └──────────────────► OrderDetail (Pedido Pago)
```

### Fluxo de Cache (React Query)

```text
Adicionar ao Carrinho
    │
    ▼
POST /cart/items ──► invalida cache ['cart']
    │
    ▼
CartPage ──► GET /cart
    │
    ▼
Checkout ──► POST /orders ──► invalida cache ['cart']
    │
    ▼
PayOrder ──► POST /payments ──► polling até CONFIRMED
    │
    ▼
invalida cache ['orders'] e ['order/{id}']
```

### Modelo de estados (Frontend)

```text
Zustand (client state)
├── authStore
│   ├── user, token, isAuthenticated, isAdmin
│   └── setAuth, setUser, clearAuth
└── (futuro) cartStore

React Query (server state)
├── ['cart']
├── ['product', id]
├── ['order', id]
├── ['orders']
├── ['payment', id]
├── ['paymentByOrder', orderId]
├── ['admin-dashboard']
├── ['admin-orders']
├── ['admin-products']
└── ['admin-users']
```

---

## Contrato da API (referência para o frontend)

### Endpoints consumidos pelo frontend

| Método | Endpoint | Feature frontend | Status |
|--------|----------|------------------|:------:|
| POST | `/auth/register` | auth | ✅ |
| POST | `/auth/login` | auth | ✅ |
| GET | `/users/me` | auth | ✅ |
| GET | `/products` | products | ✅ |
| GET | `/products/{id}` | products | ✅ |
| POST | `/cart/items` | cart | ✅ |
| GET | `/cart` | cart | ✅ |
| PUT | `/cart/items/{id}` | cart | ✅ |
| DELETE | `/cart/items/{id}` | cart | ✅ |
| DELETE | `/cart` | cart | ✅ |
| POST | `/orders` | checkout | ✅ |
| GET | `/orders` | orders | ✅ |
| GET | `/orders/{id}` | orders | ✅ |
| PATCH | `/orders/{id}/status` | admin | ✅ |
| POST | `/shipping/quote` | shipping | ✅ |
| GET | `/coupons/{code}` | checkout | ✅ |
| POST | `/payments?orderId=` | payments | ✅ |
| GET | `/payments/{id}` | payments | ✅ |
| GET | `/payments?orderId=` | payments (F5-safe) | ✅ |
| POST | `/payments/card?orderId=` | payments | ✅ |
| GET | `/admin/dashboard` | admin | ✅ |
| GET | `/admin/orders` | admin | ✅ |
| GET | `/admin/users` | admin | ✅ |
| PATCH | `/admin/users/{id}/role` | admin | ✅ |
| GET | `/admin/products/low-stock` | admin | ✅ |
| POST | `/admin/products/{id}/restock` | admin | ✅ |

### Formatos principais

**Erro (formato único):**
```json
{ "status": 400, "error": "Bad Request", "message": "..." }
```

**Token:**
```json
{ "access_token": "...", "token_type": "Bearer", "expires_in": 3600 }
```

**Carrinho, Pedido, Pagamento:** ver `README.md` do backend.

---

## Decisões técnicas — Backend

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
| **`total = subtotal − discount + shippingCost`** | Fórmula canônica em `OrderService` |
| **Cache Redis com TTL 5 min** | Reduz carga em consultas repetidas do catálogo |
| **Dois caches separados** (`products`, `productLists`) | Invalidação cirúrgica sem derrubar listas |
| **`PrometheusMetricsConfig` explícito** | Contorna bug de auto-config do Spring Boot 3.4 |
| **Stripe mockado** (`StripePaymentGateway`) | Testa o fluxo sem depender do SDK externo |
| **Coluna `discount_value`** (não `value`) | `value` é palavra reservada em SQL |
| **`RestAuthenticationEntryPoint`** | Retorna 401 em vez de 403 default do Spring |
| **`GET /payments?orderId`** | F5-safe — frontend recupera o Pix sem duplicata |
| **`.env` + `run-dev.sh`** | Centraliza variáveis sem hardcode |
| **Docker Compose com Healthcheck** | Spring só sobe após Postgres aceitar conexão |
| **`FakeEmbeddingProvider` em `src/main`** | Determinístico, reutilizável |
| **`InMemoryVectorStore` como padrão** | Permite TDD rápido; pgvector depois |
| **Eventos de domínio para indexação** | Desacopla `ProductService` de `ProductIndexer` |
| **Um único `@EventListener` despachando por `active`** | Evita bug de dois listeners mutando o índice em sentidos opostos |
| **`threshold` no `VectorRetriever`** | Descarta resultados com baixa similaridade |

---

## Decisões técnicas — Frontend

| Decisão | Por quê |
|---------|---------|
| **Feature-Sliced Design (FSD)** | Evita acoplamento, escala por domínio de negócio |
| **React 19 + Vite 6** | Fast refresh, build rápido |
| **Tailwind CSS 4** via plugin Vite | Zero-config, tokens via `@theme` |
| **TanStack Query** para server state | Cache, retry, invalidação declarativa |
| **Zustand** para client state | Store de auth e carrinho — simples, sem `Provider` |
| **Axios + interceptors** | Injeção automática de Bearer + tratamento global de 401 |
| **`ProtectedRoute` / `GuestRoute` / `AdminRoute`** | Guards declarativos por role/sessão |
| **Sincronização de busca/paginação na URL** | Compartilhável, back/forward funcionam |
| **Bootstrapper no `App.tsx`** | Valida token via `/users/me` antes de renderizar |
| **React Hook Form + Zod** | Validação schema-first, alinhada com Bean Validation |
| **Sonner** para toasts | Acessível, empilhável, sem CSS extra |
| **Lazy Loading + Suspense** | Reduz bundle inicial |
| **F5-safe payments** (`GET /payments?orderId=`) | Recupera o QR Pix sem gerar duplicata |
| **Polling com `usePaymentPolling`** | Atualiza o status do pagamento a cada 3s até CONFIRMED |
| **`localStorage` para JWT** *(provisório)* | Simples — **revisar antes de produção** (XSS) |
| **Vitest + Testing Library** | Mesma API do Jest, integração nativa com Vite |
| **`@/` alias** | Imports limpos, refatoração sem caminhos relativos |
| **Evento `auth:logout` global** | Desacopla o interceptor Axios da store de auth |

---

## Pontos de atenção

### Backend

1. **`GlobalExceptionHandler`** não cobre 403, 405, 409, 500. Erros 403/500 usam formato default do Spring.
2. **Stripe é mock.** Para produção, adicionar `com.stripe:stripe-java` + configurar `stripe.api-key` e `stripe.webhook-secret`.
3. **`JWT_EXPIRATION_MS`** hardcoded em `application.yml` (não lê env var).
4. **`PgVectorStore`** ainda não existe — só `InMemoryVectorStore`. Necessário migration V10 com pgvector.
5. **Testes de CORS com MockMvc dão falso positivo.** Valide sempre com `curl` + app rodando.
6. **`@EventListener` múltiplos no mesmo tipo de evento são perigosos.** Prefira um único listener que despacha por estado/campo.

### Frontend

1. **`localStorage` para JWT** — vulnerável a XSS. Migrar para `httpOnly cookies` + CSRF antes de prod.
2. **`AdminProducts` usa `productApi.list`** (público). Ideal: endpoint `GET /admin/products` paginado no backend.
3. **Sem cobertura E2E** (Playwright) — apenas unit/integration com Vitest.
4. **Sem Sentry / observabilidade.**
5. **Deploy ainda não configurado** (Vercel/Netlify + CORS prod no backend).

---

## Bugs encontrados e correções

### Backend

| # | Bug | Correção |
|---|-----|----------|
| 1 | `@PreAuthorize` ignorado silenciosamente | `@EnableMethodSecurity` no `SecurityConfig` |
| 2 | 403 em vez de 401 para token ausente | `RestAuthenticationEntryPoint` customizado |
| 3 | Coluna `value` reservada em SQL | Renomear para `discount_value` |
| 4 | FKs inconsistentes entre H2 e PostgreSQL | `@OnDelete(CASCADE)` explícito |
| 5 | `MeterRegistry` injetado cedo quebrava auto-config | `PrometheusMetricsConfig` explícito |
| 6 | Warnings do Mockito/ByteBuddy no JDK 21 | `-XX:+EnableDynamicAgentLoading` no Surefire |
| 7 | Nomes de parâmetros perdidos em runtime | `<parameters>true</parameters>` no Maven |
| 8 | Cache servia dados obsoletos após update | `@CacheEvict` em `create`/`update`/`delete` |
| 9 | `CorsConfigTest` dava falso positivo | Validar via `curl` + `CorsConfig` real |
| 10 | `ProductIndexer` sem beans de dependência quebrava `@SpringBootTest` | `AssistantConfig` registrando `EmbeddingProvider` e `VectorStore` |
| 11 | `ProductIndexer` com dois `@EventListener` apagava produtos recém-criados | Um único `onProductEvent()` despachando por `active` |

### Frontend

| # | Sintoma | Correção |
|---|---------|----------|
| 1 | Token `USER` no mock da store (backend manda `CUSTOMER`) | Trocar para `'CUSTOMER'` |
| 2 | `productApi.getById(id as string)` — erro de tipo | `getById(Number(id))` |
| 3 | `ShippingQuoteResponse` tipado como array | Envelopar em `{ options: [...] }` |
| 4 | `waitFor` importado mas não usado | Remover dos imports nos testes |
| 5 | Ambiguidade de `aria-label` no `AdminProducts` | Diferenciar `repor quantidade` (input) e `repor estoque` (botão) |
| 6 | Botão "Repor" desabilitado antes do React processar o input | `waitFor(() => expect(button).toBeEnabled())` no teste |
| 7 | Teste de polling com fake timers travando | Usar `vi.waitFor` (nativo do Vitest) em vez do `waitFor` do Testing Library |

---

## Como executar — Backend

### Pré-requisitos

- JDK 21
- Docker + Docker Compose
- Maven Wrapper (incluído)

### Passos

```bash
git clone https://github.com/AlvesCosta08/loja-api.git
cd loja-api
cp .env.example .env
./run-dev.sh
```

A API sobe em **http://localhost:8080**.

### Subcomandos do `run-dev.sh`

```bash
./run-dev.sh up        # sobe docker + app
./run-dev.sh down      # derruba serviços
./run-dev.sh reset     # limpa banco e sobe de novo
./run-dev.sh db info   # flyway info
./run-dev.sh db migrate
./run-dev.sh mvn test  # roda os testes
```

### Testes

```bash
./mvnw clean test      # 150 testes
```

---

## Como executar — Frontend

### Pré-requisitos

- Node.js 20 LTS+
- Backend rodando em `http://localhost:8080`

### Passos

```bash
git clone https://github.com/AlvesCosta08/loja-web.git
cd loja-web
npm install
cp .env.example .env
# VITE_API_URL=http://localhost:8080
npm run dev
```

Acesse **http://localhost:5173**.

### Testes e build

```bash
npx vitest run         # 37 testes
npm run build          # gera dist/
```

---

## Próximos passos

### Fase 10 — Deploy (a única pendente)

- **Frontend:** Vercel ou Netlify. Configurar `vercel.json`/`netlify.toml` com SPA fallback + `VITE_API_URL` de produção.
- **Backend:** Render ou Railway. Subir `Dockerfile`, provisionar PostgreSQL + Redis, configurar variáveis de ambiente.
- **CORS prod:** Ajustar `CorsConfig` no backend para o domínio do frontend.
- **Validação:** `curl` de preflight (`OPTIONS`) para confirmar que o navegador não vai bloquear.

### Backend (fases futuras)

1. `PgVectorStore` + migration V10 (pgvector).
2. `AssistantService` (retriever + LLM).
3. `AssistantController` (`POST /assistant/chat`).
4. Servidor MCP com tools (search, cart, order, payment).
5. Fine-tuning (classificador de intenção).

---

## Possíveis extensões futuras

### Frontend

- Migrar auth para `httpOnly cookies` + CSRF (antes de prod).
- E2E com Playwright cobrindo checkout completo.
- PWA (offline-first para catálogo).
- Storybook para a camada `shared/components/ui`.
- Sentry para observabilidade.

### Backend

- Stripe real.
- pgvector em produção.
- Observabilidade: dashboards Grafana pré-configurados.
- Handlers completos no `GlobalExceptionHandler` (403, 405, 409, 500).

### IA

- AssistantService público (chat de suporte).
- Servidor MCP consumível por agentes externos.
- Fine-tuning de classificador de intenção.

---

## Como atualizar este documento

Ao final de cada fase:

1. **Atualize as tabelas de roadmap** (backend/frontend) com o status real.
2. **Adicione uma subseção em "Fases concluídas"** com: endpoints, testes (+N), implementação, decisões.
3. **Registre bugs** encontrados na seção correspondente, com sintoma + correção.
4. **Atualize "Status geral"** com contagem de testes e migrations.
5. **Atualize "Última atualização"** nas Info Gerais.
6. **Commit** com `docs(resumo): atualiza contexto após fase X`.

> ⚠️ **Regra de ouro:** nunca marcar fase como concluída sem `npm run test:run` (frontend) ou `mvn clean test` (backend) verdes.