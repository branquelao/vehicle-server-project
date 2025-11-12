# 🚘 App Compra Veículo - Server + Burnout Zone! (Frontend)

Este projeto é uma aplicação **full stack** composta por:

- 🖥️ **Backend (API REST em Spring Boot)** — Gerencia **veículos (carros e motos)** e **usuários/login**
- 🌐 **Frontend (Burnout Zone!)** — Interface web para exibição e anúncio de veículos

---

## 📦 Visão Geral

A aplicação **permite cadastrar, listar e atualizar veículos e usuários**, fornecendo endpoints REST consumidos por uma interface moderna feita em **HTML + Bootstrap**.  
O banco de dados é **H2** (em memória), ideal para testes e prototipagem rápida.

---

## 📋 Funcionalidades do Backend

### 🚗 Gestão de Carros
- **GET** `/veiculos/carro` — Lista todos os carros  
- **GET** `/veiculos/carro/{id}` — Busca carro por ID  
- **POST** `/veiculos/carro` — Cria um novo carro  
- **PUT** `/veiculos/carro/{id}` — Atualiza um carro existente  
- **DELETE** `/veiculos/carro/{id}` — Remove um carro  
- **GET** `/veiculos/carro/recentes` — Retorna os carros mais recentes (para o carrossel do site)

### 🏍️ Gestão de Motos
- **GET** `/veiculos/moto` — Lista todas as motos  
- **GET** `/veiculos/moto/{id}` — Busca moto por ID  
- **POST** `/veiculos/moto` — Cria uma nova moto  
- **PUT** `/veiculos/moto/{id}` — Atualiza uma moto existente  
- **DELETE** `/veiculos/moto/{id}` — Remove uma moto  
- **GET** `/veiculos/moto/recentes` — Retorna as motos mais recentes (para o carrossel do site)

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
└── resources/
    ├── static/           ← Arquivos do site (HTML, CSS, JS, imagens)
    ├── templates/
    └── application.properties
```

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java Spring Boot** — Framework principal  
- **Spring Data JPA** — Persistência de dados  
- **H2 Database** — Banco de dados em memória  
- **Lombok** — Redução de código boilerplate  
- **Maven** — Gerenciador de dependências  

### Frontend (Burnout Zone!)
- **HTML5 / CSS3 / JavaScript**  
- **Bootstrap 5** + **Bootstrap Icons**  
- **Google Fonts (Orbitron)**  
- **Fetch API** para consumir endpoints REST  
- **Carrossel dinâmico** exibindo veículos cadastrados  

---

## 🌐 Interface (Burnout Zone!)

O frontend “**Burnout Zone!**” é um site estático localizado em `src/main/resources/static/`, consumindo os endpoints REST do servidor.  
Exemplo da página principal (`menu.html` ou `index.html`):

```html
<h2 class="text-center mt-5 mb-3">Carros em destaque</h2>
<div id="carouselCarros" class="carousel slide" data-bs-ride="carousel">
  <!-- slides carregados dinamicamente -->
</div>

<script>
document.addEventListener("DOMContentLoaded", () => {
  carregarDestaques("carro");
  carregarDestaques("moto");
});

function carregarDestaques(tipo) {
  const endpoint = tipo === "carro"
    ? "http://localhost:8080/veiculos/carro/recentes"
    : "http://localhost:8080/veiculos/moto/recentes";

  fetch(endpoint)
    .then(res => res.json())
    .then(lista => {
      // popula o carrossel dinamicamente
    });
}
</script>
```

O carrossel exibe os **veículos mais recentes** cadastrados via backend, mostrando nome, ano e valor formatado.

---

## 📦 Modelos de Dados

### 🚗 Carro
```json
{
  "id": 1,
  "carroNome": "Fusca",
  "carroCor": "Azul",
  "carroAno": 1972,
  "carroValor": 15000.0,
  "carroImagem": "fusca.jpg"
}
```

### 🏍️ Moto
```json
{
  "id": 1,
  "motoNome": "CG 160",
  "motoCor": "Vermelha",
  "motoAno": 2020,
  "motoValor": 12000.0,
  "motoImagem": "cg160.jpg"
}
```

### 👤 Login (Usuário)
```json
{
  "id": 1,
  "nome": "João",
  "senha": "1234",
  "telefone": "199988776655",
  "carteira": 5000.0
}
```

---

## 🚀 Como Executar

1. **Clone o repositório:**
   ```bash
   git clone [url-do-repositorio]
   ```

2. **Acesse a pasta do projeto:**
   ```bash
   cd appcompraveiculoserver
   ```

3. **Execute o servidor:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acesse no navegador:**
   ```
   http://localhost:8080/html/start.html
   ```
---

## ⚙️ Banco de Dados H2

Acesse o console do banco em:
```
http://localhost:8080/h2-console
```

Credenciais padrão:
```
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password:
```

---

📘 **Desenvolvido como parte do curso de Ciência da Computação - UNIFAJ**  
🚗 **Backend:** Java Spring Boot  
🏍️ **Frontend:** Burnout Zone! (HTML + Bootstrap)
