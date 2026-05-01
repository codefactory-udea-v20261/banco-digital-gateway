# Banco Digital - API Gateway

The API Gateway is the entry point for all client requests in the Banco Digital microservices architecture. It routes requests to downstream services, validates JWT tokens, and applies resilience patterns.

## Architecture

- **Spring Cloud Gateway** - Reactive API Gateway
- **JWT Authentication** - Validates tokens before routing
- **Resilience4j** - Circuit breaker and rate limiting per route
- **Eureka Client** - Service discovery
- **Micrometer Tracing** - Distributed tracing with Zipkin

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker (optional)

### Build

```bash
mvn clean package -DskipTests
```

### Run

```bash
# Set environment variables
export APP_PROFILE=local
export EUREKA_URL=http://localhost:8761/eureka/
export JWT_SECRET=your_base64_encoded_256bit_secret_here

mvn spring-boot:run
```

### Docker

```bash
docker build -t banco-digital-gateway .
docker run -p 8000:8000 \
  -e APP_PROFILE=local \
  -e EUREKA_URL=http://localhost:8761/eureka/ \
  -e JWT_SECRET=your_secret \
  banco-digital-gateway
```

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_PROFILE` | Spring profile | `local` |
| `APP_PORT` | Server port | `8000` |
| `EUREKA_URL` | Eureka server URL | `http://localhost:8761/eureka/` |
| `JWT_SECRET` | Base64-encoded 256-bit secret | *(required)* |
| `JWT_EXPIRATION_MS` | Token expiration | `3600000` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000` |

## Endpoints

- **Gateway**: `http://localhost:8000`
- **Health**: `http://localhost:8000/actuator/health`
- **Prometheus metrics**: `http://localhost:8000/actuator/prometheus`

## Routes

| Path | Service | Port |
|------|---------|------|
| `/api/v1/auth/**` | Identity Service | 8081 |
| `/api/v1/clientes/**` | Core Banking | 8080 |
| `/api/v1/cuentas/**` | Core Banking | 8080 |
| `/api/v1/transacciones/**` | Core Banking | 8080 |
| `/api/v1/reportes/**` | Reporting Service | 8083 |
| `/api/v1/audit/**` | Audit Service | 8082 |

## Public Endpoints

These endpoints do not require JWT authentication:

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/actuator/health`

## Testing

```bash
mvn test
```
