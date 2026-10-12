# Loja API — Contexto do Projeto

Documento de referência entre sessões de desenvolvimento.
Atualizado ao final de cada fase concluída.

---

## Informações gerais

### Backend

| Campo | Valor |
|-------|-------|
| Projeto | loja-api |
| Descrição | API REST de e-commerce com autenticação, catálogo, carrinho, pedidos, cupons, frete e pagamentos |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 |
| Abordagem | TDD estrito (Red → Green → Refactor) |
| Autenticação | JWT HS256 (JJWT 0.12.6) |
| Banco — produção | PostgreSQL |
| Banco — testes | H2 em memória |
| Cache — produção | Redis |
| Cache — testes | ConcurrentMapCacheManager |
| Métricas | Micrometer + Prometheus |
| Migrações | Flyway |
| Build | Maven |
| Boilerplate | Lombok |
| Pacote base | `com.alvescosta.loja` |
| Repositório backend | https://github.com/AlvesCosta08/loja-api |
| Última atualização backend | Fase 14 — Dashboard de métricas |

### Frontend

| Campo | Valor |
|-------|-------|
| Projeto | loja-web |
| Descrição | SPA de e-commerce que consome a `loja-api` |
| Framework | Angular 19 (standalone components) |
| Linguagem | TypeScript 5.6 |
| Estilização | Tailwind CSS 4 |
| Estado | Angular Signals + Services |
| HTTP | `HttpClient` + `HttpInterceptor` (JWT) |
| Rotas | `CanActivate` (cliente) + `CanMatch` (admin) |
| Formulários | Reactive Forms |
| Testes | Jasmine + Karma (unit) + Cypress (E2E) |
| Build | Angular CLI 19 |
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

116 testes automatizados, todos verdes.
77 arquivos de produção, 12 classes de teste, 9 migrations Flyway.

### Frontend

**Roadmap: 0 de 10 fases concluídas.**

Projeto a iniciar. Stack: Angular 19 + Tailwind CSS 4.

---

## Objetivo do projeto

Construir um e-commerce full-stack cobrindo:

**Backend** (concluído)

- Autenticação e autorização com JWT e roles
- Catálogo de produtos com filtros e paginação
- Carrinho persistente
- Checkout com snapshot de preços
- Integração com gateways de pagamento
- Cupons, frete e painel administrativo
- Cache com Redis e dashboard de métricas

**Frontend** (a construir)

- Autenticação com JWT (login, cadastro, guard de rotas)
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
| 0 | Setup do projeto | Concluído |
| 1 | Cadastro de usuário (`POST /auth/register`) | Concluído |
| 2 | Login + JWT (`POST /auth/login`) | Concluído |
| 3 | Usuário logado (`GET /users/me`) | Concluído |
| 4 | CRUD de produtos (`/products`) | Concluído |
| 5 | Categorias e busca | Concluído (absorvido pela Fase 4) |
| 6 | Carrinho (`/cart`) | Concluído |
| 7 | Checkout e pedidos (`/orders`) | Concluído |
| 8 | Cupons de desconto (`/coupons`) | Concluído |
| 9 | Cálculo de frete (`/shipping`) | Concluído |
| 10 | Pagamento Pix (`/payments`) | Concluído |
| 11 | Pagamento com cartão (Stripe) | Concluído |
| 12 | Painel administrativo (`/admin`) | Concluído |
| 13 | Cache Redis no catálogo | Concluído |
| 14 | Dashboard de métricas | Concluído |

---

## Roadmap — Frontend

| # | Fase | Status |
|---|------|--------|
| 0 | Setup Angular + CORS no backend + layout base | Pendente |
| 1 | Roteamento + guards + interceptor JWT | Pendente |
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

Configuração:

- `pom.xml` com Spring Boot 3.4 e Java 21
- Dependências: Web, Security, Data JPA, Cache, Redis, Validation, Actuator, Micrometer Prometheus, Flyway, JJWT 0.12.6, PostgreSQL, H2 (testes), Lombok
- `application.yml` (produção) e `application-test.yml` (perfil de teste)
- `@EnableMethodSecurity` habilitado

Ambiente local:

- JDK 21 em `/usr/lib/jvm/java-21-openjdk-amd64`
- `JAVA_HOME` configurado em `~/.bashrc`
- Maven Wrapper 3.10.0 via `./mvnw`

---

