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
| Branch em progresso | `feature/rag-foundation` |
| Última atualização backend | Fase 16 — Fundação do RAG (7 ciclos TDD) |

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

**Roadmap completo: 16 de 16 fases concluídas.**

- **147 testes** automatizados, todos verdes.
- **15 classes de teste** + 1 config (`PrometheusTestConfig`).
- **~95 arquivos** de produção.
- **9 migrations** Flyway (V1 a V9).
- **Fundação do RAG completa e blindada por testes** (unidade + integração).

### Frontend

**Roadmap: 0 de 11 fases concluídas.**

Projeto a iniciar. Stack: React 19 + Vite + Tailwind CSS 4.

✅ **CORS configurado no backend.** Bloqueio removido.

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

### Fase 15 — Infraestrutura de Desenvolvimento

**Endpoints afetados:** Nenhum endpoint novo, mas todos agora lidam com CORS.

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

*(Nenhuma ainda. Primeira fase prevista: Setup Vite + React + Tailwind + layout base.)*

---

## Pré-requisitos para iniciar o frontend

Antes da Fase 0 do frontend, é necessário:

1. **Configurar CORS no backend.**
   ✅ **Estado atual:** O `CorsConfig` existe e o `SecurityConfig` está configurado. O frontend já pode conectar.
2. **Node.js** versão 20 LTS ou superior.
3. **Nenhum CLI global necessário** — usar `npm create vite@latest`.
4. **Estratégia de autenticação:** `localStorage` (simples) — decisão provisória, revisar antes do deploy.

---

## Estrutura prevista do projeto frontend

*(Mantido idêntico ao original)*

---

## Contrato da API (referência para o frontend)

*(Mantido idêntico ao original. Quando o `AssistantController` for criado na fase seguinte, adicionar a seção `/assistant/*` aqui.)*

---

## Decisões técnicas — Backend

| Decisão | Por quê |
|---------|---------|
| ... | ... |
| `MethodArgumentNotValidException` → mensagem concatenada `campo: erro` | Feedback único e legível |
| **`.env` + `run-dev.sh`** | **Centraliza variáveis sem hardcode. O script injeta no shell antes do Maven rodar.** |
| **Docker Compose com Healthcheck** | **Garante que o Spring Boot só suba após o Postgres estar aceitando conexões (`pg_isready`).** |
| **Flyway Maven Plugin via `run-dev.sh`** | **Permite rodar `./run-dev.sh db info` e `./run-dev.sh db migrate` isoladamente.** |
| **`FakeEmbeddingProvider` em `src/main`** | **Determinístico, sem custo de API, reutilizável em testes e dev** |
| **`InMemoryVectorStore` como implementação padrão** | **Permite TDD rápido. `PgVectorStore` entra na fase final do RAG** |
| **Eventos de domínio para indexação** | **Desacopla `ProductService` de `ProductIndexer`. Reaproveita o padrão do cache** |
| **Um único `@EventListener` despachando por `active`** | **Evita o bug de dois listeners recebendo o mesmo evento e mutando o índice em sentidos opostos** |
| **`threshold` no `VectorRetriever`** | **Descarta resultados com baixa similaridade, reduz ruído no contexto do LLM** |

---

## Decisões técnicas — Frontend

*(A confirmar durante a Fase 0)*

Decisões provisórias: *(Mantidas idênticas ao original)*

---

## Pontos de atenção herdados do backend

1-14. *(Mantidos idênticos ao original)*
15. ✅ **CORS configurado** — `http://localhost:5173` permitido.
16-20. *(Mantidos idênticos ao original)*
21. **Testes de CORS com MockMvc dão falso positivo.** `@AutoConfigureMockMvc` não reproduz o preflight real do Tomcat. Sempre valide CORS com `curl` contra a app rodando.
22. **`@EventListener` múltiplos no mesmo tipo de evento são perigosos.** Todos os métodos são chamados para todos os eventos. Prefira **um único listener** que despacha por estado/campo. Sempre cubra com teste de integração (`ApplicationEventPublisher` real), não só unitário.

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
| 9 | `CorsConfigTest` passava mesmo sem CORS em produção | Falso positivo do `MockMvc`. Validação real via `curl` + criação do `CorsConfig` |
| 10 | **`ProductIndexer` com `@Component` sem beans de dependência** quebrava o contexto de todos os `@SpringBootTest` | Criar `AssistantConfig` registrando `EmbeddingProvider` e `VectorStore` como beans |
| 11 | **`ProductIndexer` com dois `@EventListener`** apagava produtos recém-criados do índice (bug crítico de RAG) | Consolidar em um único `onProductEvent()` que despacha por `active`. Coberto por teste de integração |
| 12 | `.class` corrompido pelo editor quebrava `@InjectMocks` (Flexible Constructor Bodies) | Sempre rodar `mvn clean test` após alterar `ProductService`. Documentado para futuras sessões |

---

## Bugs encontrados e correções — Frontend

*(Nenhum ainda — projeto a iniciar.)*

---

## Como executar — Backend

### Pré-requisitos

- JDK 21
- Docker + Docker Compose (para Postgres e Redis)
- Maven Wrapper (incluído no projeto)

### Passos (Desenvolvimento)

```bash
# 1. Clonar
git clone https://github.com/AlvesCosta08/loja-api.git
cd loja-api

# 2. Criar variáveis de ambiente
cp .env.example .env
# Edite .env se necessário (as senhas padrão são 'postgres')

# 3. Rodar a aplicação
./run-dev.sh