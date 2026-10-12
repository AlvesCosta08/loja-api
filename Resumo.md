# 📘 Resumo.md — Documento de Contexto do Projeto

> **Propósito:** manter o contexto do projeto entre sessões. Atualizado ao final de cada fase concluída.
> **Como usar:** toda vez que retomar o projeto, leia este arquivo primeiro.

---

## 📌 Informações Gerais

| Campo | Valor |
|-------|-------|
| **Projeto** | loja-api |
| **Descrição** | API REST de e-commerce (Loja Virtual) com autenticação, catálogo, carrinho, pedidos, cupons, frete e pagamentos |
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.4 |
| **Abordagem** | TDD (Red → Green → Refactor) |
| **Autenticação** | JWT (JJWT 0.12.6) |
| **Banco (produção)** | PostgreSQL |
| **Banco (testes)** | H2 em memória |
| **Migrations** | Flyway |
| **Boilerplate** | Lombok |
| **Build** | Maven |
| **Pacote base** | `com.alvescosta.loja` |
| **Repositório** | https://github.com/AlvesCosta08/loja-api |
| **Autor** | Alves Costa |
| **Última atualização** | Fim da Fase 10 — Pagamento Pix |

---

## 🎯 Objetivo do Projeto

Construir uma API de e-commerce robusta, seguindo **TDD estrito**, cobrindo:

- Autenticação e autorização com JWT e roles
- Catálogo de produtos com filtros e paginação
- Carrinho de compras persistente
- Checkout com snapshot de preços
- Integração com gateways de pagamento
- Cálculo de frete e cupons
- Painel administrativo
- Cache com Redis
- Dashboard de métricas

---

## 🗺️ Roadmap Completo

| # | Fase | Status |
|---|------|--------|
| 0 | Setup do projeto (Maven, Spring Boot, H2, dependências) | ✅ CONCLUÍDO |
| 1 | Cadastro de usuário (`POST /auth/register`) | ✅ CONCLUÍDO |
| 2 | Login + JWT (`POST /auth/login`) | ✅ CONCLUÍDO |
| 3 | Usuário logado (`GET /users/me`) | ✅ CONCLUÍDO |
| 4 | CRUD de produtos (`/products`) | ✅ CONCLUÍDO |
| 5 | Categorias + busca | ✅ CONCLUÍDO (embutido na Fase 4) |
| 6 | Carrinho (`/cart`) | ✅ CONCLUÍDO |
| 7 | Checkout / Pedidos (`/orders`) | ✅ CONCLUÍDO |
| 8 | Cupons de desconto (`/coupons`) | ✅ CONCLUÍDO |
| 9 | Cálculo de frete (`/shipping`) | ✅ CONCLUÍDO |
| 10 | Pagamento Pix (`/payments`) | ✅ CONCLUÍDO |
| **11** | **Pagamento cartão / Stripe** | **⏳ PRÓXIMA FASE** |
| 12 | Painel administrativo | ⏳ PENDENTE |
| 13 | Cache Redis no catálogo | ⏳ PENDENTE |
| 14 | Dashboard de métricas | ⏳ PENDENTE |

**Progresso:** `11 / 14 fases concluídas (79%)` · **Testes verdes:** `80`

---

## ✅ FASES CONCLUÍDAS — DETALHAMENTO

### 🏗️ Fase 0 — Setup do projeto

**Estrutura de pacotes criada (dentro de `com.alvescosta.loja`):**

```
com.alvescosta.loja
├── config/       → SecurityConfig
├── domain/       → Entidades JPA
├── dto/          → Records de request/response
├── exception/    → Exceções customizadas + handler global
├── repository/   → Interfaces JpaRepository
├── security/     → JwtService, JwtAuthFilter, UserDetails
├── service/      → Regras de negócio
└── controller/   → Endpoints REST
```

**Configurações:**

- `pom.xml` com Spring Boot 3.4 + Java 21
- Dependências: Web, Security, Data JPA, Validation, Actuator, Flyway, JJWT 0.12.6, PostgreSQL, H2 (testes), Lombok
- `application.yml` (produção) e `application-test.yml` (H2 em memória)
- `@EnableMethodSecurity` habilitado

**Ambiente do dev:**

