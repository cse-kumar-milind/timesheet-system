# Timesheet & Leave Management System

Enterprise-grade workforce management application built with Spring Boot Microservices.

## Tech Stack
- **Backend**: Spring Boot 4.x Microservices
- **Security**: Spring Security + JWT
- **Gateway**: Spring Cloud Gateway (WebFlux)
- **Registry**: Netflix Eureka
- **Database**: MySQL + JPA/Hibernate
- **Java**: 17

## Architecture
```
React Frontend
      ↓
API Gateway :8080
      ↓
┌──────────────────────────────────────┐
│  Auth    Timesheet   Leave   Admin   │
│  :8081    :8082      :8083   :8084   │
└──────────────────────────────────────┘
      ↓
   MySQL
```

## Services

| Service | Port | Status |
|---------|------|--------|
| Eureka Server | 8761 | ✅ Complete |
| API Gateway | 8080 | ✅ Complete |
| Auth Service | 8081 | ✅ Complete |
| Timesheet Service | 8082 | ✅ Complete |
| Leave Service | 8083 | ✅ Complete |
| Admin Service | 8084 | ✅ Complete |

## How to Run

### Prerequisites
- Java 17
- MySQL 8.x
- Maven 3.x

### Start Order
```
1. Eureka Server  → http://localhost:8761
2. Auth Service   → http://localhost:8081
3. API Gateway    → http://localhost:8080
4. Other services...
```

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /auth/signup | Public | Register user |
| POST | /auth/login | Public | Login + get JWT |
| GET | /timesheet/** | JWT Required | Timesheet APIs |
| GET | /leave/** | JWT Required | Leave APIs |
| GET | /admin/** | JWT Required | Admin APIs |

## Progress
- [x] Phase 0 - Project Understanding
- [x] Phase 1 - Architecture Design
- [x] Phase 2 - Auth Service
- [x] Phase 3 - API Gateway
- [x] Phase 4 - Timesheet Service
- [x] Phase 5 - Leave Service
- [x] Phase 6 - Admin Service
