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
| Migrações | Flyway |
| Build | Maven |
| Boilerplate | Lombok |
| Pacote base | `com.alvescosta.loja` |
| Repositório | https://github.com/AlvesCosta08/loja-api |
| Autor | Alves Costa |
| Última atualização | Fase 10 — Pagamento Pix |

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
| 11 | Pagamento com cartão (Stripe) | Próxima |
| 12 | Painel administrativo | Pendente |
| 13 | Cache Redis no catálogo | Pendente |
| 14 | Dashboard de métricas | Pendente |

Progresso: 11 de 14 fases. 80 testes automatizados, todos verdes.

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

## Estrutura do projeto

```
loja-api/
├── pom.xml
├── README.md
├── CHANGELOG.md
├── Resumo.md
├── LICENSE
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
    │   │   ├── config/SecurityConfig.java
    │   │   ├── domain/          (13 arquivos: entidades e enums)
    │   │   ├── dto/             (19 records)
    │   │   ├── exception/       (4 arquivos)
    │   │   ├── repository/      (6 interfaces JPA)
    │   │   ├── security/        (4 arquivos)
    │   │   ├── service/         (9 serviços)
    │   │   └── controller/      (8 controllers)
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
    │           └── V8__create_payments.sql
    └── test/
        ├── java/com/alvescosta/loja/
        │   ├── auth/AuthControllerTest.java             (6 testes)
        │   ├── users/UserControllerTest.java            (3 testes)
        │   ├── products/ProductControllerTest.java      (14 testes)
        │   ├── cart/CartControllerTest.java             (14 testes)
        │   ├── orders/OrderControllerTest.java          (17 testes)
        │   ├── coupons/CouponControllerTest.java        (12 testes)
        │   ├── shipping/ShippingControllerTest.java     (6 testes)
        │   └── payments/PaymentControllerTest.java      (8 testes)
        └── resources/application-test.yml
```

Total: 65 arquivos de produção, 8 classes de teste, 80 testes.

---

## Próxima fase — Fase 11: Pagamento com cartão (Stripe)

### Objetivo

Adicionar suporte a cartão de crédito reaproveitando a estrutura da Fase 10.
Criar `PaymentIntent` com `clientSecret`, receber webhook de confirmação e
transicionar pedido para `PAID`. Interface `PaymentGateway` permite trocar
PIX por Stripe sem alterar o controller.

### Testes (RED) — `CardPaymentControllerTest`

- `naoDeveCriarSemToken` → 401
- `deveCriarPaymentIntent` → 201 com `clientSecret` e `gatewayRef`
- `deveRetornar404` → pedido inexistente
- `naoDevePagarPedidoDeOutroUsuario` → 403
- `naoDevePagarPedidoJaPago` → 400
- `webhookDeveConfirmarPagamento` → 200 e pedido vira `PAID`
- `webhookSemAssinaturaDeveFalhar` → 400
- `webhookDuplicadoNaoDeveRepagar` → idempotência
- `donoBuscaPaymentIntent` → 200

### Implementação (GREEN)

- `service/PaymentGateway.java` — interface comum
- `service/StripePaymentGateway.java` — mock determinístico
- `controller/WebhookController.java` — endpoint público `/webhooks/stripe`
- Migration `V9__alter_payments_add_gateway_fields.sql` com `gateway_ref` e `client_secret`

### Refactor

- `PaymentService` escolhe gateway conforme `PaymentMethod`
- Rate limit no endpoint de webhook

### Critérios de conclusão

- `./mvnw clean test` com sucesso
- Cerca de 10 testes novos passando
- Webhook idempotente coberto por teste dedicado

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

---

## Como executar

```bash
# Todos os testes
./mvnw test

# Limpar e executar (recomendado após mudança de schema)
./mvnw clean test

# Classe específica
./mvnw test -Dtest=ProductControllerTest

# Subir a aplicação
./mvnw spring-boot:run
```

---

## Como atualizar este documento

Ao concluir uma fase:

1. Marcar a fase na tabela do roadmap e atualizar o contador de progresso
2. Adicionar um bloco em "Fases concluídas" seguindo o formato das fases anteriores
3. Atualizar a seção "Estrutura do projeto"
4. Registrar novos bugs na tabela de bugs encontrados, se houver
5. Atualizar a seção "Próxima fase" com o novo escopo