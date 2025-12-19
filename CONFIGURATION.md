# Configuration Guide

This guide covers all configuration options for the Time-Locked Release system.

## Environment Variables

### Backend Configuration

#### Required Variables

| Variable | Description | Example | Notes |
|----------|-------------|---------|-------|
| `JWT_SECRET` | Base64-encoded secret key for JWT signing | `8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0=` | Must be 32+ bytes, Base64 encoded |
| `JWT_ISSUER` | JWT issuer claim | `example.com` | Must match `jwt.issuer` in `application.yml` |

#### Optional Variables

| Variable | Description | Example | Default |
|----------|-------------|---------|---------|
| `APP_RELEASE_WEBHOOK_URL` | Webhook URL to call on release execution | `https://webhook.site/abc123` | Empty (disabled) |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:mysql://localhost:3307/appdb?useSSL=false&serverTimezone=UTC` | See `application.yml` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `appuser` | See `application.yml` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `apppass` | See `application.yml` |
| `SERVER_PORT` | Server port | `8081` | `8081` |

### Frontend Configuration

| Variable | Description | Example | Default |
|----------|-------------|---------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:8081` | `http://localhost:8081` |

## Application Configuration

### Backend (`demo/src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: time-locked-release
  datasource:
    url: jdbc:mysql://localhost:3307/appdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: appuser
    password: apppass
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: UTC
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

jwt:
  issuer: example.com
  ttl-minutes: 120
  secret: ${JWT_SECRET}

app:
  release:
    webhook-url: ${APP_RELEASE_WEBHOOK_URL:}
```

## Docker Configuration

### Docker Compose

The `docker/docker-compose.yml` file configures:
- MySQL 8.0 database
- Spring Boot backend
- Nginx serving the React frontend

### Customization

To customize ports, modify the `ports` section:

```yaml
services:
  db:
    ports:
      - "3307:3306"  # Change 3307 to your preferred port
  app:
    ports:
      - "8081:8080"  # Change 8081 to your preferred port
  ui:
    ports:
      - "5173:80"    # Change 5173 to your preferred port
```

## Database Configuration

### Connection Settings

Default MySQL connection:
- Host: `localhost`
- Port: `3307` (when using Docker Compose)
- Database: `appdb`
- Username: `appuser`
- Password: `apppass`

### Production Recommendations

1. **Use strong passwords**: Change default passwords in production
2. **Enable SSL**: Set `useSSL=true` in connection URL
3. **Connection pooling**: Spring Boot uses HikariCP by default
4. **Backup strategy**: Implement regular database backups

## Security Configuration

### JWT Secret Generation

Generate a secure JWT secret:

```bash
# Using OpenSSL
openssl rand -base64 32

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### CORS Configuration

CORS is configured in `SecurityConfig.java`. To add new origins:

```java
cfg.setAllowedOrigins(java.util.List.of(
    "http://localhost:3000",
    "http://localhost:5173",
    "https://yourdomain.com"  // Add your domain
));
```

## Webhook Configuration

### Setting Up Webhooks

1. **Get a webhook URL**: Use services like [webhook.site](https://webhook.site) for testing
2. **Configure in application.yml**:
   ```yaml
   app:
     release:
       webhook-url: "https://webhook.site/your-unique-id"
   ```
3. **Or use environment variable**:
   ```bash
   export APP_RELEASE_WEBHOOK_URL="https://webhook.site/your-unique-id"
   ```

### Webhook Payload

When a release executes, the webhook receives:

```json
{
  "releaseId": 123,
  "title": "Release Title",
  "payload": "{\"key\": \"value\"}"
}
```

### Webhook Retry

The system automatically retries failed webhooks:
- Maximum 3 attempts
- Exponential backoff (2s, 4s, 6s delays)
- 10-second timeout per attempt
- Non-retryable errors (4xx except 408, 429) fail immediately

## Logging Configuration

### Log Levels

Configure in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.example.timelock: DEBUG  # For detailed application logs
    org.springframework.web: INFO
    org.hibernate: WARN
```

### Log Output

- Console: Default output
- File: Add `logging.file.name: logs/application.log`

## Monitoring

### Actuator Endpoints

Available endpoints (when enabled):
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

## Production Checklist

- [ ] Change default database passwords
- [ ] Generate secure JWT secret
- [ ] Configure proper CORS origins
- [ ] Set up SSL/TLS for database connections
- [ ] Configure proper logging
- [ ] Set up monitoring and alerts
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set up webhook endpoints
- [ ] Configure environment-specific settings