### Fase 1 — Cadastro de usuário

Endpoints:
- `POST /auth/register` → 201

Testes: 3
Implementação: `domain/User`, `domain/Role`, `dto/UserCreateRequest` com Bean Validation, `dto/UserResponse` sem senha, `UserService.register()` com BCrypt, `AuthController`, `GlobalExceptionHandler`.

---

### Fase 2 — Login + JWT

Endpoints:
- `POST /auth/login` → 200 com `access_token`

Testes: 3
Implementação: `JwtService` com HS256, `UserService.authenticate()`, `LoginRequest`, `TokenResponse`, `UnauthorizedException`. Parâmetros do JWT externalizados em `application.yml`.

---

### Fase 3 — Usuário logado

Endpoints:
- `GET /users/me` → 200

Testes: 3
Implementação: `JwtAuthFilter`, `UserDetailsServiceImpl` (username = ID), `RestAuthenticationEntryPoint` (401 em vez de 403), `UserController.me()`.

---

### Fase 4 — CRUD de Produtos

Endpoints:
- `GET /products` (paginado, filtros `category` e `search`)
- `GET /products/{id}`
- `POST /products` (ADMIN)
- `PUT /products/{id}` (ADMIN)
- `DELETE /products/{id}` (ADMIN)

Testes: 14
Implementação: `Product` com `BigDecimal`, `ProductService`, `ProductController` com `@PreAuthorize`, `@PageableDefault(size=10, sort="name")`.

---

### Fase 6 — Carrinho de Compras

Endpoints:
- `POST /cart/items`, `GET /cart`, `PUT /cart/items/{id}`, `DELETE /cart/items/{id}`, `DELETE /cart`

Testes: 14
Implementação: `CartItem` com unique `(user_id, product_id)`, `@OnDelete(CASCADE)`, `CartService`, `CartCalculator`, `CartController`.

---

### Fase 7 — Checkout e Pedidos

Endpoints:
- `POST /orders`, `GET /orders`, `GET /orders/{id}`, `PATCH /orders/{id}/status`

Testes: 12
Implementação: `Order`, `OrderItem` com snapshot de `productName` e `unitPrice`, `OrderStatus`, `OrderService` validando estoque antes de mutar.

---

### Fase 8 — Cupons de Desconto

Endpoints:
- `POST /coupons` (ADMIN), `GET /coupons/{code}`

Testes: 12
Implementação: `Coupon` com coluna `discount_value` (`value` é palavra reservada), `CouponService` com validação de expiração, limite e valor mínimo.

---

### Fase 9 — Cálculo de Frete

Endpoints:
- `POST /shipping/quote`, `POST /orders?cep=...`

Testes: 10
Implementação: `ShippingService` com tabela por prefixo de CEP, frete grátis acima de R$ 200.

---

### Fase 10 — Pagamento Pix

Endpoints:
- `POST /payments?orderId=...`, `GET /payments/{id}`, `POST /payments/{id}/confirm`

Testes: 8
Implementação: `Payment`, `PaymentMethod`, `PaymentStatus`, `PixGenerator` (mock EMV + QR), `PaymentService`.

---

### Fase 11 — Pagamento com cartão (Stripe)

Endpoints:
- `POST /payments/card?orderId=...`, `POST /webhooks/stripe`

Testes: 9
Implementação: `PaymentGateway` (interface), `StripePaymentGateway` (mock), `WebhookController`, idempotência no webhook.

---

### Fase 12 — Painel administrativo

Endpoints (todos ADMIN):
- `GET /admin/dashboard`, `GET /admin/orders`, `GET /admin/users`, `PATCH /admin/users/{id}/role`, `GET /admin/products/low-stock`, `POST /admin/products/{id}/restock`

Testes: 12
Implementação: `AdminService`, queries agregadas com `@Query`, guarda-corpo contra auto-rebaixamento.

---

### Fase 13 — Cache Redis no catálogo

Comportamento idêntico ao anterior; ganho de performance em consultas repetidas.

Testes: 8
Implementação: `spring-boot-starter-cache` + `spring-boot-starter-data-redis`, `CacheConfig` com `@EnableCaching`, `@Cacheable` em `findById` e `list`, `@CacheEvict` em `create`/`update`/`delete`, dois caches separados (`products`, `productLists`).

---

### Fase 14 — Dashboard de métricas

