# Vehicle Server API
Uma **API REST para um marketplace de veículos usados**, inspirada em plataformas como **OLX** e **Webmotors**, construída com **Spring Boot** e **JDBC puro**.

> Este repositório contém apenas o backend. Ele alimenta um site e um app Android (ambos em processo de reformulação) que consomem essa API.

---

## Funcionalidades

### Autenticação
- Hash de senha com BCrypt (nenhuma senha armazenada em texto puro)
- Endpoint de verificação de login
- Upload de imagem de perfil por usuário

### Gestão de Veículos
- CRUD completo para **carros** e **motos**
- Upload de imagem com nomes de arquivo gerados pelo servidor (evita path traversal e colisões)
- Endpoint de "anúncios recentes" para cada tipo de veículo (usado em destaques na página inicial)
- Cada anúncio é vinculado ao vendedor (`Login`) que o criou

### Gestão de Usuários
- CRUD completo para usuários
- DTOs de resposta que nunca expõem o hash da senha
- DTOs de requisição que só aceitam os campos que o cliente deveria poder definir (sem bind direto na entidade)

### Documentação da API
- Swagger UI interativo pra explorar e testar cada endpoint direto pelo navegador — sem precisar de nenhum cliente separado
- Disponível em `/docs`

### Arquitetura
- API REST em Spring Boot
- JDBC puro (`JdbcTemplate`) — sem ORM/Hibernate
- Camada de Service separando regra de negócio dos controllers
- Exceptions customizadas mapeadas pros status HTTP corretos
- CORS configurável via `application.properties`
- Banco H2 baseado em arquivo

---

## Como Executar

### Pré-requisitos
- JDK 17+
- Maven

### Rodando a Aplicação
1. Clone o repositório
2. Execute:
```bash
   ./mvnw spring-boot:run
```
3. Swagger UI abre em:
   `http://localhost:8080/docs`
4. Console do H2 (pra inspecionar o banco) em:
   `http://localhost:8080/h2-console`

---

## Modelos de Dados

### Carro
```json
{
  "id": 1,
  "carroNome": "Fusca",
  "carroCor": "Azul",
  "carroAno": 1972,
  "carroValor": 15000.0,
  "carroImagem": "a1b2c3.jpg",
  "loginId": 1
}
```

### Moto
```json
{
  "id": 1,
  "motoNome": "CG 160",
  "motoCor": "Vermelha",
  "motoAno": 2020,
  "motoValor": 12000.0,
  "motoImagem": "d4e5f6.jpg",
  "loginId": 1
}
```

### Login (Usuário)
Requisição (`POST`/`PUT /login`):
```json
{
  "usuario": "joao123",
  "senha": "minhaSenha",
  "telefone": "19999887766"
}
```
Resposta (nunca inclui a senha):
```json
{
  "id": 1,
  "usuario": "joao123",
  "telefone": "19999887766",
  "loginImagem": null,
  "loginCriadoEm": "18/07/2026 14:32:00",
  "loginAtualizadoEm": "18/07/2026 14:32:00"
}
```

---

## Tecnologias
- Java / Spring Boot
- Spring JDBC (`JdbcTemplate`)
- H2 Database
- Spring Security Crypto (BCrypt)
- springdoc-openapi (Swagger UI)
- Lombok
- Maven

---

## Status do Projeto
Em desenvolvimento — sendo refatorado de um projeto de faculdade pra um backend de portfólio.

### Concluído
- ✅ CRUD completo pra carros, motos e usuários
- ✅ Migração de JPA/Hibernate para JDBC puro
- ✅ Camada de Service separando regra de negócio dos controllers
- ✅ Hash de senha (BCrypt)
- ✅ Proteção contra path traversal em uploads de arquivo
- ✅ CORS configurável (sem mais `origins = "*"`)
- ✅ DTOs pra requisições e respostas (sem expor entidade/senha crua)
- ✅ Documentação com Swagger UI
- ✅ Banco de dados/uploads removidos do versionamento

### Planejado
- 🔲 Frontend web redesenhado e simplificado
- 🔲 App Android redesenhado
- 🔲 Autenticação baseada em JWT
- 🔲 Testes automatizados