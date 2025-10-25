# 🚘 App Compra Veículo - Server

Este projeto é uma aplicação **backend** desenvolvida em **Spring Boot**, que fornece uma **API REST** para gerenciamento de **veículos (carros e motos)** e **usuários/login**.  
A aplicação utiliza **JPA** para persistência de dados e segue os princípios **RESTful**.

---

## 📋 Funcionalidades

### 🚗 Gestão de Carros
- **GET** `/veiculos/carro` — Lista todos os carros  
- **GET** `/veiculos/carro/{id}` — Busca carro por ID  
- **POST** `/veiculos/carro` — Cria um novo carro  
- **PUT** `/veiculos/carro/{id}` — Atualiza um carro existente  
- **DELETE** `/veiculos/carro/{id}` — Remove um carro  

### 🏍️ Gestão de Motos
- **GET** `/veiculos/moto` — Lista todas as motos  
- **GET** `/veiculos/moto/{id}` — Busca moto por ID  
- **POST** `/veiculos/moto` — Cria uma nova moto  
- **PUT** `/veiculos/moto/{id}` — Atualiza uma moto existente  
- **DELETE** `/veiculos/moto/{id}` — Remove uma moto  

### 👥 Gestão de Usuários (Login)
- **GET** `/login` — Lista todos os usuários  
- **GET** `/login/{id}` — Busca usuário por ID  
- **POST** `/login` — Cria um novo usuário  
- **PUT** `/login/{id}` — Atualiza um usuário existente  
- **DELETE** `/login/{id}` — Remove um usuário  

---

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   └── java/
│       └── br/edu/unifaj/cc/poo/appcompraveiculoserver/
│           ├── controllers/
│           │   ├── CarroController.java
│           │   ├── MotoController.java
│           │   └── LoginController.java
│           ├── entities/
│           │   ├── Carro.java
│           │   ├── Moto.java
│           │   └── Login.java
│           ├── repositories/
│           │   ├── CarroRepository.java
│           │   ├── MotoRepository.java
│           │   └── LoginRepository.java
│           └── AppcompraveiculoserverApplication.java
```

---

## 🛠️ Tecnologias Utilizadas

- **Java Spring Boot** — Framework principal  
- **Spring Data JPA** — Persistência de dados  
- **H2 Database** — Banco de dados em memória (ou outro configurado)  
- **Lombok** — Redução de código boilerplate  
- **Maven** — Gerenciamento de dependências  

---

## 📦 Modelos de Dados

### 🚗 Carro
```json
{
  "id": 1,
  "carroNome": "Fusca",
  "carroCor": "Azul",
  "carroAno": 1972,
  "carroValor": 15000.0
}
```

### 🏍️ Moto
```json
{
  "id": 1,
  "motoNome": "CG 160",
  "motoCor": "Vermelha",
  "motoAno": 2020,
  "motoValor": 12000.0
}
```

### 👤 Login (Usuário)
```json
{
  "id": 1,
  "nome": "João",
  "senha": "1234",
  "telefone": 999999999,
  "carteira": 5000
}
```

---

## 🚀 Como Executar

1. **Clone o repositório:**
   ```bash
   git clone [url-do-repositorio]
   ```

2. **Navegue até o diretório do projeto:**
   ```bash
   cd appcompraveiculoserver
   ```

3. **Execute a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```
   ou
   ```bash
   mvn spring-boot:run
   ```

4. **Acesse a aplicação:**
   ```
   http://localhost:8080
   ```

---

## 📡 Endpoints da API

### Carros
```
GET    /veiculos/carro
GET    /veiculos/carro/{id}
POST   /veiculos/carro
PUT    /veiculos/carro/{id}
DELETE /veiculos/carro/{id}
```

### Motos
```
GET    /veiculos/moto
GET    /veiculos/moto/{id}
POST   /veiculos/moto
PUT    /veiculos/moto/{id}
DELETE /veiculos/moto/{id}
```

### Login
```
GET    /login
GET    /login/{id}
POST   /login
PUT    /login/{id}
DELETE /login/{id}
```

---

## ⚙️ Configuração

Certifique-se de ter as seguintes dependências no `pom.xml`:

- `spring-boot-starter-web`  
- `spring-boot-starter-data-jpa`  
- `com.h2database:h2`  
- `lombok`  

A configuração do banco H2 é automática com o Spring Boot.  
Acesse o console do H2 em:  
```
http://localhost:8080/h2-console
```

---

## 🔧 Observações

- Os repositórios estendem `JpaRepository`, fornecendo operações CRUD básicas.  
- As entidades utilizam a anotação `@Entity` para mapeamento JPA.  
- O **Lombok** é usado para gerar automaticamente getters, setters e construtores.  
- A aplicação segue o padrão **RESTful** para design de APIs.  

---

## 📝 Próximos Passos Sugeridos

1. Implementar autenticação e autorização (Spring Security)  
2. Adicionar validação de dados (Bean Validation)  
3. Criar tratamento de erros personalizado (ControllerAdvice)  
4. Adicionar documentação com **Swagger/OpenAPI**  
5. Configurar banco de dados **PostgreSQL** para produção  
6. Criar **testes unitários e de integração**

---

📘 **Desenvolvido como parte do curso de Ciência da Computação - UNIFAJ**
