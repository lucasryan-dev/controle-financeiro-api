# 💰 Controle Financeiro API

API REST para controle de finanças pessoais, desenvolvida com **Java e Spring Boot**. Com ela você registra receitas e despesas, acompanha o saldo e, nas próximas versões, recebe **dicas para melhorar sua saúde financeira** a partir dos seus gastos.

> 🚧 **Projeto em desenvolvimento.** Estou construindo esta API em etapas para estudar e praticar desenvolvimento back-end. Acompanhe a evolução no [roadmap](#️-roadmap) e no histórico de commits.

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot**
- **Spring Web**: API REST
- **Spring Data JPA / Hibernate**: persistência de dados
- **PostgreSQL**: banco de dados relacional
- **Bean Validation**: validação dos dados de entrada
- **Lombok**: redução de código repetitivo
- **Maven**: gerenciamento de dependências

## 📐 Arquitetura

O projeto segue a arquitetura em camadas, organizado por funcionalidade:

```
Requisição HTTP (JSON)
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
├── categoria/
│   ├── Categoria.java                       # entidade JPA
│   ├── CategoriaRequest.java                # DTO de entrada, com validações
│   ├── CategoriaResponse.java               # DTO de saída
│   ├── CategoriaRepository.java             # acesso ao banco
│   ├── CategoriaService.java                # regras de negócio
│   ├── CategoriaController.java             # endpoints REST
│   └── CategoriaSeeder.java                 # cadastra as categorias iniciais
├── exception/
│   ├── ConflitoException.java               # exceção de dado duplicado (409)
│   ├── ErroResponse.java                    # formato padrão das respostas de erro
│   ├── RecursoNaoEncontradoException.java   # exceção de recurso inexistente (404)
│   └── GlobalExceptionHandler.java          # tratamento global de erros
├── relatorio/
│   ├── ResumoMensalResponse.java            # DTO do resumo do mês
│   ├── RelatorioService.java                # cálculos dos relatórios
│   └── RelatorioController.java             # endpoints de relatórios
└── transacao/
    ├── Transacao.java              # entidade JPA
    ├── TipoTransacao.java          # enum: RECEITA, DESPESA
    ├── TransacaoRequest.java       # DTO de entrada, com validações
    ├── TransacaoResponse.java      # DTO de saída
    ├── TransacaoRepository.java    # acesso ao banco
    ├── TransacaoService.java       # regras de negócio
    └── TransacaoController.java    # endpoints REST
```

### Decisões técnicas

- **`BigDecimal` para valores monetários**: evita os erros de arredondamento do `double` (ex.: `0.1 + 0.2 = 0.30000000000000004`).
- **Receitas e despesas na mesma entidade**: diferenciadas pelo campo `tipo`, o que simplifica o cálculo do saldo. O valor é sempre positivo; o `tipo` define se ele soma ou subtrai.
- **Enums salvos como texto** (`EnumType.STRING`): os dados continuam corretos mesmo se a ordem dos valores do enum mudar.
- **Injeção de dependência pelo construtor**: deixa as dependências explícitas e facilita os testes.
- **DTOs com `record`**: a API recebe `TransacaoRequest` e devolve `TransacaoResponse`, sem expor a entidade JPA. O cliente não consegue enviar o `id`, e mudanças na entidade não alteram o contrato da API.
- **Validação com Bean Validation**: dados inválidos são recusados com `400 Bad Request` antes de chegar ao banco.
- **Status HTTP semânticos**: `201` ao criar, `204` ao excluir, `400` para dados inválidos e `404` quando a transação não existe.
- **Tratamento global de erros com `@RestControllerAdvice`**: todos os erros saem no mesmo formato JSON. Erros inesperados são registrados no log, e o cliente recebe só uma mensagem genérica, sem expor detalhes internos como stack trace ou SQL.
- **Exceções de domínio**: a camada de serviço lança `RecursoNaoEncontradoException` e não conhece nada de HTTP. Quem traduz a exceção para `404` é o tratador global.
- **Categorias como entidade, relacionadas por `@ManyToOne`**: cada transação guarda apenas o id da categoria, em uma chave estrangeira (`categoria_id`). O usuário pode criar novas categorias sem mudar o código, e renomear uma categoria reflete em todas as transações ligadas a ela.
- **Carregamento `LAZY` no relacionamento**: a categoria só é buscada no banco quando é usada, evitando consultas desnecessárias.
- **Categorias iniciais com seeder idempotente**: ao subir, a aplicação cadastra as categorias padrão apenas se a tabela estiver vazia. Rodar uma ou várias vezes produz o mesmo resultado.
- **Nomes de categoria únicos**: a regra é garantida em duas camadas. A Service verifica antes de salvar, sem diferenciar maiúsculas e minúsculas, e responde `409 Conflict`; o banco também tem uma constraint `UNIQUE` como proteção final.
- **Exclusão de categoria bloqueada quando em uso**: em vez de apagar as transações junto (cascata) ou movê-las sem o usuário pedir, a API recusa a exclusão com `409 Conflict`. Assim o histórico financeiro nunca é perdido por acidente.
- **Consultas derivadas do Spring Data**: métodos como `existsByNomeIgnoreCase`, `existsByNomeIgnoreCaseAndIdNot` e `existsByCategoriaId` geram o SQL a partir do nome, inclusive navegando pelo relacionamento entre entidades, sem consulta escrita à mão.
- **Somas feitas no banco com JPQL**: os totais do relatório são calculados com `SUM` direto no banco, em vez de carregar todas as transações para a memória. As consultas usam parâmetros nomeados, o que protege contra SQL Injection, e `COALESCE` para devolver `0` em meses sem transações.
- **Cálculos monetários com `BigDecimal`**: percentuais com 2 casas decimais e arredondamento `HALF_UP`, multiplicando antes de dividir para não perder precisão. Meses sem receitas devolvem percentual `0` em vez de erro de divisão por zero.
- **PostgreSQL com usuário dedicado**: a aplicação acessa o banco com um usuário próprio, sem privilégios de administrador. As credenciais podem ser definidas por variáveis de ambiente, para que senhas reais nunca fiquem no código.

## 📌 Endpoints

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/transacoes` | Cadastra uma transação | `201 Created` | `400`, `404` |
| `GET` | `/transacoes` | Lista todas as transações | `200 OK` | |
| `GET` | `/transacoes/{id}` | Busca uma transação pelo id | `200 OK` | `404` |
| `PUT` | `/transacoes/{id}` | Atualiza uma transação | `200 OK` | `400`, `404` |
| `DELETE` | `/transacoes/{id}` | Exclui uma transação | `204 No Content` | `404` |
| `POST` | `/categorias` | Cadastra uma categoria | `201 Created` | `400`, `409` |
| `GET` | `/categorias` | Lista todas as categorias | `200 OK` | |
| `PUT` | `/categorias/{id}` | Renomeia uma categoria | `200 OK` | `400`, `404`, `409` |
| `DELETE` | `/categorias/{id}` | Exclui uma categoria sem transações | `204 No Content` | `404`, `409` |
| `GET` | `/relatorios/resumo?ano={ano}&mes={mes}` | Resumo financeiro do mês | `200 OK` | |

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

### Categorias iniciais

Na primeira execução, a aplicação cadastra automaticamente: Alimentação, Transporte, Moradia, Saúde, Educação, Lazer, Salário e Outros. Novas categorias podem ser criadas pelo endpoint `POST /categorias`.

**Exemplo** `POST /categorias`
```json
{ "nome": "Pet" }
```

**Resposta** `201 Created`
```json
{ "id": 9, "nome": "Pet" }
```

O nome é obrigatório, tem no máximo 50 caracteres e não pode se repetir, sem diferenciar maiúsculas e minúsculas: se "Pet" já existe, "pet" é recusado com `409 Conflict`.

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
  "categoria": { "id": 7, "nome": "Salário" },
  "data": "2026-11-05"
}
```

A transação recebe o **id** de uma categoria existente (consulte os ids em `GET /categorias`) e a resposta traz a categoria completa, com id e nome.

### Regras de validação

| Campo | Regras |
|---|---|
| `descricao` | obrigatória, até 100 caracteres |
| `valor` | obrigatório, maior que zero, no máximo 2 casas decimais |
| `tipo` | obrigatório: `RECEITA` ou `DESPESA` |
| `categoriaId` | obrigatório, id de uma categoria existente (senão, `404 Not Found`) |
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

**Transação ou categoria não encontrada** `404 Not Found`
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

**Pré-requisitos:** Java 21+, Git e PostgreSQL. O Maven não precisa estar instalado, porque o projeto usa o Maven Wrapper.

### 1. Criar o banco de dados

```bash
sudo -u postgres psql
```
```sql
CREATE USER financeiro WITH PASSWORD 'financeiro';
CREATE DATABASE controle_financeiro OWNER financeiro;
\q
```

As tabelas são criadas automaticamente pela aplicação na primeira execução.

### 2. Rodar a aplicação

```bash
# clonar o repositório
git clone https://github.com/lucasryan-dev/controle-financeiro-api.git
cd controle-financeiro-api

# rodar a aplicação (Linux/Mac)
./mvnw spring-boot:run

# rodar a aplicação (Windows)
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Use o Postman, o Insomnia ou outro cliente HTTP para testar os endpoints.

### Variáveis de ambiente (opcionais)

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_USERNAME` | `financeiro` | usuário do banco |
| `DB_PASSWORD` | `financeiro` | senha do banco |

Os valores padrão servem para desenvolvimento local. Em produção, defina as variáveis com credenciais seguras.

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
- [ ] Relatório: gastos por categoria
- [ ] Filtros por período, tipo e categoria, com paginação
- [ ] Dicas de saúde financeira baseadas na regra 50/30/20
- [ ] Testes automatizados (JUnit, Mockito e MockMvc)
- [ ] Documentação com Swagger/OpenAPI
- [x] Banco de dados PostgreSQL
- [ ] Migrations com Flyway
- [ ] Docker e docker-compose
- [ ] Autenticação com JWT (Spring Security)

## 👤 Autor

**Lucas Ryan**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-ryan-33848524b/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/lucasryan-dev)
