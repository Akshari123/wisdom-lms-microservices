# Wisdom LMS Microservices

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0%2B-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)

An enterprise-grade, distributed **Learning Management System (LMS)** designed with a cloud-native microservices architecture. Built using **Java 21**, **Spring Boot**, **Spring Cloud Gateway**, and **MongoDB**, this system provides independent scalability, robust inter-service security, and full containerization with Docker Compose.

---

## 🏛️ System Architecture

The ecosystem utilizes an **API Gateway pattern** where all client traffic enters through a single entry point (Port `8080`). The Gateway applies rate limiting, handles route dispatching, and automatically injects authentication headers (`X-API-KEY`) when communicating with backend microservices.

```
                            ┌────────────────────────┐
                            │   Client / Frontend    │
                            └───────────┬────────────┘
                                        │ (HTTP Requests)
                                        ▼
                            ┌────────────────────────┐
                            │      API Gateway       │
                            │     localhost:8080     │
                            └───────────┬────────────┘
                                        │
     ┌──────────────┬───────────────────┼───────────────────┬──────────────┐
     │              │                   │                   │              │
     ▼              ▼                   ▼                   ▼              ▼
┌──────────┐  ┌───────────┐       ┌───────────┐       ┌───────────┐  ┌───────────┐
│   Auth   │  │  Student  │       │  Teacher  │       │   Class   │  │  Payment  │
│ Service  │  │  Service  │       │  Service  │       │  Service  │  │  Service  │
│  :8082   │  │   :8081   │       │   :8083   │       │   :8084   │  │   :8085   │
└────┬─────┘  └─────┬─────┘       └─────┬─────┘       └─────┬─────┘  └─────┬─────┘
     │              │                   │                   │              │
     └──────────────┴───────────────────┼───────────────────┴──────────────┘
                                        ▼
                            ┌────────────────────────┐
                            │    MongoDB Database    │
                            │     localhost:27017    │
                            └────────────────────────┘
```

---

## 🛠️ Technology Stack

- **Core Runtime:** Java 21 (Eclipse Temurin LTS)
- **Framework:** Spring Boot, Spring Web / WebFlux
- **API Gateway:** Spring Cloud Gateway
- **Database & Persistence:** MongoDB, Spring Data MongoDB
- **Build & Dependency Management:** Apache Maven (Multi-module wrappers)
- **Containerization:** Docker (Multi-stage builds) & Docker Compose
- **API Documentation & Exploration:** OpenAPI 3.0 / Swagger UI (SpringDoc OpenAPI)
- **Version Control & Collaboration:** Git & GitHub Flow

---

## 📦 Microservices Catalog

| Service | Port | Database Name | Primary Responsibility |
| :--- | :---: | :--- | :--- |
| **`gateway-service`** | `8080` | *None* | Central ingress router, rate limiting, and automated API-key header injection |
| **`student-service`** | `8081` | `studentdb` | Student registration, profile management, and student records |
| **`auth-service`** | `8082` | `wisdom_lms_auth` | User account registration, credentials management, and authentication |
| **`teacher-service`** | `8083` | `wisdom_teacher_db` | Teacher onboarding, contact info, and subject assignments |
| **`class-service`** | `8084` | `classdb` | Class scheduling, subject assignments, and timetable management |
| **`payment-service`** | `8085` | `wisdom_payment` | Payment processing, transaction history, and payment status updates |
| **`mongodb`** | `27017` | *Shared Instance* | Document database providing isolated logical databases per service |

---

## 🚪 API Gateway & Routing Configuration

The **Spring Cloud API Gateway** (`:8080`) provides a unified API surface. Clients interact solely with the gateway, eliminating the need to track individual microservice ports or supply internal security tokens.

| Client Gateway Route | Target Microservice URI | Injected Security Header |
| :--- | :--- | :--- |
| `/api/auth/**` | `http://auth-service:8082` | Rate Limiter (`simpleRateLimiter`) |
| `/api/students/**` | `http://student-service:8081` | `X-API-KEY: student-secret-2026` |
| `/api/teachers/**` | `http://teacher-service:8083` | `X-API-KEY: wisdom-teacher-secret-2026` |
| `/api/classes/**` | `http://class-service:8084` | `X-API-KEY: wisdom-lms-secret-key` |
| `/api/payments/**` | `http://payment-service:8085` | `X-API-KEY: wisdom-payment-key-2026` *(Rewritten to `/payments/**`)* |

