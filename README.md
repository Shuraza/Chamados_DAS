# Sistema de Chamados - API REST

API REST para gestão de chamados de suporte, desenvolvida na disciplina de Design e Arquitetura de Sistemas (Univille), aplicando arquitetura em camadas.

<details>
<summary><strong>Tecnologias</strong></summary>

- Java 21
- Spring Boot 4.1.1 (Web, Data JPA, Validation)
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

</details>

<details>
<summary><strong>Como executar</strong></summary>

```
.\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

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
<summary><strong>Endpoints</strong></summary>

| Recurso | Rotas |
|---|---|
| Clientes | `POST/GET /clientes`, `GET/PUT/DELETE /clientes/{id}` |
| Categorias | `POST/GET /categorias`, `GET/PUT/DELETE /categorias/{id}` |
| Técnicos | `POST/GET /tecnicos`, `GET/PUT/DELETE /tecnicos/{id}` |
| Chamados | `POST/GET /chamados`, `GET/PUT/DELETE /chamados/{id}`, `PATCH /chamados/{id}/status` |
| Comentários | `POST/GET /chamados/{id}/comentarios` |

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

Comentário:

```json
{ "texto": "Verificando a fonte do equipamento", "autor": "Carlos Souza" }
```

</details>

<details>
<summary><strong>Regras de negócio</strong></summary>

- E-mail de cliente e de técnico é único; nome de categoria é único.
- Chamado nasce com status `ABERTO` e `dataAbertura` preenchida automaticamente.
- Ao fechar o chamado, `dataFechamento` é preenchida.
- Chamado fechado não pode ser alterado.
- Não é possível excluir cliente, categoria ou técnico com chamados vinculados, nem chamado com comentários.

</details>

<details>
<summary><strong>Códigos de resposta</strong></summary>

| Código | Situação |
|---|---|
| 200 | Consulta/atualização com sucesso |
| 201 | Registro criado |
| 204 | Registro excluído |
| 400 | Dados inválidos |
| 404 | Registro não encontrado |
| 409 | Regra de negócio violada (duplicidade, vínculo, chamado fechado) |

</details>

<details>
<summary><strong>Pendências (próximas etapas)</strong></summary>

- `PATCH /chamados/{id}/atribuir` para atribuição de técnico
- Filtros e paginação em `/chamados`
- Testes automatizados (`@DataJpaTest` e `@WebMvcTest`)
- Documentação OpenAPI e profile PostgreSQL

</details>
