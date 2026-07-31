# Vehicle Server API

Uma **API REST para um marketplace de veículos usados**, inspirada em plataformas como **OLX** e **Webmotors**, construída com **Spring Boot**, **Spring Security (JWT)** e **JPA/Hibernate**.

> Este repositório contém apenas o backend. Ele alimenta um site e um app Android (ambos em processo de reformulação) que consomem essa API.

---

## Funcionalidades

### Autenticação e Autorização
- Login com **JWT** (`POST /auth/login`) — retorna token Bearer
- Hash de senha com BCrypt (nenhuma senha armazenada em texto puro)
- Perfis de acesso: `USER` (dono de anúncio) e `ADMIN` (moderação/gestão)
- Regra de autorização: usuário só edita/exclui os próprios anúncios e a própria conta, exceto `ADMIN`
- Endpoints de escrita (POST/PUT/DELETE) protegidos; leitura de anúncios (`GET /veiculos/**`) é pública
- Upload de imagem de perfil por usuário

### Gestão de Veículos
- Entidade única `Veiculo` (carros e motos), diferenciada pelo campo `tipo`, eliminando duplicação de código
- Ficha técnica ampliada: marca, modelo, ano de fabricação/modelo, km, combustível, câmbio, único dono, aceita troca, estado de conservação, e campos específicos por tipo (carroceria/portas para carro, cilindrada/categoria para moto)
- Ciclo de vida do anúncio via `status`: `ATIVO`, `PAUSADO`, `VENDIDO`, `EXPIRADO`
- Múltiplas imagens por anúncio, com imagem principal e ordenação
- Catálogo de opcionais/equipamentos (relação N:N)
- Upload de imagem com nomes de arquivo gerados pelo servidor (evita path traversal e colisões)
- Endpoint de "anúncios recentes" para destaque na página inicial
- Cada anúncio é vinculado ao vendedor (`Login`) que o criou

### Gestão de Usuários
- CRUD completo para usuários
- Perfil de vendedor: pessoa física ou loja/revenda (com razão social e CNPJ quando aplicável)
- DTOs de resposta que nunca expõem o hash da senha
- DTOs de requisição que só aceitam os campos que o cliente deveria poder definir (sem bind direto na entidade)

### Documentação da API
- Swagger UI interativo pra explorar e testar cada endpoint direto pelo navegador, incluindo autenticação Bearer
- Disponível em `/docs`

### Arquitetura
- API REST em Spring Boot
- Persistência com **JPA/Hibernate**
- **Flyway** para versionamento de schema (`ddl-auto=validate`)
- Autenticação stateless com **Spring Security + JWT**
- Camada de Service separando regra de negócio dos controllers
- Exceptions customizadas mapeadas pros status HTTP corretos via `@RestControllerAdvice`
- CORS configurável via `application.properties`
- Banco de dados **PostgreSQL**

---

## Como Executar

### Pré-requisitos
- JDK 17+
- Maven
- PostgreSQL (banco `veiculos_db`)

### Rodando a Aplicação
1. Clone o repositório
2. Configure as credenciais do banco em `application.properties` (ou via variáveis de ambiente)
3. Execute:
```bash
   ./mvnw spring-boot:run
```
4. As tabelas são criadas automaticamente pelo Flyway na primeira execução
5. Swagger UI abre em:
   `http://localhost:8080/docs`

---

## Modelos de Dados

### Veiculo
```json
{
  "id": 1,
  "tipo": "CARRO",
  "marca": "Volkswagen",
  "modelo": "Fusca",
  "anoFabricacao": 1972,
  "anoModelo": 1972,
  "km": 85000,
  "cor": "Azul",
  "combustivel": "GASOLINA",
  "cambio": "MANUAL",
  "unicoDono": false,
  "aceitaTroca": true,
  "estadoConservacao": "USADO",
  "valor": 15000.0,
  "carroceria": "HATCH",
  "portas": 2,
  "status": "ATIVO",
  "anunciadoEm": "18/07/2026 14:32:00",
  "atualizadoEm": "18/07/2026 14:32:00",
  "loginId": 1,
  "imagens": ["a1b2c3.jpg"],
  "opcionais": ["Ar condicionado", "Freio ABS"]
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
  "role": "USER",
  "loginCriadoEm": "18/07/2026 14:32:00",
  "loginAtualizadoEm": "18/07/2026 14:32:00"
}
```

### Autenticação
Requisição (`POST /auth/login`):
```json
{
  "usuario": "joao123",
  "senha": "minhaSenha"
}
```
Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuario": "joao123",
  "role": "USER"
}
```

---

## Tecnologias
- Java / Spring Boot
- Spring Security + JWT (JJWT)
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- springdoc-openapi (Swagger UI)
- Lombok
- Maven

---

## Status do Projeto
Em desenvolvimento — sendo refatorado de um projeto de faculdade pra um backend de portfólio.

### Concluído
- ✅ Autenticação JWT com Spring Security
- ✅ Migração de JDBC puro para JPA/Hibernate
- ✅ Versionamento de schema com Flyway
- ✅ Unificação de `Carro`/`Moto` em entidade `Veiculo` única
- ✅ Múltiplas imagens por anúncio
- ✅ Catálogo de opcionais (N:N)
- ✅ Ciclo de vida do anúncio (status)
- ✅ Perfil de vendedor (pessoa física / loja)
- ✅ Tratamento de erro centralizado (`@RestControllerAdvice`)
- ✅ Hash de senha (BCrypt)
- ✅ Proteção contra path traversal em uploads de arquivo
- ✅ CORS configurável
- ✅ DTOs pra requisições e respostas (sem expor entidade/senha crua)
- ✅ Documentação com Swagger UI (incluindo autenticação Bearer)

### Planejado
- 🔲 Favoritos, mensagens comprador-vendedor e avaliação de vendedor
- 🔲 Busca e filtros avançados com paginação
- 🔲 Destaque pago de anúncio (Mercado Pago)
- 🔲 Testes automatizados, Docker e CI/CD
- 🔲 Frontend web redesenhado e simplificado
- 🔲 App Android redesenhado