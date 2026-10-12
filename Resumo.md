# Loja API — Contexto do Projeto

Documento de referência entre sessões de desenvolvimento.
Atualizado ao final de cada fase concluída.

---

## Informações gerais

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
| Repositório | https://github.com/AlvesCosta08/loja-api |
| Autor | Alves Costa |
| Última atualização | Fase 14 — Dashboard de métricas |

---

## Status

**Roadmap completo: 14 de 14 fases concluídas.**

116 testes automatizados, todos verdes.
77 arquivos de produção, 12 classes de teste, 9 migrations Flyway.

---

## Objetivo

Construir uma API de e-commerce robusta seguindo TDD estrito, cobrindo:

- Autenticação e autorização com JWT e roles
- Catálogo de produtos com filtros e paginação
- Carrinho persistente
- Checkout com snapshot de preços
- Integração com gateways de pagamento
- Cupons, frete e painel administrativo
- Cache com Redis e dashboard de métricas

---

## Roadmap

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

## Fases concluídas

### Fase 0 — Setup

Estrutura de pacotes criada em `com.alvescosta.loja`:

```
config/       → SecurityConfig
domain/       → entidades JPA
dto/          → records de request/response
exception/    → exceções customizadas e handler global
repository/   → interfaces Spring Data JPA
security/     → JwtService, JwtAuthFilter, UserDetails
service/      → regras de negócio
controller/   → endpoints REST
```

Configuração:

- `pom.xml` com Spring Boot 3.4 e Java 21
- Dependências: Web, Security, Data JPA, Validation, Actuator, Flyway, JJWT 0.12.6, PostgreSQL, H2 (testes), Lombok
- `application.yml` (produção) e `application-test.yml` (H2 em memória)
- `@EnableMethodSecurity` habilitado

Ambiente local:

- JDK 21 em `/usr/lib/jvm/java-21-openjdk-amd64`
- `JAVA_HOME` configurado em `~/.bashrc`
- Maven Wrapper 3.10.0 via `./mvnw`

---

### Fase 1 — Cadastro de usuário

Endpoints:
- `POST /auth/register` → 201

Testes: 3, todos passando
- `deveRegistrarUsuarioComSucesso`
- `naoDeveRegistrarEmailDuplicado`
- `naoDeveAceitarSenhaCurta`

Implementação:
- `domain/User.java`, `domain/Role.java`
- `dto/UserCreateRequest.java` com Bean Validation
- `dto/UserResponse.java` sem expor senha
- `repository/UserRepository.existsByEmail()`
- `service/UserService.register()` com BCrypt
- `controller/AuthController.register()`
- `exception/GlobalExceptionHandler`

Refactor:
- Tratamento de erros centralizado com `@RestControllerAdvice`

---

### Fase 2 — Login + JWT

Endpoints:
- `POST /auth/login` → 200 com `access_token`

Testes: 3, todos passando
- `deveLogarERetornarToken`
- `deveRejeitarSenhaErrada` → 401
- `naoDeveLogarUsuarioInexistente` → 401

Implementação:
- `security/JwtService.java` com `generate`, `extractUserId`, `getExpirationMs`
- `service/UserService.authenticate()` com `PasswordEncoder.matches()`
- `dto/LoginRequest.java`, `dto/TokenResponse.java`
- `controller/AuthController.login()`
- `exception/UnauthorizedException.java`

Refactor:
- `jwt.secret` e `jwt.expiration-ms` externalizados em `application.yml`
- `jwt.secret` aceita variável de ambiente `JWT_SECRET`
- `UnauthorizedException` mapeada para 401 no handler global

---

### Fase 3 — Usuário logado

Endpoints:
- `GET /users/me` → 200

Testes: 3, todos passando
- `deveRetornar401SemToken`
- `deveRetornar401ComTokenInvalido`
- `deveRetornarDadosDoUsuarioLogado`

Implementação:
- `security/JwtAuthFilter.java` — valida Bearer e popula o `SecurityContext`
- `security/UserDetailsServiceImpl.java` — username é o ID do usuário
- `security/RestAuthenticationEntryPoint.java` — responde 401 em vez de 403 default
- `config/SecurityConfig.java` — sessão stateless, CSRF desabilitado, filtro JWT registrado antes do filtro padrão do Spring
- `controller/UserController.me()` com `@AuthenticationPrincipal`

---

### Fase 4 — CRUD de Produtos

Endpoints:
- `GET /products` — lista paginada com `category` e `search`
- `GET /products/{id}` — detalhes ou 404
- `POST /products` — 201, requer role ADMIN
- `PUT /products/{id}` — 200, requer role ADMIN
- `DELETE /products/{id}` — 204, requer role ADMIN

