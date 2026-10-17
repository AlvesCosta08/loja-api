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
| Descrição | API REST de e-commerce com autenticação, catálogo, carrinho, pedidos, cupons, frete, pagamentos Pix/cartão, admin, métricas e camada de IA (em progresso) |
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
| Última atualização backend | Fase 15 — Infra de Dev (CORS, Env Vars, Docker, Flyway Script) |

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

**Roadmap completo: 15 de 15 fases concluídas.**

- **119 testes** automatizados, todos verdes.
- **13 classes de teste** + 1 config (`PrometheusTestConfig`).
- **78 arquivos** de produção.
- **9 migrations** Flyway (V1 a V9).

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

*(Fases 0 a 14 omitidas aqui para brevidade, mantendo o conteúdo original. Adicionada a Fase 15 abaixo.)*

### Fase 15 — Infraestrutura de Desenvolvimento, CORS e Env Vars

**Endpoints afetados:** Nenhum endpoint novo, mas todos agora lidam com CORS.
**Testes:** +3 (CorsConfigTest) → Total 119.

**Implementação:**
- **CORS Resolvido:** Criado `CorsConfig` (WebMvcConfigurer) e adicionado `.cors(Customizer.withDefaults())` no `SecurityConfig`. Permite `http://localhost:5173`.
- **Variáveis de Ambiente:** `application.yml` parametrizado com `${VAR:default}` para banco, Redis, JWT, porta, etc.
- **Dockerização de Dev:** `docker-compose.yml` configurado para PostgreSQL 16 e Redis 7 com healthchecks e redes.
- **Automação com Script:** Criado `run-dev.sh` que carrega `.env`, sobe o docker (se necessário), espera o Postgres, roda as migrações Flyway (adicionado `flyway-maven-plugin` no `pom.xml`) e inicia o Spring Boot.
- **Dockerfile:** Atualizado para Java 21 (estava 17) e otimizado com multi-stage build.
- **Configuração Local:** `.env.example` criado e `.env` ignorado via `.gitignore`.

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

*(Mantido idêntico ao original)*

---

## Decisões técnicas — Backend

*(Mantidas as originais, adicionadas as novas)*

| Decisão | Por quê |
|---------|---------|
| ... | ... |
| `MethodArgumentNotValidException` → mensagem concatenada `campo: erro` | Feedback único e legível |
| **`.env` + `run-dev.sh`** | **Centraliza variáveis sem hardcode. O script injeta no shell antes do Maven rodar.** |
| **Docker Compose com Healthcheck** | **Garante que o Spring Boot só suba após o Postgres estar aceitando conexões (`pg_isready`).** |
| **Flyway Maven Plugin via `run-dev.sh`** | **Permite rodar `./run-dev.sh info` e `./run-dev.sh migrate` isoladamente.** |

---

## Decisões técnicas — Frontend

*(A confirmar durante a Fase 0)*

Decisões provisórias: *(Mantidas idênticas ao original)*

---

## Pontos de atenção herdados do backend

*(Mantidos os originais, atualizados os itens 15 e adicionado 21)*

1-14. *(Mantidos idênticos ao original)*
15. ✅ **CORS configurado** — `http://localhost:5173` permitido.
16-20. *(Mantidos idênticos ao original)*
21. **Testes de CORS com MockMvc dão falso positivo.** `@AutoConfigureMockMvc` não reproduz o preflight real do Tomcat. Sempre valide CORS com `curl` contra a app rodando.

---

## Bugs encontrados e correções — Backend

*(Mantidos os originais, adicionado o novo)*

| # | Bug | Correção |
|---|-----|----------|
| 1-8 | *(Mantidos idênticos ao original)* | ... |
| 9 | `CorsConfigTest` passava mesmo sem CORS no código de produção | Falso positivo do `MockMvc`. Validação real feita via `curl` e criação do `CorsConfig`. |

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