- JDK 21 instalado em `/usr/lib/jvm/java-21-openjdk-amd64`
- `JAVA_HOME` configurado em `~/.bashrc`
- Maven Wrapper 3.10.0 via `./mvnw`

---

### 🔐 Fase 1 — Cadastro de usuário

**Endpoints:**

- `POST /auth/register` → 201 Created

**Testes (RED) — 3 testes:** ✅ todos passando

- `deveRegistrarUsuarioComSucesso`
- `naoDeveRegistrarEmailDuplicado`
- `naoDeveAceitarSenhaCurta`

**Implementação (GREEN):**

- `domain/User.java` (id, email, name, hashedPassword, role)
- `domain/Role.java` (CUSTOMER, ADMIN)
- `dto/UserCreateRequest.java` com `@Email`, `@Size(min=8)`, `@NotBlank`
- `dto/UserResponse.java` (nunca expõe senha)
- `repository/UserRepository.existsByEmail()`
- `service/UserService.register()` com BCrypt
- `controller/AuthController.register()`
- `exception/GlobalExceptionHandler` para 400/404

**Refactor:**

- Tratamento de erros centralizado com `@RestControllerAdvice`

---

### 🔑 Fase 2 — Login + JWT

**Endpoints:**

- `POST /auth/login` → 200 OK com `access_token`

**Testes (RED) — 3 testes:** ✅ todos passando

- `deveLogarERetornarToken`
- `deveRejeitarSenhaErrada` → 401
- `naoDeveLogarUsuarioInexistente` → 401

**Implementação (GREEN):**

- `security/JwtService.java`
  - `generate(User)` → cria token HS256
  - `extractUserId(String)` → valida e retorna subject
  - `getExpirationMs()` → expõe a expiração para o controller
- `service/UserService.authenticate()` com `PasswordEncoder.matches()`
- `dto/LoginRequest.java`, `dto/TokenResponse.java`
- `controller/AuthController.login()`
- `exception/UnauthorizedException.java`

**Refactor:**

- Parâmetros do JWT externalizados em `application.yml`:
  - `jwt.secret` (aceita variável de ambiente `JWT_SECRET`)
  - `jwt.expiration-ms`
- `UnauthorizedException` centralizada no `GlobalExceptionHandler`

---

### 👤 Fase 3 — Usuário logado

**Endpoints:**

- `GET /users/me` → 200 OK (protegido)

**Testes (RED) — 3 testes:** ✅ todos passando

- `deveRetornar401SemToken`
- `deveRetornar401ComTokenInvalido`
- `deveRetornarDadosDoUsuarioLogado`

**Implementação (GREEN):**

- `security/JwtAuthFilter.java` — valida Bearer, popula `SecurityContext` (ignora silenciosamente token inválido → 401 do entry point)
- `security/UserDetailsServiceImpl.java` — carrega usuário pelo **ID** (username = `user.getId().toString()`)
- `security/RestAuthenticationEntryPoint.java` — responde 401 JSON em vez de 403 default do Spring
- `config/SecurityConfig.java`:
  - CSRF desabilitado
  - Sessão STATELESS
  - Rotas públicas: `/auth/**`, `/health`, `GET /products/**`
  - Filtro JWT antes de `UsernamePasswordAuthenticationFilter`
  - `exceptionHandling` com entry point customizado
- `controller/UserController.me()` com `@AuthenticationPrincipal`

---

### 📦 Fase 4 — CRUD de Produtos

**Endpoints:**

- `GET /products` → lista paginada com filtros (`category`, `search`)
- `GET /products/{id}` → detalhes ou 404
- `POST /products` → 201 (ADMIN)
- `PUT /products/{id}` → 200 (ADMIN)
- `DELETE /products/{id}` → 204 (ADMIN)

**Testes (RED) — 14 testes:** ✅ todos passando

**Implementação (GREEN):**

- `domain/Product.java` (`id`, `name`, `price` `BigDecimal`, `stock`, `category`, `imageUrl`)
- `dto/ProductRequest.java`, `dto/ProductResponse.java`
- `repository/ProductRepository.java`:
  - `findByCategoryIgnoreCase`
  - `findByNameContainingIgnoreCase`
  - `findByCategoryIgnoreCaseAndNameContainingIgnoreCase`
