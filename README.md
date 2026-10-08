# DragonDeal Backend 🐉

Spring Boot REST API for the DragonDeal marketplace — secure P2P transactions with blockchain escrow.

## Overview

Backend service for the DragonDeal Android marketplace application. Provides REST API for user authentication, listing management, order processing, real-time chat, payment handling, and blockchain escrow integration.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Framework** | Spring Boot 3.3.5 |
| **Language** | Java 21 |
| **Database (prod)** | PostgreSQL |
| **Database (dev)** | H2 |
| **Migrations** | Liquibase |
| **Auth** | JWT (jjwt) |
| **Blockchain** | Web3j (Ethereum) |
| **Email** | Resend API |
| **Build** | Maven |
| **Deploy** | Docker / Railway |

## Project Structure

```
src/main/java/org/example/
├── config/              # Spring configurations
├── controller/          # REST endpoints
├── domain/              # JPA entities
│   └── enums/           # Enum types
├── dto/                 # Data Transfer Objects
│   ├── request/         # API request DTOs
│   ├── response/        # API response DTOs
│   └── blockchain/      # Blockchain-related DTOs
├── exception/           # Custom exceptions + handlers
├── mapper/              # Entity ↔ DTO mappers
├── repository/          # Spring Data JPA repositories
└── service/             # Business logic services
```

## Getting Started

### Prerequisites

- **JDK 21** (Temurin recommended)
- **Maven 3.8+**
- **PostgreSQL** (for production profile)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/dasha140/dragondeal-backend.git
cd dragondeal-backend

# Run with H2 (development)
mvn spring-boot:run

# Run with PostgreSQL (production)
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL="jdbc:postgresql://localhost:5432/marketgm" \
SPRING_DATASOURCE_USERNAME="postgres" \
SPRING_DATASOURCE_PASSWORD="secret" \
JWT_SECRET="$(openssl rand -hex 32)" \
mvn spring-boot:run
```

Server starts on `http://localhost:8081`

### Configuration

#### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | `dev` or `prod` | `dev` |
| `DATABASE_URL` | JDBC connection string | (H2 file) |
| `SPRING_DATASOURCE_USERNAME` | DB username | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | (empty) |
| `JWT_SECRET` | JWT signing key (32+ hex chars) | dev-key |
| `MAIL_USERNAME` | Resend API key | (required) |
| `MAIL_FROM` | Sender email | `noreply@dragondeal.online` |
| `APP_MAIL_DEV_MODE` | Skip actual email sending | `true` |
| `BLOCKCHAIN_RPC_URL` | Ethereum node RPC URL | (optional) |
| `BLOCKCHAIN_CONTRACT_ADDRESS` | Escrow contract address | (optional) |

#### Profiles

- **`dev`** — H2 database, JWT dev mode, mail dev mode
- **`prod`** — PostgreSQL, production JWT, real email via Resend

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run JAR
java -jar target/Marketgm-1.0-SNAPSHOT.jar
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest
```

## API Endpoints

### Authentication
- `POST /api/auth/register` — Register new user
- `POST /api/auth/login` — Login
- `POST /api/auth/verify-email` — Verify email with code
- `POST /api/auth/reset-password` — Request password reset

### Listings
- `GET /api/listings` — Browse all listings
- `GET /api/listings/{id}` — Get listing details
- `POST /api/listings` — Create listing (auth required)
- `PUT /api/listings/{id}` — Update listing (owner only)
- `DELETE /api/listings/{id}` — Delete listing (owner only)
- `POST /api/listings/{id}/upload` — Upload images

### Orders
- `GET /api/orders` — Get user's orders
- `GET /api/orders/{id}` — Get order details
- `POST /api/orders` — Create order from listing
- `POST /api/orders/{id}/accept` — Accept order (seller)
- `POST /api/orders/{id}/complete` — Complete order (buyer)
- `POST /api/orders/{id}/dispute` — Open dispute

### Chat
- `GET /api/chat/{orderId}` — Get chat messages
- `POST /api/chat/{orderId}` — Send message

### Payment
- `GET /api/payment/methods` — Get available payment methods
- `POST /api/payment/{orderId}` — Initiate payment

### User
- `GET /api/user/profile` — Get current user profile
- `PUT /api/user/profile` — Update profile
- `GET /api/user/{id}` — Get user public profile
- `GET /api/user/{id}/reviews` — Get user reviews

## Docker

```bash
# Build image
docker build -t dragondeal-backend .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host:5432/marketgm \
  dragondeal-backend
```

## Deploying to Railway

Railway automatically detects the `Dockerfile` and builds the image.

Required Railway variables:

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | `openssl rand -hex 32` |
| `MAIL_USERNAME` | Resend API key |
| `MAIL_FROM` | `noreply@dragondeal.online` |
| `APP_MAIL_DEV_MODE` | `false` |

Railway injects `DATABASE_URL` automatically from the PostgreSQL plugin.

## Blockchain Integration

The backend integrates with the `DragonDealEscrow` smart contract via Web3j:

- Creates escrow when order is accepted
- Releases funds when order is completed
- Handles disputes via contract methods

Contract source: `blockchain/contracts/DragonDealEscrow.sol`

## Architecture

### Layered Architecture

```
Controller → Service → Repository → Database
                ↓
           Blockchain (Web3j)
```

- **Controllers** — HTTP request handling, validation
- **Services** — Business logic, transaction management
- **Repositories** — Data access (Spring Data JPA)
- **Mappers** — Entity ↔ DTO conversion
- **DTOs** — API contract definitions

## CI/CD

GitHub Actions workflow runs on every push/PR:
- Maven build (`mvn clean package`)
- Unit tests (`mvn test`)
- Docker image build

## License

Private project — all rights reserved.

## Contact

- **GitHub**: [@dasha140](https://github.com/dasha140)
