# URL Shortener

A production-oriented URL Shortener backend built with **Java 17 and Spring Boot** that converts long URLs into compact short URLs and provides scalable URL redirection, caching, rate limiting, authentication, and click analytics.

The system uses **MySQL** for persistent storage, **Redis** for caching and rate limiting, **Apache Kafka** for asynchronous click-event processing, and **Flyway** for database schema versioning. The application is fully containerized using Docker Compose.

---

## Features

### URL Shortening
- Convert long URLs into short, unique URLs.
- Generate compact URL identifiers using **Base62 encoding**.
- Persist original and shortened URLs in MySQL.
- Support URL retrieval and redirection.
- Handle URL expiration and validation.

### Authentication & Security
- User authentication using **JWT**.
- Stateless authentication using Spring Security.
- Protected URL-management and analytics operations.
- Password handling and authentication flow managed through the security layer.

### Redis Caching
- Cache frequently accessed short URLs using Redis.
- Reduce repeated database lookups during URL redirection.
- Improve response time for high-frequency URLs.
- Cache invalidation is handled when URL state changes.

### Rate Limiting
- Redis-backed rate limiting for API protection.
- Restricts excessive requests from clients.
- Helps protect URL creation and redirection endpoints from abuse.
- Provides a scalable rate-limiting mechanism across application instances.

### Click Analytics
- Track URL access events.
- Capture click-related information such as request metadata.
- Publish click events asynchronously through Kafka.
- Process click events separately from the main redirection flow.
- Store analytics data for later querying.

### Kafka Event Processing
- Uses Apache Kafka for asynchronous click-event processing.
- URL redirection does not need to wait for analytics persistence.
- Decouples the URL-serving path from analytics processing.
- Improves scalability of analytics workloads.

### Database Management
- MySQL used as the primary persistent database.
- Flyway used for database schema versioning and migrations.
- Database schema is automatically initialized when the application starts.
- Supports reproducible database setup across environments.

### API Documentation
- Integrated **Swagger / OpenAPI** documentation.
- APIs can be explored and tested directly through Swagger UI.

### Validation & Error Handling
- Request validation for API inputs.
- Centralized exception handling.
- Consistent error responses for invalid requests and application failures.

---

# System Architecture

```text
                         ┌──────────────────────┐
                         │       Client         │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Spring Boot API    │
                         │   REST Controllers   │
                         └──────────┬───────────┘
                                    │
                    ┌───────────────┼────────────────┐
                    │               │                │
                    ▼               ▼                ▼
              ┌──────────┐   ┌────────────┐   ┌──────────────┐
              │   JWT    │   │   Redis    │   │ Rate Limiter │
              │ Security │   │   Cache    │   │    Redis     │
              └──────────┘   └────────────┘   └──────────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    Service Layer     │
                         │ URL Shortening Logic │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │        MySQL         │
                         │ Persistent URL Data  │
                         └──────────────────────┘

                  URL Click / Analytics Flow

                         URL Redirect Request
                                  │
                                  ▼
                         ┌────────────────┐
                         │ Redis / MySQL  │
                         └───────┬────────┘
                                 │
                                 ▼
                         Redirect to Target
                                 │
                                 ▼
                         Publish Click Event
                                 │
                                 ▼
                         ┌────────────────┐
                         │     Kafka      │
                         └───────┬────────┘
                                 │
                                 ▼
                         Analytics Consumer
                                 │
                                 ▼
                         Analytics Storage
