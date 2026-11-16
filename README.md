# Uprophet Backend

A Modernized Social Networking API for Expressive Quotes

## Overview

**Uprophet** is a full-stack social networking platform that provides users a dedicated space to express themselves by creating and sharing quotes. Originally developed in 2015 using PHP, the API was later rebuilt with Node.js and Express. This backend is that same API, re-implemented in **Spring Boot** so it can run beside the existing app.

It uses the current MySQL schema. Passwords and the `upUserId` JWT cookie are verified the same way, so existing users and sessions keep working.

## Key Features

* **Quote Engine**: Create, read, and delete quotes, with likes, comments, a home feed, and a random explore feed.
* **JWT Authentication**: Secure user sessions using **JSON Web Tokens** (access and refresh) stored in an HttpOnly cookie.
* **Request Validation**: Jakarta Bean Validation on incoming request bodies.
* **Email Integration**: Password-reset email powered by **Resend**.
* **Existing Database**: Spring Data JPA reads and writes the current MySQL schema. Hibernate does not create or alter tables.
* **Production Reliability**: Architected for modern cloud environments like **Railway**.

## Technical Stack

* **Language**: Java 17
* **Framework**: Spring Boot 4
* **Database**: MySQL
* **Persistence**: Spring Data JPA
* **Validation**: Jakarta Bean Validation
* **Authentication**: JWT (jjwt) and Spring Security
* **Email Service**: Resend
* **Hosting**: Railway

---

## Installation & Setup

### 1. Prerequisites

* **Java**: Version 17.
* **MySQL**: The existing `uprophet` database. This app does not create the schema.
* **Railway Account**: Optional, for cloud deployment.

### 2. Clone the Repository

```bash
git clone https://github.com/Jenkins1128/uprophet-backend-spring.git
cd uprophet-backend-spring
```

### 3. Install Dependencies

The Maven wrapper downloads dependencies. A separate Maven install is not required.

```bash
./mvnw -q -DskipTests package
```

### 4. Environment Configuration

Copy the example file and fill in the same values the Node app uses:

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

`application-local.yml` is gitignored. It overrides `application.yml` unless the `prod` profile is active. Quote `site-key` and `nonce-salt` when the value contains `#`, or YAML treats the rest of the line as a comment.

```yaml
server:
  port: 3001

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/uprophet?serverTimezone=UTC
    username: your_db_user
    password: your_db_password

app:
  resend:
    api-key: your_resend_api_key
  cors:
    allowed-origins: http://localhost:3000,https://uprophet.com
  jwt:
    access-token-secret: your_access_secret
    access-token-life: 120s
    refresh-token-secret: your_refresh_secret
    refresh-token-life: 365d
  password:
    site-key: "your_site_key"
    nonce-salt: "your_nonce_salt"
```

`ACCESS_TOKEN_SECRET`, `REFRESH_TOKEN_SECRET`, `SITE_KEY`, and `NONCE_SALT` must match the Node backend.

On Railway, set `SPRING_PROFILES_ACTIVE=prod` and provide the variables instead of the local file:

| Variable | Purpose |
| --- | --- |
| `PORT` | Server port. Defaults to `3001`. |
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` | Database connection. |
| `ALLOWED_ORIGINS` | Comma-separated CORS origins. |
| `ACCESS_TOKEN_SECRET`, `ACCESS_TOKEN_LIFE` | Access JWT. |
| `REFRESH_TOKEN_SECRET`, `REFRESH_TOKEN_LIFE` | Refresh JWT. |
| `RESEND_API_KEY` | Password-reset email. |
| `SITE_KEY`, `NONCE_SALT` | Password hash. |

### 5. Database

Point the datasource at the database the Node app already uses. `ddl-auto` is `none` and Flyway is off, so starting this app does not change tables.

### 6. Start the Application

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:3001`. To build and run the jar:

```bash
./mvnw -DskipTests package
java -jar target/uprophet-backend-0.0.1-SNAPSHOT.jar
```

Run the tests with:

```bash
./mvnw test
```