- `service/ProductService.java`: `create`, `findById`, `list`, `update`, `delete`
- `controller/ProductController.java`:
  - `@PreAuthorize("hasRole('ADMIN')")` em POST/PUT/DELETE
  - `@PageableDefault(size=10, sort="name")`

**Refactor:**

- `@EnableMethodSecurity` no `SecurityConfig`
- `BigDecimal` para dinheiro
- `getOrThrow(id)` centralizado no `ProductService`

---

### 🛒 Fase 6 — Carrinho de Compras

**Endpoints:**

- `POST /cart/items` → 201 (adiciona ou soma item)
- `GET /cart` → 200 (lista + `total` + `itemCount`)
- `PUT /cart/items/{id}` → 200 (atualiza quantidade)
- `DELETE /cart/items/{id}` → 204
- `DELETE /cart` → 204 (limpa carrinho)

**Testes (RED) — 14 testes:** ✅ todos passando

**Implementação (GREEN):**

- `domain/CartItem.java` com `@ManyToOne User` + `@ManyToOne Product`, unique `(user_id, product_id)`, `@Min(1)` na quantidade
- `@OnDelete(CASCADE)` em ambas as FKs (produção e H2)
- `dto/AddToCartRequest.java`, `UpdateCartItemRequest.java`, `CartItemResponse.java`, `CartResponse.java`
- `repository/CartItemRepository.java`:
  - `findByUserId`, `findByUserIdAndProductId`, `findByIdAndUserId`, `deleteByUserId`
- `service/CartService.java`: `addItem`, `getCart`, `updateQuantity`, `removeItem`, `clearCart`
- `service/CartCalculator.java` (cálculo de total isolado, testável)
- `controller/CartController.java` (helper `currentUserId(principal)`)

**Refactor:**

- `@Transactional` em escrita, `@Transactional(readOnly=true)` na leitura
- Unique constraint + `CHECK (quantity >= 1)` no banco (defesa em profundidade)
- `CartItemResponse.from(CartItem)` centraliza mapeamento

---

### 📦 Fase 7 — Checkout / Pedidos

**Endpoints:**

- `POST /orders` → 201 (cria pedido a partir do carrinho)
- `GET /orders` → 200 (paginado, apenas do usuário logado)
- `GET /orders/{id}` → 200 (dono ou ADMIN; 403 caso contrário)
- `PATCH /orders/{id}/status` → 200 (ADMIN; altera `PENDING → PAID → SHIPPED → DELIVERED`)

**Testes (RED) — 12 testes:** ✅ todos passando

- Autenticação: `naoDeveCriarPedidoSemToken`
- Criação: `deveCriarPedidoAPartirDoCarrinho`, `naoDeveCriarPedidoComCarrinhoVazio`, `naoDeveCriarPedidoAcimaDoEstoque`, `carrinhoDeveSerLimpoAposCheckout`, `estoqueDeveSerDecrementado`
- Snapshot: `deveManterSnapshotDePrecos`
- Listagem: `usuarioVeApenasSeusPedidos`
- Busca: `deveRetornar404QuandoPedidoNaoExiste`, `naoDeveAcessarPedidoDeOutroUsuario`, `donoVeSeuPedido`
- Status: `adminDeveMudarStatus`, `clienteNaoPodeMudarStatus`

**Implementação (GREEN):**

- `domain/OrderStatus.java` (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)
- `domain/Order.java` (`user` `@OnDelete(CASCADE)`, `status`, `total`, `discount`, `couponCode`, `createdAt`, `items`)
- `domain/OrderItem.java` (snapshot: `productName`, `unitPrice`; `@OnDelete(CASCADE)` em order, `@OnDelete(SET_NULL)` em product)
- `dto/OrderResponse.java`, `dto/OrderItemResponse.java`, `dto/UpdateOrderStatusRequest.java`
- `repository/OrderRepository.java` (`findByUserIdOrderByCreatedAtDesc`)
- `service/OrderService.java`:
  - `checkout(userId, couponCode)` — valida estoque **antes** de mutar
  - `listMine(userId, pageable)`
  - `findByIdForUser(orderId, userId, isAdmin)`
  - `updateStatus(orderId, newStatus)`
