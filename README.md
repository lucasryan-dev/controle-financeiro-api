# 💰 Controle Financeiro API

API REST para controle de finanças pessoais, desenvolvida com **Java e Spring Boot**. Cada usuário cria sua conta, faz login com JWT e, com acesso apenas aos próprios dados, registra receitas e despesas, organiza os gastos em categorias, acompanha relatórios do mês e recebe **dicas para melhorar sua saúde financeira**, baseadas na regra 50/30/20.

> 🚧 **Projeto em desenvolvimento.** Estou construindo esta API em etapas para estudar e praticar desenvolvimento back-end. Acompanhe a evolução no [roadmap](#️-roadmap) e no histórico de commits.

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot**
- **Spring Web**: API REST
- **Spring Data JPA / Hibernate**: persistência de dados
- **PostgreSQL**: banco de dados relacional
- **Flyway**: versionamento do banco de dados com migrations
- **Spring Security e OAuth2 Resource Server**: autenticação com JWT
- **Bean Validation**: validação dos dados de entrada
- **Lombok**: redução de código repetitivo
- **JUnit 5, Mockito e AssertJ**: testes automatizados
- **Maven**: gerenciamento de dependências
- **Docker e Docker Compose**: containerização da aplicação e do banco
- **springdoc-openapi (Swagger UI)**: documentação interativa da API

## 📐 Arquitetura

O projeto segue a arquitetura em camadas, organizado por funcionalidade:

```
Requisição HTTP (JSON) + token JWT
      ↓
 Spring Security → valida o token e identifica o usuário
      ↓
 Controller   → recebe a requisição, valida o DTO e devolve a resposta
      ↓
  Service     → regras de negócio e conversão entre DTO e entidade
      ↓
 Repository   → acesso ao banco de dados

 GlobalExceptionHandler → intercepta as exceções de todos os controllers
                          e devolve respostas de erro padronizadas
```

```
src/main/java/com/lucasdev/controle_financeiro/
├── auth/
│   ├── SecurityConfig.java                  # regras de acesso, BCrypt e chaves do JWT
│   ├── AuthController.java                  # endpoints de cadastro e login
│   ├── AuthService.java                     # cadastro e verificação de credenciais
│   ├── TokenService.java                    # geração do token JWT
│   ├── RegistroRequest.java / LoginRequest.java
│   └── UsuarioResponse.java / TokenResponse.java
├── categoria/
│   ├── Categoria.java                       # entidade JPA
│   ├── GrupoCategoria.java                  # enum: NECESSIDADE, DESEJO, RENDA
│   ├── CategoriaRequest.java                # DTO de entrada, com validações
│   ├── CategoriaResponse.java               # DTO de saída
│   ├── CategoriaRepository.java             # acesso ao banco
│   ├── CategoriaService.java                # regras de negócio
│   ├── CategoriaController.java             # endpoints REST
│   └── CategoriasIniciais.java              # cria as categorias padrão de cada novo usuário
├── exception/
│   ├── ConflitoException.java               # exceção de dado duplicado (409)
│   ├── CredenciaisInvalidasException.java   # login com e-mail ou senha errados (401)
│   ├── ErroResponse.java                    # formato padrão das respostas de erro
│   ├── RecursoNaoEncontradoException.java   # exceção de recurso inexistente (404)
│   └── GlobalExceptionHandler.java          # tratamento global de erros
├── relatorio/
│   ├── ResumoMensalResponse.java            # DTO do resumo do mês
│   ├── TotalPorCategoria.java               # projeção da consulta agrupada
│   ├── GastoPorCategoriaResponse.java       # DTO dos gastos por categoria
│   ├── IndicadorGrupo.java                  # valor, percentual e meta de um grupo
│   ├── DicasResponse.java                   # DTO das dicas de saúde financeira
│   ├── RelatorioService.java                # cálculos dos relatórios
│   └── RelatorioController.java             # endpoints de relatórios
└── transacao/
    ├── Transacao.java              # entidade JPA
    ├── TipoTransacao.java          # enum: RECEITA, DESPESA
    ├── TransacaoRequest.java       # DTO de entrada, com validações
    ├── TransacaoResponse.java      # DTO de saída
    ├── TransacaoRepository.java    # acesso ao banco
    ├── TransacaoSpecifications.java # filtros dinâmicos da listagem
    ├── TransacaoService.java       # regras de negócio
    └── TransacaoController.java    # endpoints REST
└── usuario/
    ├── Usuario.java                # entidade JPA (senha guardada como hash)
    └── UsuarioRepository.java      # acesso ao banco
```

Na raiz do projeto ficam também o `Dockerfile` (imagem da aplicação), o `docker-compose.yml` (aplicação + PostgreSQL) e o `.dockerignore`.

### Decisões técnicas

- **`BigDecimal` para valores monetários**: evita os erros de arredondamento do `double` (ex.: `0.1 + 0.2 = 0.30000000000000004`).
- **Receitas e despesas na mesma entidade**: diferenciadas pelo campo `tipo`, o que simplifica o cálculo do saldo. O valor é sempre positivo; o `tipo` define se ele soma ou subtrai.
- **Enums salvos como texto** (`EnumType.STRING`): os dados continuam corretos mesmo se a ordem dos valores do enum mudar.
- **Injeção de dependência pelo construtor**: deixa as dependências explícitas e facilita os testes.
- **DTOs com `record`**: a API recebe `TransacaoRequest` e devolve `TransacaoResponse`, sem expor a entidade JPA. O cliente não consegue enviar o `id`, e mudanças na entidade não alteram o contrato da API.
- **Validação com Bean Validation**: dados inválidos são recusados com `400 Bad Request` antes de chegar ao banco.
- **Status HTTP semânticos**: `201` ao criar, `204` ao excluir, `400` para dados inválidos e `404` quando a transação não existe.
- **Tratamento global de erros com `@RestControllerAdvice`**: todos os erros saem no mesmo formato JSON. Erros inesperados são registrados no log, e o cliente recebe só uma mensagem genérica, sem expor detalhes internos como stack trace ou SQL.
- **Mensagens de erro controladas**: para exceções da própria aplicação, a resposta usa a mensagem escrita para o usuário da API. Para exceções do Java e do Spring (parâmetro ausente, tipo errado, data inválida), a API responde com uma mensagem fixa em português, sem expor detalhes internos.
- **Exceções de domínio**: a camada de serviço lança `RecursoNaoEncontradoException` e não conhece nada de HTTP. Quem traduz a exceção para `404` é o tratador global.
- **Categorias como entidade, relacionadas por `@ManyToOne`**: cada transação guarda apenas o id da categoria, em uma chave estrangeira (`categoria_id`). O usuário pode criar novas categorias sem mudar o código, e renomear uma categoria reflete em todas as transações ligadas a ela.
- **Carregamento `LAZY` no relacionamento**: a categoria só é buscada no banco quando é usada, evitando consultas desnecessárias.
- **Grupo na categoria, e não na transação**: classificar cada categoria como necessidade, desejo ou renda uma única vez evita que o usuário precise classificar cada gasto. A categoria "Outros" é tratada como desejo, a opção mais cautelosa.
- **Reaproveitamento de DTOs**: a resposta de transação usa o `CategoriaResponse`, então qualquer campo novo da categoria, como o grupo, aparece automaticamente nas transações.
- **Autenticação stateless com JWT**: o login devolve um token assinado com HS256, válido por 2 horas, que acompanha cada requisição no cabeçalho `Authorization`. O servidor não guarda sessão, e a validação usa o suporte oficial do Spring Security (OAuth2 Resource Server), sem filtro escrito à mão. O CSRF fica desligado porque a API não usa cookies.
- **Senhas com BCrypt**: só o hash da senha é guardado no banco, e ela nunca aparece nas respostas da API.
- **Mensagem genérica no login**: e-mail inexistente e senha errada recebem a mesma resposta (`401`), para não revelar quais e-mails têm conta.
- **Dados isolados por usuário**: transações e categorias têm um dono (`usuario_id`). O id do usuário vem do token assinado, e não da URL, então não pode ser forjado. Todas as consultas filtram pelo dono.
- **404 em vez de 403 para dados de outro usuário**: tentar acessar a transação ou a categoria de outra pessoa responde "não encontrada", sem confirmar que o registro existe.
- **Categorias por usuário**: cada conta nasce com as 8 categorias padrão e pode renomear, excluir e criar as suas. O nome é único por usuário (restrição única composta em `usuario_id` + `nome`).
- **Cadastro transacional**: o usuário e suas categorias iniciais são salvos em uma única transação (`@Transactional`). Se algo falhar, nada fica salvo pela metade.
- **Nomes de categoria únicos**: a regra é garantida em duas camadas. A Service verifica antes de salvar, sem diferenciar maiúsculas e minúsculas, e responde `409 Conflict`; o banco também tem uma constraint `UNIQUE` como proteção final.
- **Exclusão de categoria bloqueada quando em uso**: em vez de apagar as transações junto (cascata) ou movê-las sem o usuário pedir, a API recusa a exclusão com `409 Conflict`. Assim o histórico financeiro nunca é perdido por acidente.
- **Consultas derivadas do Spring Data**: métodos como `existsByNomeIgnoreCase`, `existsByNomeIgnoreCaseAndIdNot` e `existsByCategoriaId` geram o SQL a partir do nome, inclusive navegando pelo relacionamento entre entidades, sem consulta escrita à mão.
- **Somas feitas no banco com JPQL**: os totais do relatório são calculados com `SUM` direto no banco, em vez de carregar todas as transações para a memória. As consultas usam parâmetros nomeados, o que protege contra SQL Injection, e `COALESCE` para devolver `0` em meses sem transações.
- **Filtros dinâmicos com `Specification`**: cada filtro é uma peça independente, e só as peças informadas na requisição entram na consulta. Isso evita criar um método de consulta para cada combinação de filtros.
- **Paginação na listagem**: as transações são devolvidas em páginas, ordenadas das mais recentes para as mais antigas, para a API continuar rápida mesmo com milhares de registros. A resposta usa `PagedModel`, o formato estável recomendado pelo Spring Data.
- **Agrupamento no banco com projeção**: os gastos por categoria são somados com `GROUP BY` e já ordenados pelo banco. O resultado é convertido direto em objetos com `SELECT new` (projeção com construtor), e a Service só calcula os percentuais.
- **Dicas geradas por regras de negócio**: as dicas não dependem de inteligência artificial, e sim de regras claras e testáveis na camada de serviço, baseadas na regra 50/30/20. A poupança é calculada como o saldo do mês, e cada grupo é representado pelo mesmo record (`IndicadorGrupo`), evitando campos repetidos.
- **Cálculos monetários com `BigDecimal`**: percentuais com 2 casas decimais e arredondamento `HALF_UP`, multiplicando antes de dividir para não perder precisão. Meses sem receitas devolvem percentual `0` em vez de erro de divisão por zero.
- **Testes unitários com mocks**: as regras de negócio dos relatórios, das dicas, das categorias e das transações são testadas sem banco de dados, com os repositórios substituídos por mocks do Mockito. Nos cenários de erro, os testes verificam também que nada foi salvo ou apagado (`verify(..., never())`). Os testes rodam em milissegundos e cobrem os casos de borda, como meses sem receitas e poupança negativa. Valores `BigDecimal` são comparados com `isEqualByComparingTo`, que ignora diferenças de escala.
- **Docker com multi-stage build**: a imagem é montada em duas etapas. A primeira compila o projeto com Maven e o JDK completo; a segunda roda só o `.jar` sobre uma imagem com o JRE, menor e com menos coisas instaladas. O `pom.xml` é copiado antes do código para aproveitar o cache do Docker e não baixar as dependências a cada mudança.
- **Docker Compose com healthcheck e volume**: a API só sobe depois que o PostgreSQL responde ao `pg_isready`, e os dados ficam em um volume, sobrevivendo à remoção dos containers. Dentro da rede do Compose, a API encontra o banco pelo nome do serviço (`db`).
- **Documentação gerada a partir do código**: o springdoc lê os controllers, os DTOs e as anotações de validação e monta a especificação OpenAPI sozinho, então a documentação nunca fica desatualizada em relação à API. O esquema de segurança Bearer/JWT foi declarado uma vez, e o Swagger UI envia o token em todas as requisições.
- **Banco versionado com Flyway**: as tabelas são criadas por migrations SQL numeradas (`src/main/resources/db/migration`), que o Flyway executa em ordem ao iniciar a aplicação e registra na tabela `flyway_schema_history`. Cada mudança no banco vira uma nova migration, com histórico no Git, e migrations já aplicadas nunca são editadas. O Hibernate fica em modo `validate`: não cria nem altera tabelas, só confere se as entidades batem com o banco, e a aplicação não sobe se houver divergência.
- **PostgreSQL com usuário dedicado**: a aplicação acessa o banco com um usuário próprio, sem privilégios de administrador. As credenciais e a chave do JWT podem ser definidas por variáveis de ambiente, para que segredos reais nunca fiquem no código.

## 📖 Documentação interativa (Swagger)

Com a aplicação rodando, a documentação completa fica em:

```
http://localhost:8080/swagger-ui.html
```

Ela é gerada automaticamente a partir dos controllers e DTOs, e permite testar todos os endpoints pelo navegador, sem Postman:

1. Em **`POST /auth/login`**, clique em **Try it out**, informe e-mail e senha e execute.
2. Copie o `token` da resposta.
3. Clique em **Authorize** 🔓 no topo da página e cole o token (sem a palavra `Bearer`).
4. Pronto: as próximas requisições já vão autenticadas.

A especificação OpenAPI em JSON fica em `http://localhost:8080/v3/api-docs`. A documentação é pública, mas os endpoints continuam exigindo token.

## 📌 Endpoints

### 🔐 Autenticação

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/auth/registro` | Cria uma conta | `201 Created` | `400`, `409` |
| `POST` | `/auth/login` | Faz login e devolve o token | `200 OK` | `400`, `401` |

**Cadastro** `POST /auth/registro`
```json
{ "nome": "Ana", "email": "ana@email.com", "senha": "senhadaana123" }
```

**Login** `POST /auth/login`
```json
{ "email": "ana@email.com", "senha": "senhadaana123" }
```

**Resposta** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-21T19:30:00Z"
}
```

Todos os outros endpoints exigem o token no cabeçalho:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Sem token, ou com um token inválido ou expirado, a resposta é `401 Unauthorized`. Cada usuário só enxerga e altera as **próprias** transações e categorias.

### Transações, categorias e relatórios

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/transacoes` | Cadastra uma transação | `201 Created` | `400`, `404` |
| `GET` | `/transacoes` | Lista as transações, com filtros e paginação | `200 OK` | `400` |
| `GET` | `/transacoes/{id}` | Busca uma transação pelo id | `200 OK` | `404` |
| `PUT` | `/transacoes/{id}` | Atualiza uma transação | `200 OK` | `400`, `404` |
| `DELETE` | `/transacoes/{id}` | Exclui uma transação | `204 No Content` | `404` |
| `POST` | `/categorias` | Cadastra uma categoria | `201 Created` | `400`, `409` |
| `GET` | `/categorias` | Lista todas as categorias | `200 OK` | |
| `PUT` | `/categorias/{id}` | Renomeia uma categoria | `200 OK` | `400`, `404`, `409` |
| `DELETE` | `/categorias/{id}` | Exclui uma categoria sem transações | `204 No Content` | `404`, `409` |
| `GET` | `/relatorios/resumo?ano={ano}&mes={mes}` | Resumo financeiro do mês | `200 OK` | `400` |
| `GET` | `/relatorios/gastos-por-categoria?ano={ano}&mes={mes}` | Despesas do mês agrupadas por categoria | `200 OK` | `400` |
| `GET` | `/relatorios/dicas?ano={ano}&mes={mes}` | Dicas de saúde financeira (regra 50/30/20) | `200 OK` | `400` |

### Relatório: resumo do mês

**Requisição**
```http
GET /relatorios/resumo?ano=2026&mes=11
```

**Resposta** `200 OK`
```json
{
  "ano": 2026,
  "mes": 11,
  "totalReceitas": 3000.00,
  "totalDespesas": 1250.90,
  "saldo": 1749.10,
  "percentualGasto": 41.70
}
```

- `saldo`: receitas menos despesas do mês
- `percentualGasto`: quanto das receitas foi gasto, em porcentagem
- Meses sem transações devolvem todos os valores como `0`

### Relatório: gastos por categoria

**Requisição**
```http
GET /relatorios/gastos-por-categoria?ano=2026&mes=11
```

**Resposta** `200 OK`
```json
[
  { "categoria": "Moradia", "total": 800.00, "percentual": 63.95 },
  { "categoria": "Alimentação", "total": 450.90, "percentual": 36.05 }
]
```

- Considera apenas as **despesas** do mês
- Ordenado do maior para o menor gasto
- `percentual`: quanto cada categoria representa do total de despesas do mês
- Meses sem despesas devolvem uma lista vazia

### 💡 Dicas de saúde financeira

O app compara os gastos do mês com a **regra 50/30/20** e gera dicas personalizadas:

| Grupo | Recomendado | Como é calculado |
|---|---|---|
| Necessidades | até **50%** da renda | despesas de categorias do grupo `NECESSIDADE` |
| Desejos | até **30%** da renda | despesas de categorias do grupo `DESEJO` |
| Poupança | pelo menos **20%** da renda | receitas menos despesas do mês |

**Requisição**
```http
GET /relatorios/dicas?ano=2026&mes=11
```

**Resposta** `200 OK`
```json
{
  "necessidades": { "valor": 1350.00, "percentual": 45.00, "recomendado": 50 },
  "desejos":      { "valor": 1000.00, "percentual": 33.33, "recomendado": 30 },
  "poupanca":     { "valor": 650.00,  "percentual": 21.67, "recomendado": 20 },
  "dicas": [
    "Seus gastos com necessidades estão dentro do recomendado.",
    "Atenção: seus gastos com desejos estão acima dos 30% recomendados. Revise seus gastos para não faltar com suas necessidades.",
    "Parabéns! Você está poupando pelo menos 20% da sua renda. Você está no caminho certo."
  ]
}
```

**Regras das dicas:**
- **Sem receitas no mês**: o app pede que as receitas sejam cadastradas, sem calcular as demais dicas.
- **Necessidades e desejos**: alerta quando o percentual passa do recomendado.
- **Poupança**: três situações. Parabéns com 20% ou mais, aviso quando está poupando menos que isso, e alerta quando os gastos passam da renda (poupança negativa).

### Categorias iniciais

Cada categoria pertence a um **grupo**, usado nas dicas de saúde financeira (regra 50/30/20):

| Grupo | Significado |
|---|---|
| `NECESSIDADE` | gastos essenciais para viver |
| `DESEJO` | gastos que podem ser reduzidos |
| `RENDA` | categorias de receita, que não entram na conta dos gastos |

Cada nova conta já nasce com estas categorias, que pertencem só àquele usuário:

| Categoria | Grupo |
|---|---|
| Alimentação, Transporte, Moradia, Saúde, Educação | `NECESSIDADE` |
| Lazer, Outros | `DESEJO` |
| Salário | `RENDA` |

O usuário pode criar novas categorias pelo endpoint `POST /categorias`, além de renomear e excluir as suas.

**Exemplo** `POST /categorias`
```json
{ "nome": "Pet", "grupo": "NECESSIDADE" }
```

**Resposta** `201 Created`
```json
{ "id": 9, "nome": "Pet", "grupo": "NECESSIDADE" }
```

O nome é obrigatório, tem no máximo 50 caracteres e não pode se repetir **entre as categorias do mesmo usuário**, sem diferenciar maiúsculas e minúsculas: se ele já tem "Pet", "pet" é recusado com `409 Conflict`. Usuários diferentes podem ter categorias com o mesmo nome. O grupo também é obrigatório.

**Regras para renomear e excluir:**
- Ao renomear, a verificação de nome repetido ignora a própria categoria: "Lazer" pode virar "LAZER".
- Renomear uma categoria atualiza automaticamente todas as transações ligadas a ela.
- Uma categoria usada por alguma transação **não pode ser excluída** (`409 Conflict`). Para excluí-la, primeiro mude a categoria dessas transações.

### Exemplo: cadastrar uma transação

**Requisição**
```http
POST /transacoes
Content-Type: application/json
```
```json
{
  "descricao": "Salário",
  "valor": 3000.00,
  "tipo": "RECEITA",
  "categoriaId": 7,
  "data": "2026-11-05"
}
```

**Resposta** `201 Created`
```json
{
  "id": 1,
  "descricao": "Salário",
  "valor": 3000.00,
  "tipo": "RECEITA",
  "categoria": { "id": 7, "nome": "Salário", "grupo": "RENDA" },
  "data": "2026-11-05"
}
```

A transação recebe o **id** de uma categoria existente (consulte os ids em `GET /categorias`) e a resposta traz a categoria completa, com id, nome e grupo.

### Listagem com filtros e paginação

Todos os filtros são opcionais e podem ser combinados:

| Parâmetro | Exemplo | Filtra por |
|---|---|---|
| `tipo` | `DESPESA` | receitas ou despesas |
| `categoriaId` | `1` | uma categoria |
| `inicio` | `2026-11-01` | transações a partir desta data |
| `fim` | `2026-11-30` | transações até esta data |
| `page` | `0` | número da página (começa em 0) |
| `size` | `20` | itens por página (padrão: 20) |
| `sort` | `valor,desc` | ordenação (padrão: `data,desc`, as mais recentes primeiro) |

**Exemplo**
```http
GET /transacoes?tipo=DESPESA&inicio=2026-11-01&fim=2026-11-30&size=2
```

**Resposta** `200 OK`
```json
{
  "content": [
    { "id": 5, "descricao": "Presentes", "valor": 400.00, "tipo": "DESPESA", "categoria": { "id": 8, "nome": "Outros", "grupo": "DESEJO" }, "data": "2026-11-20" },
    { "id": 4, "descricao": "Cinema e restaurante", "valor": 600.00, "tipo": "DESPESA", "categoria": { "id": 6, "nome": "Lazer", "grupo": "DESEJO" }, "data": "2026-11-15" }
  ],
  "page": { "size": 2, "number": 0, "totalElements": 4, "totalPages": 2 }
}
```

### Regras de validação

| Campo | Regras |
|---|---|
| `descricao` | obrigatória, até 100 caracteres |
| `valor` | obrigatório, maior que zero, no máximo 2 casas decimais |
| `tipo` | obrigatório: `RECEITA` ou `DESPESA` |
| `categoriaId` | obrigatório, id de uma categoria **do próprio usuário** (senão, `404 Not Found`) |
| `data` | obrigatória, no formato `AAAA-MM-DD` |

Requisições que não seguem essas regras recebem `400 Bad Request`.

### Respostas de erro

Todos os erros seguem o mesmo formato:

**Dados inválidos** `400 Bad Request`
```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "campos": {
    "valor": "O valor deve ser maior que zero",
    "data": "A data é obrigatória"
  },
  "timestamp": "2026-11-15T16:55:12.345"
}
```

**JSON malformado ou valor de enum inexistente** `400 Bad Request`
```json
{
  "status": 400,
  "mensagem": "JSON inválido ou com valores não aceitos",
  "campos": null,
  "timestamp": "2026-11-15T16:56:03.120"
}
```

**Parâmetro da URL inválido ou ausente** `400 Bad Request`

Por exemplo, `GET /relatorios/resumo?ano=2026&mes=13`, sem o `mes`, com `mes=abc` ou uma ordenação por campo inexistente (`GET /transacoes?sort=banana,desc`):
```json
{
  "status": 400,
  "mensagem": "Parâmetro inválido ou ausente na requisição",
  "campos": null,
  "timestamp": "2026-11-17T10:53:44.610"
}
```

**E-mail ou senha inválidos** `401 Unauthorized`
```json
{
  "status": 401,
  "mensagem": "E-mail ou senha inválidos",
  "campos": null,
  "timestamp": "2026-11-21T17:20:12.410"
}
```

**Transação ou categoria não encontrada, ou de outro usuário** `404 Not Found`
```json
{
  "status": 404,
  "mensagem": "Categoria não encontrada",
  "campos": null,
  "timestamp": "2026-11-16T16:01:40.871"
}
```

**Categoria com nome repetido** `409 Conflict`
```json
{
  "status": 409,
  "mensagem": "Já existe uma categoria com esse nome",
  "campos": null,
  "timestamp": "2026-11-16T12:50:21.512"
}
```

**Categoria em uso** `409 Conflict`
```json
{
  "status": 409,
  "mensagem": "Categoria em uso por transações e não pode ser excluída",
  "campos": null,
  "timestamp": "2026-11-16T16:58:02.233"
}
```

**Erro inesperado** `500 Internal Server Error`
```json
{
  "status": 500,
  "mensagem": "Erro interno no servidor",
  "campos": null,
  "timestamp": "2026-11-15T16:57:18.004"
}
```

## ▶️ Como rodar

### Opção 1: com Docker (recomendado)

**Pré-requisitos:** Git e Docker. Não é preciso instalar Java nem PostgreSQL.

```bash
git clone https://github.com/lucasryan-dev/controle-financeiro-api.git
cd controle-financeiro-api
docker compose up --build
```

Esse comando sobe dois containers:

| Container | O que é | Porta |
|---|---|---|
| `controle-financeiro-db` | PostgreSQL 16, com o banco e o usuário já criados | `5432` |
| `controle-financeiro-api` | a aplicação, compilada dentro do Docker | `8080` |

A API só inicia depois que o banco está pronto para receber conexões. Os dados ficam guardados em um volume do Docker e continuam lá mesmo depois de parar os containers.

```bash
docker compose down      # para os containers e mantém os dados
docker compose down -v   # para os containers e apaga os dados
```

Com tudo no ar, abra **http://localhost:8080/swagger-ui.html** para explorar e testar a API.

### Opção 2: sem Docker

**Pré-requisitos:** Java 21+, Git e PostgreSQL. O Maven não precisa estar instalado, porque o projeto usa o Maven Wrapper.

**1. Criar o banco de dados**

```bash
sudo -u postgres psql
```
```sql
CREATE USER financeiro WITH PASSWORD 'financeiro';
CREATE DATABASE controle_financeiro OWNER financeiro;
\q
```

As tabelas são criadas automaticamente pelo Flyway na primeira execução, a partir das migrations em `src/main/resources/db/migration`.

**2. Rodar a aplicação**

```bash
git clone https://github.com/lucasryan-dev/controle-financeiro-api.git
cd controle-financeiro-api

# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

> Também é possível subir só o banco com Docker (`docker compose up db`) e rodar a aplicação pela IDE.

A API sobe em `http://localhost:8080`. Use o Postman, o Insomnia ou outro cliente HTTP para testar os endpoints.

### Variáveis de ambiente (opcionais)

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/controle_financeiro` | endereço do banco |
| `DB_USERNAME` | `financeiro` | usuário do banco |
| `DB_PASSWORD` | `financeiro` | senha do banco |
| `JWT_SECRET` | chave de desenvolvimento | chave que assina os tokens (mínimo de 32 caracteres) |

Os valores padrão servem para desenvolvimento local. No Docker Compose, a aplicação usa `DB_URL` apontando para o serviço `db`. Em produção, defina as variáveis com credenciais seguras, principalmente a `JWT_SECRET`: quem tem a chave consegue gerar tokens válidos.

## 🧪 Testes

```bash
# Linux/Mac
./mvnw test

# Windows
mvnw.cmd test
```

Os testes unitários cobrem as regras de negócio dos relatórios, das categorias e das transações:

**Relatórios e dicas**

| Cenário testado | Garante que |
|---|---|
| Resumo do mês | saldo e percentual gasto são calculados corretamente |
| Mês sem receitas | o percentual é `0`, sem erro de divisão por zero |
| Desejos acima de 30% | a dica de alerta dos desejos é gerada |
| Poupança abaixo de 20% | o app avisa que está poupando pouco |
| Gastos maiores que a renda | o alerta de poupança negativa é gerado |
| Renda zero | apenas a dica de cadastrar receitas é exibida |

**Categorias**

| Cenário testado | Garante que |
|---|---|
| Nome novo | a categoria é salva e devolvida com o id |
| Nome repetido | a API recusa com `409` e nada é salvo no banco |
| Excluir categoria em uso | a API recusa com `409` e a categoria não é apagada |
| Excluir categoria inexistente | a API responde `404` e nada é apagado |

**Transações**

| Cenário testado | Garante que |
|---|---|
| Criar com categoria do usuário | a transação é salva com a categoria e devolvida com o id |
| Criar com categoria inexistente | a API responde `404` e nada é salvo |
| Criar com categoria de outro usuário | a API responde `404` e nada é salvo |
| Buscar transação inexistente | a API responde `404` |
| Atualizar | os dados mudam, mas o id da transação é mantido |

> O teste `contextLoads`, gerado pelo Spring Initializr, sobe a aplicação completa e precisa do PostgreSQL rodando.

## 🗺️ Roadmap

- [x] Entidade `Transacao` com persistência via JPA
- [x] Cadastrar e listar transações
- [x] Buscar, atualizar e excluir transações (CRUD completo)
- [x] DTOs e validação dos dados de entrada
- [x] Tratamento de erros padronizado
- [x] Cadastro de categorias, com categorias iniciais
- [x] Relacionamento entre transações e categorias (`@ManyToOne`)
- [x] Renomear e excluir categorias (bloqueando a exclusão de categorias em uso)
- [x] Relatório: resumo do mês (receitas, despesas, saldo e percentual gasto)
- [x] Relatório: gastos por categoria
- [x] Filtros por período, tipo e categoria, com paginação
- [x] Classificação das categorias em necessidade, desejo e renda
- [x] Dicas de saúde financeira baseadas na regra 50/30/20
- [x] Testes unitários dos relatórios e das dicas (JUnit, Mockito e AssertJ)
- [x] Testes unitários das regras de categorias
- [x] Testes unitários das regras de transações
- [ ] Testes dos endpoints (MockMvc)
- [x] Documentação com Swagger/OpenAPI
- [x] Banco de dados PostgreSQL
- [x] Migrations com Flyway
- [x] Docker e Docker Compose
- [x] Autenticação com JWT (Spring Security), com cadastro e login
- [x] Dados isolados por usuário, incluindo categorias próprias
- [ ] Dicas personalizadas com IA (Spring AI), usando as regras atuais como base e sem enviar dados pessoais

## 👤 Autor

**Lucas Ryan**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-ryan-33848524b/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/lucasryan-dev)
