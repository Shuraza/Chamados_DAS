# Sistema de Chamados - API REST

API REST para gestão de chamados de suporte, desenvolvida na disciplina de Design e Arquitetura de Sistemas (Univille), aplicando arquitetura em camadas.

<details>
<summary><strong>Tecnologias</strong></summary>

- Java 21
- Spring Boot 4.1.1 (Web, Data JPA, Validation, Thymeleaf)
- springdoc-openapi 3.2.0 (OpenAPI + Swagger UI)
- H2 Database (em memória)
- Maven

</details>

<details>
<summary><strong>Arquitetura</strong></summary>

Controller → Service → Repository → Entity, com DTOs para entrada/saída e tratamento de erros centralizado no `GlobalExceptionHandler`.

| Pacote | Responsabilidade |
|---|---|
| `controller` | Recebe as requisições HTTP e devolve status/resposta |
| `dto` | Contrato de entrada e saída da API |
| `service` | Regras de negócio e transações |
| `repository` | Acesso a dados via Spring Data JPA |
| `entity` | Mapeamento das tabelas do banco |
| `exception` | Exceções de domínio e padronização das respostas de erro |
| `web` | Telas Thymeleaf (controllers MVC e formulários) |
| `config` | Configuração da documentação OpenAPI |

</details>

<details>
<summary><strong>Como executar</strong></summary>

```
.\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

| Endereço | O que é |
|---|---|
| `http://localhost:8080/` | Front-end (redireciona para `/web/chamados`) |
| `http://localhost:8080/swagger-ui.html` | Documentação da API (Swagger UI) |
| `http://localhost:8080/v3/api-docs` | Especificação OpenAPI em JSON |
| `http://localhost:8080/h2-console` | Console do banco H2 |

</details>

<details>
<summary><strong>Banco H2</strong></summary>

- Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:chamadosdb`
- Usuário: `sa`
- Senha: em branco

O banco é recriado a cada reinício da aplicação.

</details>

<details>
<summary><strong>Front-end (Thymeleaf)</strong></summary>

Cada entidade tem sua página, e elas são interligadas:

| Página | Conteúdo |
|---|---|
| `/web/chamados` | Lista com filtros (status, prioridade, cliente) e paginação |
| `/web/chamados/novo` | Abertura de chamado |
| `/web/chamados/{id}` | Detalhe com comentários, alteração de status, atribuição de técnico e exclusão |
| `/web/clientes` e `/web/clientes/{id}` | Cadastro, edição e chamados do cliente |
| `/web/categorias` e `/web/categorias/{id}` | Cadastro, edição e chamados da categoria |
| `/web/tecnicos` e `/web/tecnicos/{id}` | Cadastro, edição e chamados atribuídos ao técnico |

Na tabela de chamados, o cliente, a categoria e o técnico são links para as respectivas páginas. A página de cada cliente, categoria e técnico tem um atalho para abrir um chamado já preenchido.

</details>

<details>
<summary><strong>Documentação da API (OpenAPI)</strong></summary>

A documentação é gerada pelo springdoc a partir dos controllers REST, com `@Tag` e `@Operation`, e fica disponível no Swagger UI em `/swagger-ui.html`. As rotas `/web/**` do front-end não entram na documentação.

</details>

<details>
<summary><strong>Endpoints</strong></summary>

| Recurso | Rotas |
|---|---|
| Clientes | `POST/GET /clientes`, `GET/PUT/DELETE /clientes/{id}` |
| Categorias | `POST/GET /categorias`, `GET/PUT/DELETE /categorias/{id}` |
| Técnicos | `POST/GET /tecnicos`, `GET/PUT/DELETE /tecnicos/{id}` |
| Chamados | `POST/GET /chamados`, `GET/PUT/DELETE /chamados/{id}`, `PATCH /chamados/{id}/status`, `PATCH /chamados/{id}/atribuir` |
| Chamados do cliente | `GET /clientes/{id}/chamados` |
| Comentários | `POST/GET /chamados/{id}/comentarios` |

`GET /chamados/{id}` devolve o chamado com cliente, categoria, técnico e comentários.

</details>

<details>
<summary><strong>Exemplos de requisição</strong></summary>

Cliente:

```json
{ "nome": "João Silva", "email": "joao@gmail.com", "telefone": "47999998888" }
```

Categoria:

```json
{ "nome": "Hardware", "descricao": "Problemas de equipamento" }
```

Técnico (`perfil`: `ADMIN` ou `TECNICO`):

```json
{ "nome": "Carlos Souza", "email": "carlos@empresa.com", "perfil": "TECNICO" }
```

Chamado (`prioridade`: `BAIXA`, `MEDIA`, `ALTA` ou `CRITICA`; `tecnicoId` opcional):

```json
{ "titulo": "PC não liga", "descricao": "Computador não liga desde ontem", "prioridade": "ALTA", "clienteId": 1, "categoriaId": 1, "tecnicoId": 1 }
```

Alterar status (`ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE` ou `FECHADO`):

```json
{ "status": "FECHADO" }
```

Atribuir técnico:

```json
{ "tecnicoId": 1 }
```

Comentário:

```json
{ "texto": "Verificando a fonte do equipamento", "autor": "Carlos Souza" }
```

</details>

<details>
<summary><strong>Filtros e paginação</strong></summary>

`GET /chamados` e `GET /clientes/{id}/chamados` aceitam filtros opcionais e paginação:

```
GET /chamados?status=ABERTO&prioridade=ALTA&clienteId=1&page=0&size=10&sort=titulo,asc
```

- Filtros: `status`, `prioridade` e `clienteId` (podem ser combinados).
- Ordenação padrão: `dataAbertura` decrescente.
- Campos aceitos no `sort`: `id`, `titulo`, `dataAbertura`, `dataFechamento`, `prioridade`, `status`, `cliente.nome`, `categoria.nome`.

</details>

<details>
<summary><strong>Regras de negócio</strong></summary>

- E-mail de cliente e de técnico é único; nome de categoria é único.
- Chamado nasce com status `ABERTO` e `dataAbertura` preenchida automaticamente.
- Ao fechar o chamado, `dataFechamento` é preenchida.
- Chamado fechado não pode ser alterado, nem receber técnico ou comentário (retorna 409).
- Não é possível excluir cliente, categoria ou técnico com chamados vinculados, nem chamado com comentários.

</details>

<details>
<summary><strong>Códigos de resposta</strong></summary>

| Código | Situação |
|---|---|
| 200 | Consulta/atualização com sucesso |
| 201 | Registro criado |
| 204 | Registro excluído |
| 400 | Dados inválidos, filtro ou ordenação inválida |
| 404 | Registro não encontrado |
| 409 | Regra de negócio violada (duplicidade, vínculo, chamado fechado) |

</details>

<details>
<summary><strong>Testes</strong></summary>

```
.\mvnw test
```

- `repository`: testes com `@DataJpaTest` (persistência, restrições e filtros/paginação).
- `controller`: testes com `@WebMvcTest` (status HTTP, validação e respostas de erro).
- `service`: testes unitários com Mockito das regras de negócio do chamado.
- `web`: testes com `@WebMvcTest` das telas Thymeleaf (renderização, formulários e redirecionamentos).
- `OpenApiDocumentacaoTest`: confere se o `/v3/api-docs` documenta a API REST.

</details>

<details>
<summary><strong>Pendências (próximas etapas)</strong></summary>

- Profile PostgreSQL

</details>