- `controller/OrderController.java`

**Refactor / Decisões:**

- **Snapshot de preços**: se o produto mudar de preço, o pedido antigo mantém o valor histórico
- Valida estoque **antes de qualquer mutação** (atomicidade)
- Decrementa estoque **no checkout**, não no pagamento
- `AccessDeniedException` do Spring Security responde 403 automaticamente

---

### 🎟️ Fase 8 — Cupons de Desconto

**Endpoints:**

- `POST /coupons` → 201 (ADMIN)
- `GET /coupons/{code}` → 200 (público)

**Testes (RED) — 12 testes:** ✅ todos passando

- Autenticação: `naoDeveCriarCupomSemToken`, `clienteNaoPodeCriarCupom`
- Criação: `adminDeveCriarCupomPercentual`, `naoDeveCriarCupomDuplicado`, `naoDeveAceitarPercentualAcimaDe100`
- Aplicação no checkout: `deveAplicarCupomPercentual`, `deveAplicarCupomFixo`, `naoDeveAplicarCupomInexistente`, `naoDeveAplicarCupomExpirado`, `naoDeveAplicarCupomAbaixoDoMinimo`, `naoDeveAplicarCupomEsgotado`
- Contador: `deveIncrementarUso`

**Implementação (GREEN):**

- `domain/DiscountType.java` (PERCENTAGE, FIXED)
- `domain/Coupon.java`:
  - `code`, `type`, `value` (coluna `discount_value` — **`VALUE` é palavra reservada SQL!**), `minOrderValue`, `maxUses`, `usageCount`, `expiresAt`, `active`
- `dto/CouponRequest.java`, `dto/CouponResponse.java`
- `repository/CouponRepository.java` (`findByCodeIgnoreCase`, `existsByCodeIgnoreCase`)
- `service/CouponService.java`:
  - `create(request)` — valida duplicata e percentual ≤ 100
  - `validateAndCalculate(code, subtotal)` — valida ativo, expiração, usos, valor mínimo; calcula desconto (mín. entre desconto e subtotal)
  - `registerUsage(code)` — incrementa `usageCount` **após** sucesso
  - `findByCode(code)`
- `controller/CouponController.java`
- `OrderService.checkout()` aceita `couponCode` opcional
- `Order` ganha `discount` e `couponCode`

**Refactor / Decisões:**

- **`usageCount` incrementado só após sucesso** — falha não queima cupom
- `expiresAt` em `Instant` (UTC) — evita bugs de timezone
- `minOrderValue` e `maxUses` **nullable** — cupom simples = sem restrição
- Cupom aplicado **no checkout**, não no carrinho
- `couponCode` + `discount` salvos no `Order` — auditoria
- **Coluna `discount_value`** em vez de `value` — `VALUE` é palavra reservada em H2/SQL-92

---

### 🚚 Fase 9 — Cálculo de Frete

**Endpoints:**

- `POST /shipping/quote` → 200 (público — consulta opções)
- `POST /orders?cep=...` → 201 (com frete aplicado no checkout)

**Testes (RED) — 10 testes:** ✅ todos passando

`ShippingControllerTest` (6):

- `deveCalcularFreteSP` (prefixo 0: STANDARD 10 / EXPRESS 20)
- `deveCalcularFreteRJ` (prefixo 2: STANDARD 15 / EXPRESS 30)
- `deveCalcularFreteOutraRegiao` (prefixo 6: STANDARD 20 / EXPRESS 40)
- `deveDarFreteGratisStandardAcimaDe200`
- `deveAceitarCepComHifen`
- `deveRejeitarCepInvalido`

`OrderControllerTest` (4 — integração):

- `deveAplicarFreteGratisAcimaDe200`
- `deveCobrarFreteParaSubtotalBaixo` (Caneta R$ 50 → total R$ 60 com frete SP R$ 10)
- `naoDeveAplicarFreteSemCep`
- `deveRejeitarCepInvalidoNoCheckout`

**Implementação (GREEN):**