Testes: 14, todos passando

Implementação:
- `domain/Product.java` com `BigDecimal` para preço
- `dto/ProductRequest.java`, `dto/ProductResponse.java`
- `repository/ProductRepository.java` com métodos derivados de filtro
- `service/ProductService.java`
- `controller/ProductController.java` com `@PreAuthorize("hasRole('ADMIN')")` em escrita
- `@PageableDefault(size = 10, sort = "name")` na listagem

Refactor:
- `@EnableMethodSecurity` no `SecurityConfig`
- Helper `getOrThrow(id)` para evitar duplicação

---

### Fase 6 — Carrinho de Compras

Endpoints:
- `POST /cart/items` — 201
- `GET /cart` — 200
- `PUT /cart/items/{id}` — 200
- `DELETE /cart/items/{id}` — 204
- `DELETE /cart` — 204

Testes: 14, todos passando

Implementação:
- `domain/CartItem.java` com `@ManyToOne` para `User` e `Product`
- Restrição única `(user_id, product_id)` e `@Min(1)` na quantidade
- `@OnDelete(CASCADE)` nas duas FKs
- `dto/AddToCartRequest.java`, `UpdateCartItemRequest.java`, `CartItemResponse.java`, `CartResponse.java`
- `repository/CartItemRepository.java`
- `service/CartService.java` com `addItem`, `getCart`, `updateQuantity`, `removeItem`, `clearCart`
- `service/CartCalculator.java` para cálculo de total
- `controller/CartController.java`

Refactor:
- `@Transactional` em escrita, `@Transactional(readOnly = true)` em leitura
- `CHECK (quantity >= 1)` no banco
- Mapeamento centralizado em `CartItemResponse.from(CartItem)`

---

### Fase 7 — Checkout e Pedidos

Endpoints:
- `POST /orders` — 201
- `GET /orders` — 200, apenas do usuário logado
- `GET /orders/{id}` — 200 (dono ou ADMIN), 403 caso contrário
- `PATCH /orders/{id}/status` — 200, requer ADMIN

Testes: 12, todos passando

Implementação:
- `domain/OrderStatus.java`
- `domain/Order.java` com cascade em `user`, campos `discount`, `couponCode`, `shippingCost`
- `domain/OrderItem.java` com snapshot de `productName` e `unitPrice`
- `dto/OrderResponse.java`, `OrderItemResponse.java`, `UpdateOrderStatusRequest.java`
- `repository/OrderRepository.java`
- `service/OrderService.java`
- `controller/OrderController.java`

Decisões:
- Snapshot de preços em `OrderItem`: pedido antigo mantém valor histórico mesmo se o produto mudar
- Validação de estoque antes de qualquer mutação
- Decremento de estoque no checkout, não no pagamento
- `@OnDelete(SET_NULL)` em `OrderItem.product` para preservar histórico

---

### Fase 8 — Cupons de Desconto

Endpoints:
- `POST /coupons` — 201, requer ADMIN
- `GET /coupons/{code}` — 200, público

Testes: 12, todos passando

Implementação:
- `domain/DiscountType.java` com `PERCENTAGE` e `FIXED`
- `domain/Coupon.java` — coluna SQL `discount_value` porque `value` é palavra reservada
- `dto/CouponRequest.java`, `CouponResponse.java`
- `repository/CouponRepository.java`
- `service/CouponService.java` com validação de expiração, limite de usos e valor mínimo
- `controller/CouponController.java`
- `OrderService.checkout()` passa a aceitar `couponCode` opcional

Decisões:
- `usageCount` incrementado somente após sucesso — falha não consome cupom
- `expiresAt` em `Instant` (UTC)
- `minOrderValue` e `maxUses` opcionais
- Aplicação do cupom no checkout, não no carrinho

---

### Fase 9 — Cálculo de Frete

Endpoints:
- `POST /shipping/quote` — 200, público
- `POST /orders?cep=...` — 201 com frete aplicado

Testes: 10, todos passando

Implementação:
- `domain/ShippingType.java` com `STANDARD` e `EXPRESS`
- `dto/ShippingQuoteRequest.java`, `ShippingOption.java`, `ShippingQuoteResponse.java`
- `service/ShippingService.java`
- `controller/ShippingController.java`
- Campo `shippingCost` em `Order` e em `OrderResponse`
- `OrderService.checkout(userId, couponCode, cep)`
- Migration `V7__alter_orders_add_shipping.sql`

