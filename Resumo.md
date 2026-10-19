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
| Versão atual | **0.16.0-SNAPSHOT** |
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
| Última atualização backend | **Fase 17 — Robustez de Tratamento de Exceções e Segurança** |

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

**Roadmap completo: 17 de 17 fases concluídas.**

- **163 testes** automatizados, todos verdes.
- **22 classes de teste** + 1 config (`PrometheusTestConfig`).
- **~95 arquivos** de produção.
- **9 migrations** Flyway (V1 a V9).
- **Fundação do RAG completa e blindada por testes** (unidade + integração).
- **Tratamento de exceções global padronizado** (400, 401, 403, 404, 405, 409, 500).
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
- **Robustez**: Tratamento de erros padronizado e injeção de segurança otimizada

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
| 17 | Robustez de Exceções e Segurança (GlobalExceptionHandler + Auth) | ✅ Concluído |

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

### Fase 17 — Robustez de Tratamento de Exceções e Segurança

**Testes:** +13 (de 150 para 163, incluindo `GlobalExceptionHandlerTest`).

**Implementação:**
- **`GlobalExceptionHandler` Blindado:** Padronização de respostas JSON para todos os cenários de erro:
  - `400 Bad Request`: `BusinessException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`.
  - `401 Unauthorized`: `UnauthorizedException`.
  - `403 Forbidden`: `AccessDeniedException`.
  - `404 Not Found`: `NotFoundException`, `EntityNotFoundException`.
  - `405 Method Not Allowed`: `HttpRequestMethodNotSupportedException`.
  - `409 Conflict`: `DuplicateEmailException`, `DataIntegrityViolationException`.
  - `500 Internal Server Error`: `Exception` (genérica, com log seguro e mensagem genérica para o cliente).
- **`DuplicateEmailException`:** Nova exceção dedicada para retornar `409 Conflict` quando um e-mail já existe, substituindo o antigo `400 Bad Request`.
- **Otimização de Segurança:** Substituição de `@AuthenticationPrincipal UserDetails` por `@AuthenticationPrincipal String userId` em todos os controllers (`User`, `Cart`, `Order`, `Payment`, `Admin`). Isso elimina `NullPointerException` e alinha perfeitamente com o `JwtAuthFilter`, que injeta o ID como `String` no subject do token.
- **Correção de Testes:** Adicionado `management.health.redis.enabled: false` no `application-test.yml` para evitar falhas de `503 Service Unavailable` nos testes de métricas, já que o ambiente de teste usa cache `simple`.

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


git clone https://github.com/AlvesCosta08/loja-api.git
cd loja-api
cp .env.example .env
./run-dev.sh


./run-dev.sh up        # sobe docker + app
./run-dev.sh down      # derruba serviços
./run-dev.sh reset     # limpa banco e sobe de novo
./run-dev.sh db info   # flyway info
./run-dev.sh db migrate
./run-dev.sh mvn test  # roda os testes


git clone https://github.com/AlvesCosta08/loja-web.git
cd loja-web
npm install
cp .env.example .env
# VITE_API_URL=http://localhost:8080
npm run dev


npx vitest run         # 37 testes
npm run build          # gera dist/

Possíveis extensões futuras
Frontend
Migrar auth para httpOnly cookies + CSRF (antes de prod).
E2E com Playwright cobrindo checkout completo.
PWA (offline-first para catálogo).
Storybook para a camada shared/components/ui.
Sentry para observabilidade.
Backend
Stripe real.
pgvector em produção.
Observabilidade: dashboards Grafana pré-configurados.
IA
AssistantService público (chat de suporte).
Servidor MCP consumível por agentes externos.
Fine-tuning de classificador de intenção.
Como atualizar este documento
Ao final de cada fase:
Atualize as tabelas de roadmap (backend/frontend) com o status real.
Adicione uma subseção em "Fases concluídas" com: endpoints, testes (+N), implementação, decisões.
Registre bugs encontrados na seção correspondente, com sintoma + correção.
Atualize "Status geral" com contagem de testes e migrations.
Atualize "Última atualização" nas Info Gerais.
Commit com docs(resumo): atualiza contexto após fase X.
⚠️ Regra de ouro: nunca marcar fase como concluída sem npm run test:run (frontend) ou mvn clean test (backend) verdes.