Endpoints Actuator:
- `GET /actuator/health` (público)
- `GET /actuator/info` (público)
- `GET /actuator/metrics` (autenticado)
- `GET /actuator/prometheus` (autenticado)

Métricas customizadas:
- `loja.orders.created` — incrementa em cada checkout
- `loja.payments.confirmed` — incrementa em cada confirmação de pagamento

Testes: 7
Implementação: `Micrometer + Prometheus`, `MetricsService` com contadores, `PrometheusMetricsConfig` (contorna bug de auto-config do Spring Boot 3.4 quando há injeção precoce do `MeterRegistry`), instrumentação em `OrderService` e `PaymentService`.

---

## Fases concluídas — Frontend

*(Nenhuma ainda. Primeira fase prevista: Setup Angular + CORS no backend.)*

---

## Pré-requisitos para iniciar o frontend

Antes da Fase 0 do frontend, é necessário:

1. **Configurar CORS no backend** — hoje o `SecurityConfig` não permite requisições de `http://localhost:4200`. Será adicionado um `CorsConfig` no backend.
2. **Node.js instalado** — versão 20 LTS ou superior (`node --version`).
3. **Angular CLI instalado globalmente** — `npm install -g @angular/cli@19`.
4. **Definir estratégia de autenticação** — `localStorage` para o JWT (simples) ou cookies httpOnly (mais seguro, exige mais ajustes).

---

## Estrutura prevista do projeto frontend

```
loja-web/
├── package.json
├── angular.json
├── tsconfig.json
├── tailwind.config.js
├── .env
├── .gitignore
└── src/
    ├── main.ts
    ├── index.html
    ├── styles.css
    ├── environments/
    │   ├── environment.ts
    │   └── environment.prod.ts
    └── app/
        ├── app.config.ts
        ├── app.routes.ts
        ├── app.component.ts
        ├── core/
        │   ├── services/         (auth, api, cart, order, product)
        │   ├── interceptors/     (jwt, error)
        │   ├── guards/           (auth, admin, guest)
        │   └── models/           (interfaces espelhando DTOs)
        ├── shared/
        │   ├── components/       (header, footer, loading, toast)
        │   └── pipes/            (currency-brl, cpf, cep)
        └── features/
            ├── auth/             (login, register)
            ├── catalog/          (list, detail)
            ├── cart/             (cart-page)
            ├── checkout/         (checkout-page)
            ├── payment/          (pix, card)
            ├── orders/           (list, detail)
            └── admin/            (dashboard, products, orders, users)
```

---

## Contrato da API (referência para o frontend)

### Autenticação

| Método | Endpoint | Auth |
|--------|----------|:----:|
| `POST` | `/auth/register` | público |
| `POST` | `/auth/login` | público |
| `GET` | `/users/me` | usuário |

### Catálogo

| Método | Endpoint | Auth |
|--------|----------|:----:|
| `GET` | `/products?category=&search=&page=&size=` | público |
| `GET` | `/products/{id}` | público |
| `POST` | `/products` | ADMIN |
| `PUT` | `/products/{id}` | ADMIN |
| `DELETE` | `/products/{id}` | ADMIN |

### Carrinho

| Método | Endpoint |
|--------|----------|
| `POST` | `/cart/items` |
| `GET` | `/cart` |
| `PUT` | `/cart/items/{id}` |
| `DELETE` | `/cart/items/{id}` |
| `DELETE` | `/cart` |

### Pedidos, cupons, frete e pagamento

| Método | Endpoint | Auth |
|--------|----------|:----:|
| `POST` | `/orders?couponCode=&cep=` | usuário |
| `GET` | `/orders` | usuário |
| `GET` | `/orders/{id}` | dono/ADMIN |
| `PATCH` | `/orders/{id}/status` | ADMIN |
| `POST` | `/coupons` | ADMIN |
| `GET` | `/coupons/{code}` | público |
| `POST` | `/shipping/quote` | público |
| `POST` | `/payments?orderId=` | dono |
| `GET` | `/payments/{id}` | dono |
| `POST` | `/payments/{id}/confirm` | ADMIN |
| `POST` | `/payments/card?orderId=` | dono |
| `POST` | `/webhooks/stripe` | público |

### Admin

| Método | Endpoint |
|--------|----------|
| `GET` | `/admin/dashboard` |
| `GET` | `/admin/orders` |
| `GET` | `/admin/users` |
| `PATCH` | `/admin/users/{id}/role` |
| `GET` | `/admin/products/low-stock?threshold=` |
| `POST` | `/admin/products/{id}/restock` |