Tabela de preços por prefixo de CEP:
- `0`, `1` → São Paulo: STANDARD R$ 10, EXPRESS R$ 20
- `2`, `3` → Rio de Janeiro / Espírito Santo: STANDARD R$ 15, EXPRESS R$ 30
- Outros → STANDARD R$ 20, EXPRESS R$ 40

Regras:
- STANDARD gratuito se subtotal ≥ R$ 200
- EXPRESS nunca gratuito
- Fórmula: `total = subtotal − discount + shippingCost`
- Frete opcional: sem parâmetro `cep`, `shippingCost` fica em zero

---

### Fase 10 — Pagamento Pix

Endpoints:
- `POST /payments?orderId=...` — 201, dono ou ADMIN
- `GET /payments/{id}` — 200, dono ou ADMIN
- `POST /payments/{id}/confirm` — 200, requer ADMIN

Testes: 8, todos passando

Implementação:
- `domain/PaymentMethod.java` com `PIX` e `CREDIT_CARD`
- `domain/PaymentStatus.java` com `PENDING`, `CONFIRMED`, `FAILED`, `EXPIRED`
- `domain/Payment.java` com cascade em `order`
- `dto/PaymentResponse.java`
- `repository/PaymentRepository.java`
- `service/PixGenerator.java` — payload EMV mock e QR em base64
- `service/PaymentService.java`
- `controller/PaymentController.java`
- Migration `V8__create_payments.sql`

Decisões:
- `pixQrCodeBase64` como coluna TEXT
- Expiração do Pix em 30 minutos
- Pedido transiciona para `PAID` apenas após confirmação administrativa
- `PixGenerator` é mock determinístico. Em produção vira chamada HTTP ao PSP.

---

### Fase 11 — Pagamento com cartão (Stripe)

Endpoints:
- `POST /payments/card?orderId=...` — 201, dono ou ADMIN
- `POST /webhooks/stripe` — 200, público (exige header `Stripe-Signature`)

Testes: 9, todos passando
- `deveCriarPaymentIntent`
- `naoDeveCriarSemToken` → 401
- `deveRetornar404` → pedido inexistente
- `naoDevePagarPedidoDeOutroUsuario` → 403
- `naoDevePagarPedidoJaPago` → 400
- `webhookDeveConfirmarPagamento`
- `webhookSemAssinaturaDeveFalhar` → 400
- `webhookDuplicadoNaoDeveRepagar` — idempotência
- `donoBuscaPaymentIntent`

Implementação:
- `service/PaymentGateway.java` — interface comum (`createIntent`)
- `service/StripePaymentGateway.java` — implementação mock determinística
- `service/PaymentService.java` — métodos `createCard` e `handleGatewayWebhook`
- `controller/WebhookController.java` — endpoint público `/webhooks/stripe`
- `domain/Payment.java` — campos `gatewayRef` e `clientSecret`
- `dto/PaymentResponse.java` — expõe os campos novos
- `repository/PaymentRepository.java` — `findByGatewayRef`
- Migration `V9__alter_payments_add_gateway_fields.sql`

Decisões:
- Idempotência no webhook: pagamento já `CONFIRMED` retorna 200 sem reprocessar
- `PaymentGateway` como interface permite trocar por Mercado Pago ou PagSeguro sem alterar o controller
- `gatewayRef` indexado — conciliação rápida em produção
- `clientSecret` nunca é logado
- Endpoint de webhook é público mas exige o header de assinatura

---

### Fase 12 — Painel administrativo

Endpoints (todos com `@PreAuthorize("hasRole('ADMIN')")`):
- `GET /admin/dashboard` — totais de vendas, ticket médio, nº de pedidos
- `GET /admin/orders` — todos os pedidos, paginado
- `GET /admin/users` — clientes com total gasto e contagem de pedidos
- `PATCH /admin/users/{id}/role` — promove/rebaixa usuário
- `GET /admin/products/low-stock?threshold=5` — produtos com estoque abaixo do limite
- `POST /admin/products/{id}/restock` — incrementa estoque

Testes: 12, todos passando
- Autorização: `deveRetornar401SemToken`, `clienteNaoPodeAcessarDashboard`, `clienteNaoPodeListarUsuarios`
- Dashboard: `dashboardZerado`, `dashboardComPedidos`
- Listagens: `adminListaTodosOsPedidos`, `adminListaUsuariosComTotalGasto`
- Roles: `adminDevePromoverUsuario`, `adminNaoPodeRebaixarASiMesmo`
- Estoque: `deveListarProdutosComEstoqueBaixo`, `adminDeveReporEstoque`, `deveRetornar404AoReporProdutoInexistente`