- `domain/ShippingType.java` (STANDARD, EXPRESS)
- `dto/ShippingQuoteRequest.java`, `ShippingOption.java`, `ShippingQuoteResponse.java`
- `service/ShippingService.java`:
  - `quote(cep, subtotal)` — retorna 2 opções
  - `costFor(cep, subtotal, type)` — usado no checkout
  - `normalizeCep()` — aceita com/sem hífen
  - Tabela: prefixo `0`/`1` → SP; `2`/`3` → RJ/ES; outros → resto
  - STANDARD grátis se `subtotal >= R$ 200`; EXPRESS nunca grátis
- `controller/ShippingController.java` (endpoint público)
- `Order.shippingCost` (`BigDecimal`, default 0)
- `OrderResponse.shippingCost`
- `OrderService.checkout(userId, couponCode, cep)` — frete opcional
- `OrderController.checkout(principal, couponCode, cep)`
- Migration `V7__alter_orders_add_shipping.sql`

**Refactor / Decisões:**

- **Fórmula:** `total = subtotal − discount + shippingCost`
- Cálculo **no service**, não no controller
- CEP normalizado com `replaceAll("[^0-9]", "")`
- `FREE_THRESHOLD = R$ 200` como constante
- Frete **opcional** — sem `?cep=`, `shippingCost = 0` (retrocompatível)

---

### 💸 Fase 10 — Pagamento Pix

**Endpoints:**

- `POST /payments?orderId=...` → 201 (gera Pix — dono ou ADMIN)
- `GET /payments/{id}` → 200 (dono ou ADMIN)
- `POST /payments/{id}/confirm` → 200 (ADMIN; transiciona pedido para `PAID`)

**Testes (RED) — 8 testes:** ✅ todos passando

- Autenticação: `naoDeveCriarPagamentoSemToken`
- Criação: `deveCriarPagamentoPix`, `deveRetornar404QuandoPedidoNaoExiste`, `naoDevePagarPedidoDeOutroUsuario`, `naoDevePagarPedidoJaPago`
- Confirmação: `adminDeveConfirmarPagamento`, `clienteNaoPodeConfirmarPagamento`
- Consulta: `donoBuscaPagamento`

**Implementação (GREEN):**

- `domain/PaymentMethod.java` (PIX, CREDIT_CARD)
- `domain/PaymentStatus.java` (PENDING, CONFIRMED, FAILED, EXPIRED)
- `domain/Payment.java` (`order` `@OnDelete(CASCADE)`, `method`, `status`, `amount`, `pixCopyPaste`, `pixQrCodeBase64`, `createdAt`, `expiresAt`, `confirmedAt`)
- `dto/PaymentResponse.java`
- `repository/PaymentRepository.java` (`findFirstByOrderIdAndStatus`)
- `service/PixGenerator.java` — payload EMV mock + PNG base64 (1×1 transparente)
- `service/PaymentService.java`:
  - `createPix(orderId, userId, isAdmin)` — valida dono e status `PENDING`; expira em 30min
  - `confirm(paymentId)` — seta `CONFIRMED`, move pedido para `PAID`
  - `findByIdForUser(paymentId, userId, isAdmin)`
- `controller/PaymentController.java`
- Migration `V8__create_payments.sql`

**Refactor / Decisões:**

- **`pixQrCodeBase64` é coluna TEXT** — imagens grandes não cabem em VARCHAR(255)
- **Status do pedido muda para `PAID` na confirmação** — um só lugar no `PaymentService.confirm`
- **Mock determinístico** (`PixGenerator`) — em produção vira chamada HTTP ao PSP
- **`expiresAt = createdAt + 30min`** — janela curta evita QR "zumbi"
- **`@PreAuthorize("hasRole('ADMIN')")` na confirmação** — só operador da loja confirma

---

## 📂 Estrutura Atual do Projeto

