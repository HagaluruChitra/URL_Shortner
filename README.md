# URL Shortener

Spring Boot URL-shortener service with MySQL persistence, Redis-backed caching and rate limiting, Kafka click-event processing, Flyway migrations, JWT authentication, and Swagger UI.

## Run with Docker

1. Ensure Docker Desktop is running.
2. From this directory, start the complete stack:

   ```powershell
   docker compose up --build
   ```

3. Confirm the application is healthy:

   ```powershell
   Invoke-WebRequest http://localhost:8080/actuator/health
   ```

The service is available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

Docker creates the MySQL database as `Shorturl` and persists it in the `mysql-data` Docker volume. The application connects to it through the `mysql` service and Flyway creates the schema automatically.

To stop the stack while keeping the database data:

```powershell
docker compose down
```

To stop the stack and remove its database volume:

```powershell
docker compose down -v
```

## Configuration

The Docker Compose file configures the required services and these application settings:

| Setting | Docker value |
| --- | --- |
| Database | `Shorturl` |
| Database host | `mysql:3306` |
| Redis host | `redis:6379` |
| Kafka broker | `kafka:9092` |
| Application port | `8080` |

Set `JWT_SECRET` before starting Docker Compose in any environment where the default development key is not appropriate. It must be a Base64-encoded key at least 32 bytes long.

## Local Development

The application keeps an H2 in-memory fallback database named `Shorturl` for local development. Start it with:

```powershell
.\mvnw.cmd spring-boot:run
```

Run the test suite with:

```powershell
.\mvnw.cmd test
```
