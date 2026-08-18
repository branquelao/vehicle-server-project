# Vehicle Server API

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791) ![License](https://img.shields.io/badge/license-MIT-blue)

A REST API for a used vehicle marketplace, built with Spring Boot, Spring Security (JWT), and JPA/Hibernate. Cars and motorcycles, search, favorites, messaging, reviews, and saved-search alerts, all in one backend.

## Features

### Authentication and Authorization

- JWT login through `POST /auth/login`, returns a Bearer token.
- Passwords are hashed with BCrypt. Nothing is stored in plain text.
- Two roles: `USER` for listing owners and `ADMIN` for moderation and account management.
- Users edit or delete only their own listings and their own account. Admins can manage anyone's.
- Write endpoints require authentication. Reading listings through `GET /veiculos/**` stays public.

### Vehicle Management

- Cars and motorcycles share a single `Veiculo` entity, split by a `tipo` field. No duplicated code between the two.
- Specs cover brand, model, manufacturing year, model year, mileage, fuel type, transmission, single-owner flag, trade-in flag, and condition. Type-specific fields add body style and doors for cars, displacement and category for motorcycles.
- Location lives on the vehicle, not the seller. A dealership can have listings spread across different cities.
- `GET /veiculos` combines filters: type, brand, partial model match, price range, year range, max mileage, color, city, state, status. All optional, all combinable.
- Pagination and sorting go through `Pageable` (`page`, `size`, `sort`). Sortable fields are whitelisted (`valor`, `anoModelo`, `anoFabricacao`, `km`, `anunciadoEm`, `atualizadoEm`, `marca`, `modelo`). Anything outside that list gets ignored instead of breaking the request.
- Without a status filter, public search returns only `ATIVO` listings.
- Listings move through a lifecycle via `status`: `ATIVO`, `PAUSADO`, `VENDIDO`, `EXPIRADO`. Updatable through `PUT /veiculos/{id}`.
- Multiple images per listing, with a main image and explicit ordering.
- A shared catalog of optionals, many-to-many, browsable publicly at `GET /opcionais`.
- A "recent listings" endpoint feeds homepage highlights.
- Every listing belongs to the seller (`Login`) who created it.

### Buyer and Seller Interaction

- **Favorites**: `POST`/`DELETE /veiculos/{id}/favoritos` to save or remove a listing, `GET /favoritos` to list your own. The action is idempotent, and favoriting your own listing is blocked.
- **Messaging**: a buyer opens a conversation on a specific vehicle through `POST /veiculos/{id}/mensagens`. Later messages reuse that same conversation via `POST /conversas/{id}/mensagens`. Only the two participants can read or reply.
- **Seller reviews**: reputation belongs to the seller (`Login`), not the vehicle. Reviewing requires an existing conversation with that seller. One review per reviewer-seller pair, updated in place rather than stacked. A public summary at `GET /logins/{id}/avaliacoes/resumo` shows average rating and total count.
- **Saved searches and alerts**: users store a set of filters. A scheduled job compares new active listings against every saved search and creates alerts, skipping the seller's own listings and avoiding duplicates.

### User Management

- Full CRUD for user accounts.
- Seller profile supports individuals and dealerships, with company name and tax ID (CNPJ) when it applies.
- Profile picture upload per user.
- Response DTOs never expose the password hash.
- Request DTOs accept only the fields a client should set. No direct binding to the entity.

### File Uploads

- Uploads are tracked in the database (`Upload`), storing the original name, the generated name, and the author. `POST /uploads` requires authentication.
- `GET /uploads` lists your own upload history.
- Files are served publicly at `/arquivos/{name}`, kept separate from the management API (`/uploads`) so private metadata never leaks alongside static content.
- File names are generated server-side, which rules out path traversal and collisions.

### Testing, Docker and CI

- Unit tests with Mockito for services and the saved-search matching job.
- Integration tests with MockMvc for controllers and `@DataJpaTest` for query specifications.
- Docker and Docker Compose to run the whole stack with one command.
- GitHub Actions builds and tests the project on every push.

### API Documentation

- Interactive Swagger UI to explore and test every endpoint from the browser, including Bearer authentication.
- Available at `/docs`.

### Architecture

- REST API on Spring Boot.
- Persistence through JPA/Hibernate.
- Flyway handles schema versioning (`ddl-auto=validate`).
- Stateless authentication with Spring Security and JWT.
- A service layer keeps business rules out of the controllers.
- Custom exceptions map to the right HTTP status through `@RestControllerAdvice`.
- CORS is configurable via `application.properties`.
- Database: PostgreSQL.

## Technologies

- Java / Spring Boot
- Spring Security + JWT (JJWT)
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- springdoc-openapi (Swagger UI)
- Lombok
- Maven
- JUnit 5, Mockito, AssertJ, MockMvc
- Docker, Docker Compose
- GitHub Actions

## Running the Application

### Prerequisites

- JDK 17+
- Maven (or use the included `mvnw`)
- PostgreSQL, database `veiculos_db`
- Docker and Docker Compose, optional but recommended

### Option 1: Docker

```bash
git clone https://github.com/branquelao/vehicle-server-project.git
cd vehicle-server-project

docker-compose up --build
```

Docker Compose brings up PostgreSQL and the API together, and Flyway applies the schema on first boot.

### Option 2: Local

```bash
git clone https://github.com/branquelao/vehicle-server-project.git
cd vehicle-server-project

./mvnw spring-boot:run
```

Configure the database credentials and the JWT secret through environment variables before running. If they're omitted, development defaults are used instead. Tables are created automatically by Flyway on first run.

The API is available at `http://localhost:8080`, and Swagger UI at `http://localhost:8080/docs`.

## Configuration (Environment Variables)

| Variable | Description |
|---|---|
| `DB_USERNAME` | PostgreSQL user. Falls back to a development default if omitted. |
| `DB_PASSWORD` | PostgreSQL password. Falls back to a development default if omitted. |
| `JWT_SECRET` | Secret key used to sign and validate JWT tokens. Falls back to a development default if omitted. |
| `JWT_EXPIRATION_MS` | Token expiration time, in milliseconds. |
| `ALERTAS_JOB_FIXED_DELAY_MS` | Optional. Interval, in milliseconds, between runs of the saved-search alert job. Defaults to `300000` (5 minutes). |

## Authentication

1. Get a token from `POST /auth/login`.
2. Send it on every request that needs it: `Authorization: Bearer <your_jwt_token>`.

`USER` covers regular buyer and seller accounts. `ADMIN` can manage any listing or account, on top of their own.

## API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/login` | Authenticates a user and returns a JWT token. | Public |
| GET | `/login` | Lists all registered users. | ADMIN |
| GET | `/login/{id}` | Gets a specific user's data. | Owner / ADMIN |
| POST | `/login` | Registers a new user account. | Public |
| PUT | `/login/{id}` | Updates a user's data: username, phone, password. | Owner / ADMIN |
| PUT | `/login/{id}/imagem` | Updates a user's profile picture. | Owner / ADMIN |
| DELETE | `/login/{id}` | Deletes a user account. | Owner / ADMIN |
| GET | `/veiculos` | Searches vehicles with filters, pagination and sorting. | Public |
| GET | `/veiculos/{id}` | Gets a specific vehicle listing. | Public |
| GET | `/veiculos/recentes` | Lists the 3 most recently published listings. | Public |
| POST | `/veiculos` | Creates a new vehicle listing, car or motorcycle. | Authenticated |
| PUT | `/veiculos/{id}` | Updates a vehicle listing. | Owner / ADMIN |
| DELETE | `/veiculos/{id}` | Deletes a vehicle listing. | Owner / ADMIN |
| GET | `/opcionais` | Lists the catalog of available optionals. | Public |
| POST | `/uploads` | Uploads an image for later use in a listing. | Authenticated |
| GET | `/uploads` | Lists the current user's uploaded images. | Authenticated |
| POST | `/veiculos/{veiculoId}/favoritos` | Adds a vehicle to favorites. | Authenticated |
| DELETE | `/veiculos/{veiculoId}/favoritos` | Removes a vehicle from favorites. | Authenticated |
| GET | `/favoritos` | Lists the current user's favorite vehicles. | Authenticated |
| POST | `/veiculos/{veiculoId}/mensagens` | Starts or continues a conversation about a listing. | Authenticated |
| POST | `/conversas/{conversaId}/mensagens` | Replies to an existing conversation. | Authenticated |
| GET | `/conversas` | Lists conversations the user takes part in. | Authenticated |
| GET | `/conversas/{conversaId}/mensagens` | Lists messages in a conversation. | Authenticated |
| POST | `/logins/{vendedorId}/avaliacoes` | Rates a seller. | Authenticated |
| GET | `/logins/{vendedorId}/avaliacoes` | Lists reviews received by a seller. | Public |
| GET | `/logins/{vendedorId}/avaliacoes/resumo` | Gets a seller's average rating and review count. | Public |
| POST | `/buscas-salvas` | Creates a saved search. | Authenticated |
| GET | `/buscas-salvas` | Lists the current user's saved searches. | Authenticated |
| DELETE | `/buscas-salvas/{id}` | Deletes a saved search. | Owner |
| GET | `/alertas` | Lists alerts generated for the current user. | Authenticated |
| PUT | `/alertas/{id}/visualizado` | Marks an alert as viewed. | Owner |

## Data Models and Examples

### Vehicle

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

### Vehicle Search

`GET /veiculos` accepts filters and pagination as query params, all optional.

```
GET /veiculos?marca=volkswagen&precoMax=30000&cidade=pedreira&page=0&size=10&sort=valor,asc
```

Paginated response:

```json
{
  "conteudo": [ /* list of VeiculoResponseDTO */ ],
  "paginaAtual": 0,
  "totalPaginas": 3,
  "totalElementos": 27,
  "tamanhoPagina": 10,
  "primeira": true,
  "ultima": false
}
```

### User (Login)

Request (`POST`/`PUT /login`):

```json
{
  "usuario": "joao123",
  "senha": "minhaSenha",
  "telefone": "19999887766"
}
```

Response, never includes the password:

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

### Login (Authentication)

Request (`POST /auth/login`):

```json
{
  "usuario": "joao123",
  "senha": "minhaSenha"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuario": "joao123",
  "role": "USER"
}
```

### Conversation

Response (`GET /conversas`):

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

### Review

Response (`GET /logins/{id}/avaliacoes`):

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

Summary (`GET /logins/{id}/avaliacoes/resumo`):

```json
{
  "media": 4.67,
  "total": 3
}
```

## Running Tests

```bash
./mvnw clean verify
```

Runs the full suite: unit tests for services and the matching job, plus integration tests for controllers and query specifications.

## Project Status

In development, being reworked from a college project into a portfolio backend.

### Done

- Authentication with JWT and Spring Security
- Migration from raw JDBC to JPA/Hibernate
- Schema versioning with Flyway
- Unified `Carro`/`Moto` into a single `Veiculo` entity
- Multiple images per listing
- Optionals catalog, with a public read endpoint
- Listing lifecycle (status), editable through PUT
- Seller profile, individual or dealership
- Favorites, buyer-seller messaging, and seller reviews
- Listing location
- Search with combined filters, pagination, and sorting
- File uploads tracked in the database, static serving kept separate from the management API
- Secrets (database password, JWT) externalized through environment variables
- Centralized error handling, including invalid query parameters like wrong types, unknown enum values, or disallowed sort fields
- Password hashing with BCrypt
- Path traversal protection on file uploads
- Configurable CORS
- Request and response DTOs, no raw entity or password ever exposed
- Swagger UI documentation, including Bearer auth and field descriptions conditional on vehicle type
- Automated unit and integration tests
- Docker and Docker Compose
- CI with GitHub Actions
- Saved searches with automatic alerts for matching new listings

### Planned

- Paid listing highlight through Mercado Pago

## Contributing

1. Fork the project.
2. Create your feature branch: `git checkout -b feature/NewFeature`.
3. Commit your changes: `git commit -m 'feat: adds NewFeature'`.
4. Push to the branch: `git push origin feature/NewFeature`.
5. Open a Pull Request.

## License

Distributed under the MIT license. See `LICENSE.md` for more information.