Implementação:
- `controller/AdminController.java` com `@PreAuthorize` na classe
- `service/AdminService.java`
- DTOs: `DashboardResponse`, `AdminUserResponse`, `UpdateRoleRequest`, `RestockRequest`
- Queries agregadas em `OrderRepository` com `@Query`
- `ProductRepository.findByStockLessThanEqualOrderByStockAsc`

Decisões:
- Guarda-corpo: ADMIN não pode alterar a própria role (evita lockout)
- `/admin/users` retorna lista simples (lojas pequenas); `/admin/orders` é paginado (cresce muito)
- Queries agregadas via `@Query` para evitar N+1 no dashboard
- `@PreAuthorize` na classe — todos os métodos herdam a restrição

---

### Fase 13 — Cache Redis no catálogo

Endpoints afetados (mesma API, comportamento idêntico):
- `GET /products/{id}` — cacheado por `id`
- `GET /products` — cacheado por combinação de filtros + paginação
- `POST/PUT/DELETE /products` — invalidam os dois caches

Testes: 8, todos passando
- `primeiraChamadaConsultaBanco`
- `segundaChamadaUsaCache`
- `cacheRetornaDadosCorretos`
- `updateInvalidaCache`
- `deleteLimpaCache`
- `listagemCacheadaPorFiltros`
- `listagemVaziaTambemCacheada`
- `cachesConfiguradosExistem`

Implementação:
- `spring-boot-starter-cache` (abstração) + `spring-boot-starter-data-redis` (backend)
- `config/CacheConfig.java` com `@EnableCaching`
- `@EnableCaching` também na classe `LojaApplication`
- `application.yml`: `spring.cache.type=redis`, TTL de 5 minutos
- `application-test.yml`: `spring.cache.type=simple`
- `ProductService`:
  - `@Cacheable("products", key = "#id")` em `findById`
  - `@Cacheable("productLists", key = "categoria|search|page|size")` em `list`
  - `@CacheEvict(value = {"products", "productLists"}, allEntries = true)` em `create`/`update`/`delete`
- `docker-compose.yml` com PostgreSQL e Redis

Decisões:
- Dois caches separados (`products`, `productLists`) — invalidação independente
- `@CacheEvict(allEntries = true)` na escrita
- `spring-boot-starter-cache` é obrigatório mesmo com Redis
- Testes limpam cache no `@BeforeEach`

---

### Fase 14 — Dashboard de métricas

Endpoints Actuator:
- `GET /actuator/health` → público
- `GET /actuator/info` → público
- `GET /actuator/metrics` → autenticado
- `GET /actuator/prometheus` → autenticado

Métricas customizadas:
- `loja.orders.created` — incrementa em cada checkout bem-sucedido
- `loja.payments.confirmed` — incrementa em cada confirmação de pagamento

Testes: 7, todos passando
- `healthPublico` — 200 sem token
- `infoPublico` — 200 sem token
- `metricsExigeAutenticacao` — 401 sem token
- `metricsComAdmin` — 200 com token ADMIN
- `contadorPedidosIncrementa` — `loja.orders.created` +1 após checkout
- `contadorPagamentosIncrementa` — `loja.payments.confirmed` +1 após confirmação
- `metricaPedidosExposta` — `/actuator/metrics/loja.orders.created` retorna 200 com valor correto