### Observabilidade

| Método | Endpoint | Auth |
|--------|----------|:----:|
| `GET` | `/actuator/health` | público |
| `GET` | `/actuator/info` | público |
| `GET` | `/actuator/metrics` | autenticado |
| `GET` | `/actuator/prometheus` | autenticado |

---

## Decisões técnicas — Backend

| Decisão | Motivo |
|---------|--------|
| `BigDecimal` para dinheiro | Evita erro de arredondamento de `double` |
| H2 em memória nos testes | Execução rápida e isolada |
| Flyway para produção | Schema versionado e reproduzível |
| `record` para DTOs | Imutabilidade e menos boilerplate |
| `@EnableMethodSecurity` | Sem isso, `@PreAuthorize` é ignorado silenciosamente |
| JWT stateless | Escala horizontal sem sessão no servidor |
| `@OnDelete(CASCADE)` em FKs | Deleção em cascata consistente entre H2 e PostgreSQL |
| `@OnDelete(SET_NULL)` em `OrderItem.product` | Produto pode ser removido sem perder histórico |
| Snapshot de preços em `OrderItem` | Pedido preserva o valor pago mesmo se o produto mudar |
| Unique `(user_id, product_id)` em `CartItem` | Um item por produto por usuário |
| Coluna `discount_value` no cupom | `value` é palavra reservada SQL |
| Username do `UserDetails` é o ID | Subject do JWT é o ID do usuário |
| `RestAuthenticationEntryPoint` | Responde 401 em vez do 403 padrão do Spring |
| Frete opcional no checkout | Sem `?cep=`, `shippingCost` fica zero (retrocompatível) |
| `total = subtotal − discount + shippingCost` | Fórmula canônica do checkout |
| `pixQrCodeBase64` como TEXT | Imagem em base64 não cabe em `VARCHAR(255)` |
| `PixGenerator` como mock | Isola regra de negócio do gateway externo |
| Expiração do Pix | Evita QR Code "zumbi" |
| `PaymentGateway` como interface | Trocar PIX/Stripe/Mercado Pago sem alterar controller |
| Idempotência no webhook | Stripe reenvia em timeout; pagamento já `CONFIRMED` não reprocessa |
| `@PreAuthorize` na classe do AdminController | Todos os métodos herdam a restrição de role |
| Guarda-corpo de role | ADMIN não pode alterar a própria role (evita lockout) |
| `spring-boot-starter-cache` obrigatório | Sem ele, nenhum `CacheManager` é criado, `@Cacheable` fica mudo |
| Dois caches separados (`products` e `productLists`) | Invalidação independente por tipo de consulta |
| Limpar cache no `@BeforeEach` dos testes | Evita vazamento de dados entre testes |
| `PrometheusMetricsConfig` explícito | Contorna bug de auto-config do Spring Boot 3.4 + Micrometer 1.14 |
| Métricas de negócio (`loja.orders.created`) | Mais úteis que métricas de infraestrutura para operação |
| Health do Redis desabilitado nos testes | Testes não devem depender de serviço externo |

---

## Decisões técnicas — Frontend

*(A definir conforme as fases forem sendo concluídas.)*

Decisões iniciais previstas:

| Decisão | Motivo |
|---------|--------|
| Angular 19 com standalone components | Padrão atual; menos boilerplate que NgModule |
| Tailwind CSS 4 | Utility-first; consistência visual sem CSS solto |
| Angular Signals para estado | Reatividade granular, sem RxJS para UI simples |
| `HttpInterceptor` funcional | Injeta JWT em toda requisição autenticada |
| `CanActivate` para auth, `CanMatch` para admin | Rotas protegidas por role |
| Reactive Forms | Validação tipada, melhor UX |
| `localStorage` para JWT | Simplicidade; ponto de migração futura para cookie httpOnly |
| Lazy loading por feature | Bundle inicial pequeno |
| Environment files | URL da API varia entre dev e prod |

---

## Bugs encontrados e correções — Backend

