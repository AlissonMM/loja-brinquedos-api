# morkstore-api

# Mork Store — API

Mork Store é uma API desenvolvida com **Spring Boot** para gerenciar uma loja online de colecionáveis e itens de RPG (miniaturas, dados, livros de sistema, cartas, board games, action figures, funkos e mais). O sistema permite o cadastro, busca, atualização e remoção de produtos, marcas e categorias, além de carrinho de compras e pedidos.

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA** (Hibernate)
- **Spring Web** (REST APIs)
- **MySQL** (Banco de dados em produção)
- **Apache Kafka** (eventos de domínio para auditoria/analytics)
- **Swagger OpenAPI** (Documentação da API)

## 📌 Funcionalidades

- CRUD de Produtos (com categoria livre — inclusive itens de RPG — e atributos extras em JSON)
- Filtragem por marca, categoria e nome
- Carrinho de compras e checkout com controle de estoque
- Histórico de pedidos por usuário
- Documentação interativa com Swagger

## 📦 Instalação e Configuração

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/morkstore-api.git
   ```
2. Acesse o diretório do projeto:
   ```bash
   cd morkstore-api
   ```
3. Configure o **application.properties** (para MySQL):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/morkstore
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
   ```
4. Execute o projeto:
   ```bash
   mvn spring-boot:run
   ```

## 🛠️ Endpoints Principais

| Método  | Endpoint                        | Descrição                               |
|---------|--------------------------------|--------------------------------|
| GET     | `/products/findAll`                | Lista todos os produtos     |
| GET     | `/products/findByName/{name}`      | Busca produto por nome      |
| GET     | `/products/findAllByBrand/{brand}` | Filtra produtos por marca   |
| GET     | `/products/findAllByCategory/{category}` | Filtra produtos por categoria |
| POST    | `/products/insert`                 | Adiciona um novo produto    |
| PUT     | `/products/update/{id}`            | Atualiza um produto         |
| PATCH   | `/products/updateVisibility/{id}`  | Altera a visibilidade do produto no catálogo |
| DELETE  | `/products/deleteById/{id}`        | Remove um produto por ID    |
| GET     | `/cart`                         | Consulta o carrinho do usuário logado |
| POST    | `/cart/items`                   | Adiciona um item ao carrinho |
| PUT     | `/cart/items/{itemId}`          | Atualiza a quantidade de um item |
| DELETE  | `/cart/items/{itemId}`          | Remove um item do carrinho |
| DELETE  | `/cart`                         | Esvazia o carrinho |
| POST    | `/orders/checkout`              | Fecha o pedido a partir do carrinho |
| POST    | `/orders/{id}/confirm-payment`  | Confirma pagamento simulado do pedido |
| POST    | `/orders/{id}/cancel`           | Cancela o pedido |
| GET     | `/orders/mine`                  | Lista os pedidos do usuário logado |
| GET     | `/orders/{id}`                  | Detalha um pedido |
| GET     | `/orders`                       | Lista todos os pedidos (admin) |

A documentação completa está disponível em:
- [Swagger UI](http://localhost:8080/swagger-ui.html)

## 🏠 Estrutura do Projeto
```
 morkstore-api/
 ├── src/main/java/edu/meialua/morkstore/
 │   ├── model                      # Classes de Negócio
 │   ├── adapters/in/repositories/  # Repositórios JPA
 │   ├── adapters/out/controller/   # Controllers (REST APIs)
 ├── src/main/resources/
 │   ├── application.properties     # Configuração do Spring Boot
```

## 💌 Devs
- **Alisson Mayer Medeji**
- **Bruno Araujo de Souza**
- **Gabriel Tavares Barsani**
- **Henrique Barreto**
- **Henrique Porto**
- **Leonardo Costa Lima**
- **Lucas Oliveira Silva**
