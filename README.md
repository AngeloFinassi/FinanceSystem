# 💰 FinanceSystem API

A robust and secure **personal finance management REST API** built with Spring Boot. FinanceSystem allows users to track income and expenses, manage bank accounts, set budgets per category, and monitor financial goals — all protected by JWT authentication.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Security](#security)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Migrations](#database-migrations)
- [API Reference](#api-reference)
- [Testing with cURL](#testing-with-curl)
- [Project Roadmap](#project-roadmap)

---

## Overview

FinanceSystem is a backend API designed to help users take control of their personal finances. It provides a complete suite of endpoints for managing accounts, transactions, budgets, and savings goals, with role-based access control separating regular users from administrators.

**Key design decisions:**
- Stateless authentication via JWT — no sessions stored on the server
- Account balances are automatically updated when transactions are created, edited, or deleted
- Budget spending is calculated in real time based on actual transactions
- Goal progress updates automatically with each deposit
- Default categories are seeded at startup and protected from modification or deletion

---

## Features

| Module | Capabilities |
|---|---|
| **Auth** | Register, login, view/update profile, change password |
| **Users (Admin)** | List all users, delete user, promote to admin |
| **Categories** | CRUD for personal categories; default system categories (read-only) |
| **Accounts** | CRUD with automatic balance tracking (CHECKING, SAVINGS, INVESTMENT, CASH) |
| **Transactions** | CRUD with filters and pagination; supports INCOME, EXPENSE, and TRANSFER types; auto-updates account balance on create/update/delete |
| **Budgets** | Monthly spending limits per category with real-time spent amount and percentage calculation |
| **Goals** | Savings goals with manual deposits, progress tracking, and automatic ACHIEVED status |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7 + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | MySQL 8.0 (Docker) |
| Migrations | Flyway 10 |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Container | Docker + Docker Compose |

---

## Architecture

The project follows a **domain-driven package structure**, where each business domain owns its entity, repository, service, controller, and DTOs:

```
finance.system.project/
├── config/
│   ├── SecurityConfig.java          # Spring Security + JWT filter chain
│   └── SwaggerConfig.java           # OpenAPI configuration
├── domain/
│   ├── user/                        # Auth, profile, admin operations
│   ├── account/                     # Bank account management
│   ├── category/                    # Transaction categories
│   ├── transaction/                 # Financial transactions
│   ├── budget/                      # Monthly spending limits
│   └── goal/                        # Savings goals
├── security/
│   ├── JwtService.java              # Token generation and validation
│   ├── JwtAuthenticationFilter.java # Request filter for JWT
│   └── UserDetailsServiceImpl.java  # Spring Security user loader
├── exception/
│   ├── GlobalExceptionHandler.java  # Centralized error handling
│   ├── BusinessException.java       # 409 Conflict errors
│   └── ResourceNotFoundException.java # 404 Not Found errors
└── report/                          # (Planned) Analytics and reports
```

### Request flow

```
Client → JwtAuthenticationFilter → Controller → Service → Repository → MySQL
```

Every request (except `/api/auth/register` and `/api/auth/login`) requires a valid JWT in the `Authorization: Bearer <token>` header.

---

## Security

### JWT Authentication

- Tokens are signed with HS256 using a configurable secret key
- Default expiration: **24 hours** (`86400000` ms)
- The `role` claim is embedded in the token (`ROLE_USER` or `ROLE_ADMIN`)
- No refresh token mechanism — users must re-authenticate after expiry

### Role-based Access Control

Authorization is enforced via `@PreAuthorize` annotations powered by `@EnableMethodSecurity`:

| Role | Access |
|---|---|
| `USER` | All personal finance endpoints |
| `ADMIN` | All USER endpoints + list all users, delete user, promote to admin |

### Data isolation

Every service method resolves the authenticated user from the JWT subject (`email`) and scopes all queries to that user. A user cannot access, modify, or delete another user's accounts, transactions, budgets, or goals.

### Password storage

Passwords are hashed using **BCrypt** via Spring Security's `BCryptPasswordEncoder`. Plain-text passwords are never stored or logged.

### Default admin account

A default admin is created via Flyway migration `V2__create_admin_user.sql`:

```
Email:    admin@financesystem.com
Password: Admin@2026
```

> ⚠️ Change this password immediately in any non-development environment.

---

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.8+
- Docker + Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/your-username/FinanceSystem.git
cd FinanceSystem
```

### 2. Start the database

```bash
cd docker
docker compose up -d
```

This starts a MySQL 8.0 container on port `3306` with:
- Database: `financedb`
- User: `admin` / Password: `123`

### 3. Run the application

```bash
./mvnw spring-boot:run
```

Flyway automatically runs all migrations on startup. The API will be available at `http://localhost:8080`.

### 4. Verify startup

You should see in the logs:

```
Successfully applied 3 migrations to schema `financedb`, now at version v3
Started ProjectApplication in X seconds
```

---

## Environment Variables

All configuration lives in `src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://...` | MySQL connection URL |
| `spring.datasource.username` | `admin` | Database user |
| `spring.datasource.password` | `123` | Database password |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate schema mode (never use `update` in production) |
| `jwt.secret` | `404E63...` | HS256 signing key (change in production) |
| `jwt.expiration` | `86400000` | Token expiry in milliseconds (24h) |

---

## Database Migrations

Flyway manages all schema changes in `src/main/resources/db/migration/`:

| Version | File | Description |
|---|---|---|
| V1 | `V1__init_schema.sql` | Creates all tables (users, categories, accounts, transactions, budgets, goals) |
| V2 | `V2__create_admin_user.sql` | Seeds the default admin user |
| V3 | `V3__seed_categories.sql` | Seeds 7 default categories (Salário, Alimentação, Transporte, etc.) |

To reset the database during development:

```sql
USE financedb;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS transactions, budgets, goals, accounts, categories, users, flyway_schema_history;
SET FOREIGN_KEY_CHECKS = 1;
```

Then restart the application and Flyway will recreate everything from scratch.

---

## API Reference

### Base URL

```
http://localhost:8080
```

### Authentication

All endpoints except register and login require:

```
Authorization: Bearer <your-jwt-token>
```

---

### Auth endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Create a new user account |
| POST | `/api/auth/login` | ❌ | Authenticate and receive JWT |
| GET | `/api/auth/profile` | ✅ | Get current user profile |
| PUT | `/api/auth/update-profile` | ✅ | Update name or email |
| PUT | `/api/auth/change-password` | ✅ | Change password |
| GET | `/api/auth/all-users` | 🔐 ADMIN | List all users |
| PATCH | `/api/auth/users/{id}/promote` | 🔐 ADMIN | Promote user to admin |
| DELETE | `/api/auth/users/{id}` | 🔐 ADMIN | Delete a user |

### Categories endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/categories` | List all categories (personal + default) |
| POST | `/api/categories` | Create a personal category |
| GET | `/api/categories/{id}` | Get category by ID |
| PUT | `/api/categories/{id}` | Update category (non-default only) |
| DELETE | `/api/categories/{id}` | Delete category (non-default only) |

### Accounts endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/accounts` | List all user accounts |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts/{id}` | Get account by ID |
| PUT | `/api/accounts/{id}` | Update account name/type |
| DELETE | `/api/accounts/{id}` | Delete account |

### Transactions endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/transactions` | List transactions (paginated, filterable) |
| POST | `/api/transactions` | Create transaction (auto-updates balance) |
| GET | `/api/transactions/{id}` | Get transaction by ID |
| PUT | `/api/transactions/{id}` | Update transaction (reverts old balance, applies new) |
| DELETE | `/api/transactions/{id}` | Delete transaction (reverts balance) |

**Query parameters for GET `/api/transactions`:**

| Parameter | Type | Example |
|---|---|---|
| `type` | `INCOME` \| `EXPENSE` \| `TRANSFER` | `?type=EXPENSE` |
| `accountId` | UUID | `?accountId=abc-123` |
| `categoryId` | UUID | `?categoryId=abc-123` |
| `startDate` | `yyyy-MM-dd` | `?startDate=2026-06-01` |
| `endDate` | `yyyy-MM-dd` | `?endDate=2026-06-30` |
| `isPaid` | boolean | `?isPaid=true` |
| `page` | int | `?page=0` |
| `size` | int | `?size=20` |

### Budgets endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/budgets?month=6&year=2026` | List budgets for a month (with real-time spent) |
| POST | `/api/budgets` | Create a monthly budget for a category |
| GET | `/api/budgets/{id}` | Get budget by ID |
| PUT | `/api/budgets/{id}` | Update budget limit |
| DELETE | `/api/budgets/{id}` | Delete budget |

### Goals endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/goals` | List all savings goals |
| POST | `/api/goals` | Create a goal |
| GET | `/api/goals/{id}` | Get goal by ID |
| PUT | `/api/goals/{id}` | Update goal details |
| DELETE | `/api/goals/{id}` | Delete goal |
| POST | `/api/goals/{id}/deposit` | Add funds to goal |

---

## Testing with cURL

### Setup

```bash
BASE=http://localhost:8080

# Register
curl -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"secret123"}'

# Login and save token
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"secret123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

### Create an account

```bash
curl -X POST $BASE/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Main Account","accountType":"CHECKING","initialBalance":2000.00}'

# Save the returned ID
ACCOUNT_ID=<id from response>
```

### List default categories and save IDs

```bash
curl $BASE/api/categories -H "Authorization: Bearer $TOKEN"

CAT_SALARY=<salary category id>
CAT_FOOD=<food category id>
```

### Create transactions

```bash
# Income
curl -X POST $BASE/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"June Salary\",\"amount\":5000,\"type\":\"INCOME\",\"transactionDate\":\"2026-06-01\",\"accountId\":\"$ACCOUNT_ID\",\"categoryId\":\"$CAT_SALARY\",\"isPaid\":true}"

# Expense
curl -X POST $BASE/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"Grocery\",\"amount\":350,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-06-05\",\"accountId\":\"$ACCOUNT_ID\",\"categoryId\":\"$CAT_FOOD\",\"isPaid\":true}"
```

### Check account balance (auto-updated)

```bash
curl $BASE/api/accounts/$ACCOUNT_ID -H "Authorization: Bearer $TOKEN"
# balance should now be 2000 + 5000 - 350 = 6650
```

### Create a budget and check spending

```bash
curl -X POST $BASE/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"categoryId\":\"$CAT_FOOD\",\"limitAmount\":800,\"month\":6,\"year\":2026}"

# List budgets — spentAmount is calculated from actual transactions
curl "$BASE/api/budgets?month=6&year=2026" -H "Authorization: Bearer $TOKEN"
```

### Create a goal and make deposits

```bash
curl -X POST $BASE/api/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Emergency Fund","targetAmount":10000,"targetDate":"2026-12-31"}'

GOAL_ID=<id from response>

curl -X POST $BASE/api/goals/$GOAL_ID/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500}'
```

### Admin operations

```bash
# Login as admin
ADMIN_TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@financesystem.com","password":"Admin@2026"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# List all users
curl $BASE/api/auth/all-users -H "Authorization: Bearer $ADMIN_TOKEN"

# Promote a user
curl -X PATCH $BASE/api/auth/users/<user-id>/promote \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Error responses

All errors follow a consistent format:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Account not found",
  "path": "/api/accounts/abc-123",
  "timestamp": "2026-06-07T11:00:00"
}
```

| HTTP Status | Error Code | When |
|---|---|---|
| 400 | `BAD_REQUEST` | Validation failure |
| 401 | `UNAUTHORIZED` | Invalid or missing JWT, wrong password |
| 403 | `FORBIDDEN` | Insufficient role (e.g. USER hitting admin endpoint) |
| 404 | `NOT_FOUND` | Resource does not exist or belongs to another user |
| 409 | `CONFLICT` | Duplicate email, account name, budget for same month/category |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

---

## Project Roadmap

- [x] JWT authentication and role-based authorization
- [x] User management with admin capabilities
- [x] Category management with protected defaults
- [x] Account CRUD with automatic balance tracking
- [x] Transaction CRUD with filters, pagination, and balance reconciliation
- [x] Budget management with real-time spending calculation
- [x] Goal management with deposit flow and automatic completion detection
- [ ] Reports module (monthly summary, category breakdown, cash flow, net worth evolution)
- [ ] Swagger / OpenAPI UI documentation
- [ ] Unit tests (JUnit 5 + Mockito)
- [ ] Integration tests (@WebMvcTest)
- [ ] CORS configuration for frontend integration

---

## License

This project is licensed under the MIT License.