```
loja-api/
├── pom.xml
├── Resumo.md
└── src/
    ├── main/
    │   ├── java/com/alvescosta/loja/
    │   │   ├── LojaApplication.java
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java                    ✅
    │   │   ├── domain/
    │   │   │   ├── User.java                              ✅
    │   │   │   ├── Role.java                              ✅
    │   │   │   ├── Product.java                           ✅
    │   │   │   ├── CartItem.java                          ✅
    │   │   │   ├── Order.java                             ✅
    │   │   │   ├── OrderItem.java                         ✅
    │   │   │   ├── OrderStatus.java                       ✅
    │   │   │   ├── Coupon.java                            ✅
    │   │   │   ├── DiscountType.java                      ✅
    │   │   │   ├── ShippingType.java                      ✅
    │   │   │   ├── Payment.java                           ✅
    │   │   │   ├── PaymentMethod.java                     ✅
    │   │   │   └── PaymentStatus.java                     ✅
    │   │   ├── dto/
    │   │   │   ├── UserCreateRequest.java                 ✅
    │   │   │   ├── UserResponse.java                      ✅
    │   │   │   ├── LoginRequest.java                      ✅
    │   │   │   ├── TokenResponse.java                     ✅
    │   │   │   ├── ProductRequest.java                    ✅
    │   │   │   ├── ProductResponse.java                   ✅
    │   │   │   ├── AddToCartRequest.java                  ✅
    │   │   │   ├── UpdateCartItemRequest.java             ✅
    │   │   │   ├── CartItemResponse.java                  ✅
    │   │   │   ├── CartResponse.java                      ✅
    │   │   │   ├── OrderResponse.java                     ✅
    │   │   │   ├── OrderItemResponse.java                 ✅
    │   │   │   ├── UpdateOrderStatusRequest.java          ✅
    │   │   │   ├── CouponRequest.java                     ✅
    │   │   │   ├── CouponResponse.java                    ✅
    │   │   │   ├── ShippingQuoteRequest.java              ✅
    │   │   │   ├── ShippingOption.java                    ✅
    │   │   │   ├── ShippingQuoteResponse.java             ✅
    │   │   │   └── PaymentResponse.java                   ✅
    │   │   ├── exception/
    │   │   │   ├── BusinessException.java                 ✅
    │   │   │   ├── NotFoundException.java                 ✅
    │   │   │   ├── UnauthorizedException.java             ✅
    │   │   │   └── GlobalExceptionHandler.java            ✅
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java                    ✅
    │   │   │   ├── ProductRepository.java                 ✅
    │   │   │   ├── CartItemRepository.java                ✅
    │   │   │   ├── OrderRepository.java                   ✅
    │   │   │   ├── CouponRepository.java                  ✅
    │   │   │   └── PaymentRepository.java                 ✅
    │   │   ├── security/
    │   │   │   ├── JwtService.java                        ✅
    │   │   │   ├── JwtAuthFilter.java                     ✅
    │   │   │   ├── UserDetailsServiceImpl.java            ✅
    │   │   │   └── RestAuthenticationEntryPoint.java      ✅
    │   │   ├── service/
    │   │   │   ├── UserService.java                       ✅
    │   │   │   ├── ProductService.java                    ✅
    │   │   │   ├── CartService.java                       ✅
    │   │   │   ├── CartCalculator.java                    ✅
    │   │   │   ├── OrderService.java                      ✅
    │   │   │   ├── CouponService.java                     ✅
    │   │   │   ├── ShippingService.java                   ✅
    │   │   │   ├── PixGenerator.java                      ✅
    │   │   │   └── PaymentService.java                    ✅
    │   │   └── controller/
    │   │       ├── AuthController.java                    ✅
    │   │       ├── UserController.java                    ✅
    │   │       ├── ProductController.java                 ✅
    │   │       ├── CartController.java                    ✅
    │   │       ├── OrderController.java                   ✅
    │   │       ├── CouponController.java                  ✅
    │   │       ├── ShippingController.java                ✅
    │   │       └── PaymentController.java                 ✅
    │   └── resources/
    │       ├── application.yml                            ✅
    │       └── db/migration/
    │           ├── V1__create_users.sql                   ✅
    │           ├── V2__create_products.sql                ✅
    │           ├── V3__create_cart_items.sql              ✅
    │           ├── V4__create_orders.sql                  ✅
    │           ├── V5__create_coupons.sql                 ✅
    │           ├── V6__alter_orders_add_coupon.sql        ✅
    │           ├── V7__alter_orders_add_shipping.sql      ✅
    │           └── V8__create_payments.sql                ✅
    └── test/
        ├── java/com/alvescosta/loja/
        │   ├── auth/AuthControllerTest.java               ✅ (6 testes)
        │   ├── users/UserControllerTest.java              ✅ (3 testes)
        │   ├── products/ProductControllerTest.java        ✅ (14 testes)
        │   ├── cart/CartControllerTest.java               ✅ (14 testes)
        │   ├── orders/OrderControllerTest.java            ✅ (17 testes)
        │   ├── coupons/CouponControllerTest.java          ✅ (12 testes)
        │   ├── shipping/ShippingControllerTest.java       ✅ (6 testes)
        │   └── payments/PaymentControllerTest.java        ✅ (8 testes)
        └── resources/
            └── application-test.yml                       ✅
```

