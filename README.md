# 💰 Controle Financeiro API

API REST para controle de finanças pessoais, desenvolvida com **Java e Spring Boot**. Com ela você registra receitas e despesas, acompanha o saldo e, nas próximas versões, recebe **dicas para melhorar sua saúde financeira** a partir dos seus gastos.

> 🚧 **Projeto em desenvolvimento.** Estou construindo esta API em etapas para estudar e praticar desenvolvimento back-end. Acompanhe a evolução no [roadmap](#-roadmap) e no histórico de commits.

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
Requisição HTTP
      ↓
 Controller   → recebe a requisição e devolve a resposta
      ↓
  Service     → regras de negócio
      ↓
 Repository   → acesso ao banco de dados
```

```
src/main/java/com/lucasdev/controle_financeiro/
└── transacao/
    ├── Transacao.java              # entidade JPA
    ├── TipoTransacao.java          # enum: RECEITA, DESPESA
    ├── Categoria.java              # enum: ALIMENTACAO, TRANSPORTE...
    ├── TransacaoRepository.java    # acesso ao banco
    ├── TransacaoService.java       # regras de negócio
    └── TransacaoController.java    # endpoints REST
```

### Decisões técnicas

- **`BigDecimal` para valores monetários**: evita os erros de arredondamento do `double` (ex.: `0.1 + 0.2 = 0.30000000000000004`).
- **Receitas e despesas na mesma entidade**: diferenciadas pelo campo `tipo`, o que simplifica o cálculo do saldo.
- **Enums salvos como texto** (`EnumType.STRING`): os dados continuam corretos mesmo se a ordem dos valores do enum mudar.
- **Injeção de dependência pelo construtor**: deixa as dependências explícitas e facilita os testes.

## 📌 Endpoints

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/transacoes` | Cadastra uma transação | `201 Created` |
| `GET` | `/transacoes` | Lista todas as transações | `200 OK` |

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

**Valores aceitos**
- `tipo`: `RECEITA`, `DESPESA`
- `categoria`: `ALIMENTACAO`, `TRANSPORTE`, `MORADIA`, `SAUDE`, `EDUCACAO`, `LAZER`, `SALARIO`, `OUTROS`

## ▶️ Como rodar

**Pré-requisitos:** Java 21+ e Git. O Maven não precisa estar instalado, porque o projeto usa o Maven Wrapper.

```bash
# clonar o repositório
git clone https://github.com/SEU_USUARIO/controle-financeiro.git
cd controle-financeiro

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
- [ ] Buscar, atualizar e excluir transações (CRUD completo)
- [ ] DTOs e validação dos dados de entrada
- [ ] Tratamento de erros padronizado
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

[![LinkedIn](www.linkedin.com/in/lucas-ryan-33848524b)