Implementação:
- `micrometer-registry-prometheus` no `pom.xml`
- `config/PrometheusMetricsConfig.java` — `PrometheusMeterRegistry` explícito (contorna bug de auto-config do Spring Boot 3.4 + Micrometer 1.14 quando há injeção precoce do `MeterRegistry`)
- `service/MetricsService.java` — `Counter` para pedidos criados e pagamentos confirmados
- `OrderService.checkout()` chama `metricsService.orderCreated()`
- `PaymentService.confirm()` e `handleGatewayWebhook()` chamam `metricsService.paymentConfirmed()`
- `application.yml` e `application-test.yml`: `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
- `application-test.yml`: `management.health.redis.enabled=false` — Redis não é dependência dos testes
- `SecurityConfig`: `/actuator/health` e `/actuator/info` públicos; restante do Actuator autenticado

Decisões:
- Métricas **de negócio** (pedidos, pagamentos), não só de infraestrutura (JVM, HTTP)
- `MetricsService` centraliza criação dos contadores — sem espalhar `MeterRegistry` pelos serviços
- `PrometheusMetricsConfig` explícito porque a auto-config do Micrometer 1.14 + Spring Boot 3.4 falha silenciosamente em alguns cenários
- Testes de métricas usam `MeterRegistry.find(nome).counter()` direto — mais estável que scrape HTTP
- `/actuator/prometheus` fica exposto mas o teste cobre o caminho `/actuator/metrics` (mais determinístico)

---

## Estrutura final do projeto

```
loja-api/
├── pom.xml
├── README.md
├── CHANGELOG.md
├── Resumo.md
├── LICENSE
├── docker-compose.yml
├── .editorconfig
├── .gitattributes
├── .env.example
├── .github/
│   ├── workflows/ci.yml
│   ├── dependabot.yml
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── ISSUE_TEMPLATE/
└── src/
    ├── main/
    │   ├── java/com/alvescosta/loja/
    │   │   ├── LojaApplication.java
    │   │   ├── config/          (SecurityConfig, CacheConfig, PrometheusMetricsConfig)
    │   │   ├── domain/          (14 arquivos: entidades e enums)
    │   │   ├── dto/             (23 records)
    │   │   ├── exception/       (4 arquivos)
    │   │   ├── repository/      (6 interfaces JPA)
    │   │   ├── security/        (4 arquivos)
    │   │   ├── service/         (14 serviços, incluindo MetricsService)
    │   │   └── controller/      (10 controllers, incluindo WebhookController)
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           ├── V1__create_users.sql
    │           ├── V2__create_products.sql
    │           ├── V3__create_cart_items.sql
    │           ├── V4__create_orders.sql
    │           ├── V5__create_coupons.sql
    │           ├── V6__alter_orders_add_coupon.sql
    │           ├── V7__alter_orders_add_shipping.sql
    │           ├── V8__create_payments.sql
    │           └── V9__alter_payments_add_gateway_fields.sql
    └── test/
        ├── java/com/alvescosta/loja/
        │   ├── auth/AuthControllerTest.java             (6 testes)
        │   ├── users/UserControllerTest.java            (3 testes)
        │   ├── products/ProductControllerTest.java      (14 testes)
        │   ├── products/ProductCacheTest.java           (8 testes)
        │   ├── cart/CartControllerTest.java             (14 testes)
        │   ├── orders/OrderControllerTest.java          (17 testes)
        │   ├── coupons/CouponControllerTest.java        (12 testes)
        │   ├── shipping/ShippingControllerTest.java     (6 testes)
        │   ├── payments/PaymentControllerTest.java      (8 testes)
        │   ├── payments/CardPaymentControllerTest.java  (9 testes)
        │   ├── admin/AdminControllerTest.java           (12 testes)
        │   └── metrics/MetricsTest.java                 (7 testes)
        └── resources/application-test.yml
```

Total: 77 arquivos de produção, 12 classes de teste, **116 testes**.

---

## Decisões técnicas

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

## Bugs encontrados e correções

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
| 13 | `ProductCacheTest` falhava — `findById` chamado mesmo após cache | Faltava `@Cacheable`/`@CacheEvict` no `ProductService` | Aplicar as anotações |
| 13 | `ProductControllerTest.qualquerPessoaPodeListarProdutos` falhava com dados antigos | Cache entre testes (mesmo contexto Spring) | Limpar cache no `@BeforeEach` |
| 14 | `/actuator/prometheus` retornava 404 | `PrometheusMeterRegistry` não era criado pela auto-config | `PrometheusMetricsConfig` explícito |
| 14 | `/actuator/health` retornava 503 | Redis health check falhava em ambiente sem Redis | `management.health.redis.enabled=false` no perfil de teste |
| 14 | `/actuator/prometheus` retornava 200 com body vazio | Registry manual não era o mesmo usado pelo `MetricsService` | Testar via `/actuator/metrics/{nome}` em vez do scrape Prometheus |

---

## Como executar

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

---

## Possíveis extensões futuras

- Refresh token para JWT
- Rate limiting nas rotas de auth e webhook
- E-mail transacional (confirmação de pedido)
- Testcontainers substituindo H2 por PostgreSQL real
- Paginação padronizada com `PagedModel` (`@EnableSpringDataWebSupport`)
- Job agendado para expirar pagamentos Pix
- Deploy em cloud (Render, Railway, Fly.io)
- Documentação automática com SpringDoc OpenAPI
- Multi-tenancy (múltiplas lojas em uma instância)

---

## Como atualizar este documento

Ao concluir uma nova fase (ou extensão futura):

1. Marcar a fase na tabela do roadmap e atualizar o contador de progresso
2. Adicionar um bloco em "Fases concluídas" seguindo o formato das fases anteriores
3. Atualizar a seção "Estrutura do projeto"
4. Registrar novos bugs na tabela de bugs encontrados, se houver
5. Atualizar a seção "Próxima fase" com o novo escopo