**Legenda:** ✅ concluído · ⏳ pendente

---

## 🎯 PRÓXIMA FASE — Fase 11: Pagamento Cartão / Stripe

### 📋 Objetivo

Adicionar suporte a **cartão de crédito** reaproveitando a estrutura da Fase 10. Modelo: criar `PaymentIntent` (com `clientSecret`), receber webhook de confirmação, transicionar pedido para `PAID`. Interface `PaymentGateway` permite trocar PIX por Stripe sem mudar o controller.

### 🔴 Passo 1 — Escrever os testes PRIMEIRO (`CardPaymentControllerTest.java`)

**Casos previstos (~10 testes):**

- [ ] 401 sem token
- [ ] 201 ao criar PaymentIntent para pedido `PENDING`
- [ ] `clientSecret` e `gatewayRef` presentes na resposta
- [ ] 404 pedido inexistente
- [ ] 403 outro CUSTOMER
- [ ] 400 pedido não-`PENDING`
- [ ] 200 webhook confirma e vira pedido `PAID`
- [ ] 400 webhook sem assinatura
- [ ] Idempotência: webhook duplicado não re-paga
- [ ] 200 dono busca PaymentIntent

### 🟢 Passo 2 — Implementação (GREEN)

- [ ] `service/PaymentGateway.java` — interface comum
- [ ] `service/StripePaymentGateway.java` — mock determinístico
- [ ] `controller/WebhookController.java` — endpoint público `/webhooks/stripe`
- [ ] Migration `V9__alter_payments_add_gateway_fields.sql` (`gateway_ref`, `client_secret`)

### 🔵 Passo 3 — Refactor

- [ ] `PaymentService` escolhe gateway por `PaymentMethod`
- [ ] Rate limit no webhook (Spring Security ou filtro)

### ✅ Critérios de Conclusão da Fase 11

- [ ] `mvn test` → BUILD SUCCESS
- [ ] ~10 testes novos passando
- [ ] Webhook idempotente (teste dedicado)

---

## 🧠 Decisões Técnicas Importantes

| Decisão | Motivo |
|---------|--------|
| `BigDecimal` para preços | Evitar erro de arredondamento de `double` |
| H2 em memória nos testes | Testes rápidos e isolados |
| PostgreSQL em produção via **Flyway** | Versionamento de schema auditável e reprodutível |
| **Lombok** para getters/setters | Reduzir boilerplate (mas usar `record` em DTOs) |
| **Actuator** habilitado | `/health` e `/metrics` prontos para observabilidade |
| `@EnableMethodSecurity` | Sem isso, `@PreAuthorize` é ignorado silenciosamente |
| JWT STATELESS | API escalável horizontalmente, sem sessão no servidor |
| `record` para DTOs | Imutabilidade + menos boilerplate |
| Repositórios com métodos derivados | Menos SQL manual, mais legível |
| Testes escritos antes do código | Garantia de cobertura real e comportamento guiado |
| **`@OnDelete(CASCADE)`** em FKs | Deletar pai apaga filhos; também destrava testes |
| **`@OnDelete(SET_NULL)`** em `OrderItem.product` | Produto pode ser deletado sem perder histórico do pedido |
| **Snapshot de preços** em `OrderItem` | Se produto mudar, pedido mantém valor histórico |
| **Unique constraint `(user_id, product_id)`** em `CartItem` | Invariante "um item por produto por usuário" |
| **`VALUE` é palavra reservada SQL** | Sempre usar `@Column(name="...")` para renomear |
| **`username = user.getId()`** no `UserDetailsServiceImpl` | Subject do JWT = ID do user |
| **`RestAuthenticationEntryPoint`** | Responde 401 em vez de 403 default do Spring |
| **Frete opcional** em `POST /orders` | Sem `?cep=`, `shippingCost = 0` (retrocompatível) |
| **`total = subtotal − discount + shippingCost`** | Fórmula canônica de checkout |
| **`pixQrCodeBase64` como TEXT** | Imagens grandes não cabem em VARCHAR(255) |
| **`PixGenerator` como mock** | Isola a regra de negócio do gateway externo |
| **`expiresAt` para Pix** | Evita QR "zumbi" |