| Fase | Sintoma | Causa | Correção |
|------|---------|-------|----------|
| 6 | `Referential integrity constraint violation` ao deletar usuário ou produto | `CartItem` sem cascade nas FKs | `@OnDelete(action = CASCADE)` nas duas FKs |
| 7 | Mesmo erro, agora em `ORDER_ITEMS` | `OrderItem.order` sem cascade | `@OnDelete(action = CASCADE)` na FK |
| 7 | `naoDeveAcessarPedidoDeOutroUsuario` retornava 200 | Teste usava token de ADMIN, que tem permissão | Teste passa a usar outro CUSTOMER |
| 8 | `Syntax error ... c1_0.[*]value` no H2 | `VALUE` é palavra reservada SQL | `@Column(name = "discount_value")` |
| 9 | Teste esperava `total = 1000` e recebeu `2500` | Produto do `setup()` custa R$ 2500 | Fixtures pequenas criadas dentro de cada teste |
| 10 | `deveCriarPagamentoPix` esperava `amount = 60` e recebeu `50` | Helper não passava `?cep=`, logo sem frete | Revisar o helper antes de calcular valores esperados |
| 12 | `adminListaUsuariosComTotalGasto` falhava em `$.content` | `/admin/users` retorna array simples; `/admin/orders` é que é paginado | Ajustar teste para `$` em vez de `$.content` |
| 13 | `No qualifying bean of type 'CacheManager'` | Faltava `spring-boot-starter-cache` no `pom.xml` | Adicionar a dependência |
| 13 | `ProductCacheTest` falhava — `findById` chamado mesmo após cache | Faltavam `@Cacheable`/`@CacheEvict` no `ProductService` | Aplicar as anotações |
| 13 | `ProductControllerTest.qualquerPessoaPodeListarProdutos` falhava com dados antigos | Cache entre testes (mesmo contexto Spring) | Limpar cache no `@BeforeEach` |
| 14 | `/actuator/prometheus` retornava 404 | `PrometheusMeterRegistry` não era criado pela auto-config | `PrometheusMetricsConfig` explícito |
| 14 | `/actuator/health` retornava 503 | Redis health check falhava em ambiente sem Redis | `management.health.redis.enabled=false` no perfil de teste |
| 14 | `/actuator/prometheus` retornava 200 com body vazio | Registry manual não era o mesmo usado pelo `MetricsService` | Testar via `/actuator/metrics/{nome}` em vez do scrape Prometheus |

---

## Como executar — Backend

```bash
# Todos os testes
./mvnw test

# Limpar e executar (recomendado após mudança de schema)
./mvnw clean test

# Classe específica
./mvnw test -Dtest=ProductControllerTest

# Subir dependências (PostgreSQL + Redis)
docker compose up -d

# Subir a aplicação
./mvnw spring-boot:run

# Consultar saúde e métricas
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus   # requer token
```

## Como executar — Frontend *(a criar)*

```bash
# Instalar dependências
npm install

# Servidor de desenvolvimento (http://localhost:4200)
npm start

# Build de produção
npm run build

# Testes unitários
npm test

# Testes E2E
npm run e2e
```

---

## Possíveis extensões futuras

**Backend:**

- Refresh token para JWT
- Rate limiting nas rotas de auth e webhook
- E-mail transacional (confirmação de pedido)
- Testcontainers substituindo H2 por PostgreSQL real
- Paginação padronizada com `PagedModel` (`@EnableSpringDataWebSupport`)
- Job agendado para expirar pagamentos Pix
- Deploy em cloud (Render, Railway, Fly.io)
- Documentação automática com SpringDoc OpenAPI
- Multi-tenancy (múltiplas lojas em uma instância)

**Frontend:**

- PWA (funciona offline para catálogo)
- Internacionalização (pt-BR, en, es)
- Tema escuro
- Wishlist (lista de desejos)
- Comparador de produtos
- Reviews e avaliações
- Chat de suporte
- SEO com Angular Universal (SSR)

**Full-stack:**

- Deploy coordenado (backend em Railway + frontend em Vercel)
- Monitoramento com Grafana + Prometheus
- CI/CD com preview deploys por PR

---

## Como atualizar este documento

Ao concluir uma nova fase (backend ou frontend):

1. Marcar a fase na tabela do roadmap correspondente e atualizar o contador
2. Adicionar um bloco em "Fases concluídas" seguindo o formato das fases anteriores
3. Atualizar a seção "Estrutura prevista" (se aplicável)
4. Atualizar a tabela "Decisões técnicas" com o que surgiu de novo
5. Registrar novos bugs na tabela de bugs encontrados, se houver