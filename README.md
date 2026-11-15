# 💰 Controle Financeiro API

API REST para controle de finanças pessoais, desenvolvida com **Java e Spring Boot**. Com ela você registra receitas e despesas, acompanha o saldo e, nas próximas versões, recebe **dicas para melhorar sua saúde financeira** a partir dos seus gastos.

> 🚧 **Projeto em desenvolvimento.** Estou construindo esta API em etapas para estudar e praticar desenvolvimento back-end. Acompanhe a evolução no [roadmap](#️-roadmap) e no histórico de commits.

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot**
- **Spring Web**: API REST
- **Spring Data JPA / Hibernate**: persistência de dados
- **H2 Database**: banco em memória para desenvolvimento
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
├── exception/
│   ├── ErroResponse.java                    # formato padrão das respostas de erro
│   ├── RecursoNaoEncontradoException.java   # exceção de recurso inexistente (404)
│   └── GlobalExceptionHandler.java          # tratamento global de erros
└── transacao/
    ├── Transacao.java              # entidade JPA
    ├── TipoTransacao.java          # enum: RECEITA, DESPESA
    ├── Categoria.java              # enum: ALIMENTACAO, TRANSPORTE...
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

## 📌 Endpoints

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/transacoes` | Cadastra uma transação | `201 Created` | `400` |
| `GET` | `/transacoes` | Lista todas as transações | `200 OK` | |
| `GET` | `/transacoes/{id}` | Busca uma transação pelo id | `200 OK` | `404` |
| `PUT` | `/transacoes/{id}` | Atualiza uma transação | `200 OK` | `400`, `404` |
| `DELETE` | `/transacoes/{id}` | Exclui uma transação | `204 No Content` | `404` |

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
  "categoria": "SALARIO",
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
  "categoria": "SALARIO",
  "data": "2026-11-05"
}
```

### Regras de validação

| Campo | Regras |
|---|---|
| `descricao` | obrigatória, até 100 caracteres |
| `valor` | obrigatório, maior que zero, no máximo 2 casas decimais |
| `tipo` | obrigatório: `RECEITA` ou `DESPESA` |
| `categoria` | obrigatória: `ALIMENTACAO`, `TRANSPORTE`, `MORADIA`, `SAUDE`, `EDUCACAO`, `LAZER`, `SALARIO` ou `OUTROS` |
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

**Transação não encontrada** `404 Not Found`
```json
{
  "status": 404,
  "mensagem": "Transação não encontrada",
  "campos": null,
  "timestamp": "2026-11-15T16:56:40.871"
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

**Pré-requisitos:** Java 21+ e Git. O Maven não precisa estar instalado, porque o projeto usa o Maven Wrapper.

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

> O banco H2 roda em memória: os dados são apagados sempre que a aplicação reinicia.

## 🗺️ Roadmap

- [x] Entidade `Transacao` com persistência via JPA
- [x] Cadastrar e listar transações
- [x] Buscar, atualizar e excluir transações (CRUD completo)
- [x] DTOs e validação dos dados de entrada
- [x] Tratamento de erros padronizado
- [ ] Categorias como entidade (relacionamento entre tabelas)
- [ ] Relatórios: saldo do mês e gastos por categoria
- [ ] Filtros por período, tipo e categoria, com paginação
- [ ] Dicas de saúde financeira baseadas na regra 50/30/20
- [ ] Testes automatizados (JUnit, Mockito e MockMvc)
- [ ] Documentação com Swagger/OpenAPI
- [ ] Banco PostgreSQL e Docker
- [ ] Autenticação com JWT (Spring Security)

## 👤 Autor

**Lucas Ryan**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-ryan-33848524b/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/lucasryan-dev)
