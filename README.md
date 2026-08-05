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

### Gestão de Veículos
- Entidade única `Veiculo` (carros e motos), diferenciada pelo campo `tipo`, eliminando duplicação de código
- Ficha técnica ampliada: marca, modelo, ano de fabricação/modelo, km, combustível, câmbio, único dono, aceita troca, estado de conservação, e campos específicos por tipo (carroceria/portas para carro, cilindrada/categoria para moto)
- Localização do anúncio (cidade/estado) — a localização pertence ao veículo, não ao vendedor, já que lojas podem ter pátios em cidades diferentes
- Busca com filtros combinados via `GET /veiculos`: tipo, marca, modelo (parcial), faixa de preço, faixa de ano, km máximo, cor, cidade, estado e status — todos opcionais e combináveis
- Paginação e ordenação via `Pageable` (`page`, `size`, `sort`), com whitelist de campos ordenáveis (`valor`, `anoModelo`, `anoFabricacao`, `km`, `anunciadoEm`, `atualizadoEm`, `marca`, `modelo`) — campos fora da whitelist são ignorados silenciosamente, sem quebrar a requisição
- Por padrão (sem filtro de `status`), a busca pública retorna apenas anúncios `ATIVO`
- Ciclo de vida do anúncio via `status`: `ATIVO`, `PAUSADO`, `VENDIDO`, `EXPIRADO`, atualizável via `PUT /veiculos/{id}`
- Múltiplas imagens por anúncio, com imagem principal e ordenação
- Catálogo de opcionais/equipamentos (relação N:N), consultável publicamente via `GET /opcionais`
- Endpoint de "anúncios recentes" para destaque na página inicial
- Cada anúncio é vinculado ao vendedor (`Login`) que o criou

### Interação Comprador-Vendedor
- **Favoritos**: usuário favorita/desfavorita anúncios (`POST`/`DELETE /veiculos/{id}/favoritos`) e lista os próprios (`GET /favoritos`); operação idempotente, e não é possível favoritar o próprio anúncio
- **Mensagens**: comprador inicia uma conversa sobre um veículo específico (`POST /veiculos/{id}/mensagens`); mensagens seguintes reaproveitam a mesma conversa (`POST /conversas/{id}/mensagens`); só os participantes (comprador e vendedor) podem ler ou responder
- **Avaliação de vendedor**: reputação é do vendedor (`Login`), não do veículo; só é possível avaliar quem já teve uma conversa iniciada; uma avaliação por par avaliador/avaliado, atualizável; resumo público com média e total (`GET /logins/{id}/avaliacoes/resumo`)

### Gestão de Usuários
- CRUD completo para usuários
- Perfil de vendedor: pessoa física ou loja/revenda (com razão social e CNPJ quando aplicável)
- Upload de imagem de perfil por usuário
- DTOs de resposta que nunca expõem o hash da senha
- DTOs de requisição que só aceitam os campos que o cliente deveria poder definir (sem bind direto na entidade)

### Upload de Arquivos
- Upload rastreado no banco (`Upload`), com nome original, nome gerado pelo servidor e autor — `POST /uploads` (autenticado)
- Listagem dos próprios uploads via `GET /uploads` (autenticado)
- Arquivos servidos publicamente via `/arquivos/{nome}`, separado da API de gerenciamento (`/uploads`) para evitar que metadados privados fiquem expostos junto do conteúdo estático
- Nomes de arquivo gerados pelo servidor (evita path traversal e colisões)

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
2. Configure as credenciais do banco e o segredo JWT via variáveis de ambiente (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) — se omitidas, valores padrão de desenvolvimento são usados
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
   "cidade": "Pedreira",
   "estado": "SP",
   "anunciadoEm": "18/07/2026 14:32:00",
   "atualizadoEm": "18/07/2026 14:32:00",
   "loginId": 1,
   "imagens": ["a1b2c3.jpg"],
   "opcionais": ["Ar condicionado", "Freio ABS"]
}
```

### Busca de Veículos
`GET /veiculos` aceita filtros e paginação via query params, todos opcionais:

```
GET /veiculos?marca=volkswagen&precoMax=30000&cidade=pedreira&page=0&size=10&sort=valor,asc
```

Resposta paginada:
```json
{
   "conteudo": [ /* lista de VeiculoResponseDTO */ ],
   "paginaAtual": 0,
   "totalPaginas": 3,
   "totalElementos": 27,
   "tamanhoPagina": 10,
   "primeira": true,
   "ultima": false
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

### Conversa
Resposta (`GET /conversas`):
```json
{
   "id": 1,
   "veiculoId": 1,
   "veiculoTitulo": "Volkswagen Fusca",
   "compradorId": 2,
   "compradorUsuario": "maria456",
   "vendedorId": 1,
   "vendedorUsuario": "joao123",
   "criadaEm": "02/08/2026 16:30:43",
   "atualizadaEm": "02/08/2026 16:31:07"
}
```

### Avaliação
Resposta (`GET /logins/{id}/avaliacoes`):
```json
{
   "id": 1,
   "avaliadorId": 2,
   "avaliadorUsuario": "maria456",
   "nota": 5,
   "comentario": "Vendedor muito atencioso",
   "criadaEm": "02/08/2026 16:40:00"
}
```
Resumo (`GET /logins/{id}/avaliacoes/resumo`):
```json
{
   "media": 4.67,
   "total": 3
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
- ✅ Catálogo de opcionais (N:N), com endpoint público de consulta
- ✅ Ciclo de vida do anúncio (status), editável via PUT
- ✅ Perfil de vendedor (pessoa física / loja)
- ✅ Favoritos, mensagens comprador-vendedor e avaliação de vendedor
- ✅ Localização do anúncio
- ✅ Busca com filtros combinados, paginação e ordenação (`GET /veiculos`)
- ✅ Upload de arquivos rastreado no banco, com serving estático separado da API de gerenciamento
- ✅ Segredos (senha do banco, JWT) externalizados via variáveis de ambiente
- ✅ Tratamento de erro centralizado (`@RestControllerAdvice`), incluindo parâmetros de query inválidos (tipo errado, enum inexistente, campo de ordenação não permitido)
- ✅ Hash de senha (BCrypt)
- ✅ Proteção contra path traversal em uploads de arquivo
- ✅ CORS configurável
- ✅ DTOs pra requisições e respostas (sem expor entidade/senha crua)
- ✅ Documentação com Swagger UI (incluindo autenticação Bearer e descrições de campos condicionais por tipo de veículo)

### Planejado
- 🔲 Busca salva / alerta de novo anúncio compatível
- 🔲 Destaque pago de anúncio (Mercado Pago)
- 🔲 Testes automatizados, Docker e CI/CD
- 🔲 Frontend web redesenhado e simplificado
- 🔲 App Android redesenhado