---

## 🔐 Security & API Key Authentication

Backend services validate incoming requests using a dedicated **`X-API-KEY`** filter. Requests originating outside the Gateway or lacking a valid API key receive an **HTTP 401 Unauthorized** response.

> **Development / Demo API Keys:**
> - **Student Service:** `student-secret-2026`
> - **Teacher Service:** `wisdom-teacher-secret-2026`
> - **Class Service:** `wisdom-lms-secret-key`
> - **Payment Service:** `wisdom-payment-key-2026`
> - **Auth Service:** Public endpoints (handles user registration and authentication)

*Note: All Swagger UI and OpenAPI documentation endpoints (`/v3/api-docs/**`, `/swagger-ui/**`) are whitelisted across all services for seamless testing.*

---

## 📖 Interactive Swagger / OpenAPI Documentation

Each microservice exposes an interactive **Swagger UI** for testing and inspecting REST models:

- **Student Service Swagger:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Auth Service Swagger:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **Teacher Service Swagger:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- **Class Service Swagger:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html)
- **Payment Service Swagger:** [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html)

---

## 📡 REST API Endpoint Reference

All endpoints below can be invoked directly through the **API Gateway** (`http://localhost:8080`):

### 1. Authentication Service (`/api/auth`)
- `GET /api/auth/test` — Health check endpoint
- `POST /api/auth/register` — Register a new user account
  ```json
  {
    "username": "john_doe",
    "password": "securePassword123"
  }
  ```
- `POST /api/auth/login` — Authenticate and log in

### 2. Student Service (`/api/students`)
- `GET /api/students` — Retrieve all students
- `GET /api/students/{id}` — Retrieve a student by ID
- `POST /api/students` — Register a new student
  ```json
  {
    "firstName": "Nuwan",
    "lastName": "Pradeep",
    "email": "nuwan.p@wisdom.edu"
  }
  ```
- `PUT /api/students/{id}` — Update student profile
- `DELETE /api/students/{id}` — Remove student record

### 3. Teacher Service (`/api/teachers`)
- `GET /api/teachers` — Retrieve all teachers
- `GET /api/teachers/{id}` — Retrieve teacher by ID
- `POST /api/teachers` — Add a new teacher
  ```json
  {
    "name": "Prof. Bandara",
    "email": "bandara@wisdom.edu",
    "phone": "0773344556",
    "subject": "Chemistry"
  }
  ```
- `PUT /api/teachers/{id}` — Update teacher details
- `DELETE /api/teachers/{id}` — Remove teacher record

### 4. Class Service (`/api/classes`)
- `GET /api/classes` — Retrieve all classes
- `GET /api/classes/{id}` — Retrieve class details by ID
- `POST /api/classes` — Schedule a new class
  ```json
  {
    "subject": "Organic Chemistry",
    "year": "2026",
    "classDate": "2026-08-28",
    "classTime": "14:00:00"
  }
  ```
- `PUT /api/classes/{id}` — Update class schedule
- `DELETE /api/classes/{id}` — Remove class

### 5. Payment Service (`/api/payments`)
- `POST /api/payments/process` — Process a course payment
  ```json
  {
    "userId": 301,
    "orderId": 801,
    "amount": 9500.00,
    "paymentMethod": "BANK_TRANSFER"
  }
  ```
- `GET /api/payments/history?userId={userId}` — Retrieve payment history (optionally filtered by student `userId`)
- `GET /api/payments/{id}` — Get payment receipt by database ID
- `PUT /api/payments/{id}/status` — Update payment transaction status (`COMPLETED`, `PENDING`, `FAILED`)
  ```json
  {
    "status": "COMPLETED"
  }
  ```

---

## 🐳 Docker Setup & Single-Command Deployment

The entire microservices ecosystem (MongoDB + 6 microservices) is fully containerized.

### 1. Build and Start All Services
```bash
# Build multi-stage container images
docker compose build

# Start all containers in detached mode
docker compose up -d

# Verify all containers are running and healthy
docker compose ps
```