---

## 🧪 Como Rodar os Testes

```bash
# Todos os testes
./mvnw test

# Limpa target + roda tudo (recomendado após mudanças de schema)
./mvnw clean test

# Uma classe específica
./mvnw test -Dtest=ProductControllerTest

# Sobe a aplicação
./mvnw spring-boot:run
```

---

## 🐛 Bugs de Domínio Encontrados (lições)

| # | Fase | Sintoma | Causa | Fix |
|---|------|---------|-------|-----|
| 1 | 6 | `Referential integrity constraint violation` ao deletar user/product | `CartItem` sem `@OnDelete(CASCADE)` | `@OnDelete(action = CASCADE)` nas 2 FKs |
| 2 | 7 | Mesmo erro, agora envolvendo `ORDER_ITEMS` | `OrderItem.order` sem `@OnDelete(CASCADE)` | `@OnDelete(action = CASCADE)` na FK |
| 3 | 7 | `naoDeveAcessarPedidoDeOutroUsuario` retorna 200 em vez de 403 | Teste usava token de ADMIN (que tem permissão) | Teste usa outro CUSTOMER |
| 4 | 8 | `Syntax error ... c1_0.[*]value` no H2 | `VALUE` é palavra reservada SQL | `@Column(name = "discount_value")` |
| 5 | 9 | Teste esperava `total = 1000` mas recebeu `2500` | Esqueci que o `setup()` cria produto de R$ 2500 | Criar fixtures pequenas dentro de cada teste (ex: "Caneta" R$ 50) |
| 6 | 10 | `deveCriarPagamentoPix` esperava `amount = 60` mas recebeu `50` | Helper `criarPedido()` não passa `?cep=`, então não tem frete | Sempre revisar o helper antes de calcular valores esperados |

---

## 📝 Template para Atualizar ao Final de Cada Fase

Ao concluir uma nova fase, **atualize** este arquivo assim:

### 1. Atualize a tabela do **Roadmap** (marque ✅) e o contador de progresso

### 2. Atualize a seção **FASES CONCLUÍDAS** com um novo bloco:

````markdown
### 🔄 Fase X — [Nome] ([endpoints])

**Endpoints:**
- `METHOD /rota` → status

**Testes (RED) — N testes:** ✅ todos passando

**Implementação (GREEN):**
- Arquivos criados com breve descrição

**Refactor / Decisões:**
- O que foi melhorado e por quê



---

## 📊 O que mudou vs. versão que você colou

| Item | Antes (no seu arquivo) | Depois |
|------|------------------------|--------|
| **Cabeçalho** | `Repositório:` ausente | ✅ Adicionado `https://github.com/AlvesCosta08/loja-api` |
| **Fim do arquivo** | Colado texto de "Resumo do que mudou" + "Próximo passo" (que eram da minha mensagem de chat, não do documento) | ✅ Removido |
| **Template final** | Dizia "adicione ao arquivo" | ✅ Reescrito como "atualize este arquivo" |
| **Consistência** | Progresso 11/14, 80 testes ✅ | ✅ Mantido |

---

## 📋 O que preciso de você

Roda:

```bash
# Substitui o Resumo.md
nano Resumo.md
# (cola o conteúdo acima, salva com Ctrl+O, Enter, Ctrl+X)

# Confirma
head -20 Resumo.md
tail -20 Resumo.md
```

Depois, se quiser commitar a atualização:

```bash
git add Resumo.md
git commit -m "docs: atualiza Resumo.md com Fase 10 concluída"
git push
```

**Manda ver.** 🚀