### 2. Verified Docker Deployment Containers

| Container Name | Service | Status | Port Mapping |
| :--- | :--- | :---: | :--- |
| `wisdom-mongodb` | MongoDB | `Up (healthy)` | `27017:27017` |
| `wisdom-gateway-service` | API Gateway | `Up` | `8080:8080` |
| `wisdom-student-service` | Student Service | `Up` | `8081:8081` |
| `wisdom-auth-service` | Auth Service | `Up` | `8082:8082` |
| `wisdom-teacher-service` | Teacher Service | `Up` | `8083:8083` |
| `wisdom-class-service` | Class Service | `Up` | `8084:8084` |
| `wisdom-payment-service` | Payment Service | `Up` | `8085:8085` |

### 3. Viewing Logs
```bash
# View aggregated logs
docker compose logs -f

# View logs for a specific service
docker compose logs -f gateway-service
docker compose logs -f payment-service
```

### 4. Stopping the System
```bash
# Stop and remove containers and network
docker compose down
```

---

## 💻 Local Development Setup (Maven)

To run individual microservices locally without Docker, ensure a local MongoDB instance is running on port `27017`.

```bash
# Start MongoDB locally or via Docker
docker run -d -p 27017:27017 --name local-mongo mongo:latest

# Run any microservice using the Maven wrapper
cd student-service
.\mvnw.cmd spring-boot:run

# In separate terminals, run the remaining services:
cd ../auth-service    && .\mvnw.cmd spring-boot:run
cd ../teacher-service && .\mvnw.cmd spring-boot:run
cd ../class-service   && .\mvnw.cmd spring-boot:run
cd ../payment-service && .\mvnw.cmd spring-boot:run
cd ../gateway-service && .\mvnw.cmd spring-boot:run
```

---

## 🧪 Testing & Verification

### 1. Automated Unit & Integration Tests
Each microservice includes an automated test suite. Run tests across any service with:

```bash
# Example: Run tests in payment-service
cd payment-service
.\mvnw.cmd clean test
```

### 2. End-to-End Gateway Verification
All Gateway routes have been validated through end-to-end HTTP request testing on port `8080`:

```powershell
# Test Auth Registration through Gateway
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"username":"student1","password":"password123"}'

# Test Student Creation through Gateway
Invoke-RestMethod -Uri "http://localhost:8080/api/students" -Method Post -ContentType "application/json" -Body '{"firstName":"Nuwan","lastName":"Pradeep","email":"nuwan@wisdom.edu"}'

# Test Payment Processing through Gateway
Invoke-RestMethod -Uri "http://localhost:8080/api/payments/process" -Method Post -ContentType "application/json" -Body '{"userId":101,"orderId":201,"amount":5000.00,"paymentMethod":"CREDIT_CARD"}'
```

---

## 📋 Project Submission Checklist

- [x] **Microservices Implemented:** 5 domain services + 1 central API Gateway.
- [x] **Database Architecture:** Unified MongoDB persistence across all services with isolated databases.
- [x] **API Gateway Routing:** Full path mapping, reverse proxying, and CORS handling on port `8080`.
- [x] **Inter-Service Security:** Automated `X-API-KEY` token injection and validation.
- [x] **Interactive Documentation:** SpringDoc OpenAPI 3.0 / Swagger UI on all services.
- [x] **Unit & Build Tests:** All Maven builds and tests compile with 0 errors / 0 failures.
- [x] **Docker Multi-Stage Builds:** Standardized Java 21 alpine Dockerfiles for minimal image footprint.
- [x] **Docker Compose Orchestration:** Single-command bootstrap with healthcheck dependencies.
- [x] **End-to-End Gateway Routing:** 100% verified live routes across all microservices.
- [x] **Project Documentation:** Comprehensive architectural and endpoint guides.

---

## 🌿 GitHub Collaboration Workflow

To maintain clean repository hygiene and adhere to industry pair-programming standards:

1. **Feature Branches:** Create dedicated feature branches for new enhancements (`git checkout -b feature/<feature-name>`).
2. **Review & Integration:** Test changes locally and in Docker Compose before committing.
3. **Pull Requests (PR):** Open a Pull Request against `main` for code review. Direct pushes to `main` are restricted.
