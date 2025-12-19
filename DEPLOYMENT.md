# Deployment Guide

This guide covers deploying the Time-Locked Release system to production environments.

## Prerequisites

- Docker and Docker Compose (or Kubernetes)
- MySQL 8.0+ database
- JDK 21 (if running without Docker)
- Node 18+ (if building frontend separately)
- Reverse proxy (Nginx/Traefik) for production

## Docker Deployment

### Production Docker Compose

Create a `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${DB_NAME:-appdb}
      MYSQL_USER: ${DB_USER:-appuser}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    volumes:
      - dbdata:/var/lib/mysql
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  app:
    build:
      context: ../demo
      dockerfile: ../Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${DB_NAME:-appdb}?useSSL=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-appuser}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_ISSUER: ${JWT_ISSUER}
      APP_RELEASE_WEBHOOK_URL: ${WEBHOOK_URL:-}
      SERVER_PORT: "8080"
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - app-network

  ui:
    build:
      context: ..
      dockerfile: ./docker/release-ui.Dockerfile
      args:
        VITE_API_URL: ${API_URL:-http://localhost:8081}
    restart: unless-stopped
    networks:
      - app-network

volumes:
  dbdata:

networks:
  app-network:
    driver: bridge
```

### Environment Variables

Create a `.env` file:

```bash
# Database
DB_NAME=appdb
DB_USER=appuser
DB_PASSWORD=your_secure_password_here
DB_ROOT_PASSWORD=your_secure_root_password_here

# JWT
JWT_SECRET=your_base64_encoded_secret_here
JWT_ISSUER=yourdomain.com

# API
API_URL=https://api.yourdomain.com

# Webhook (optional)
WEBHOOK_URL=https://webhook.yourdomain.com/releases
```

### Deploy

```bash
docker compose -f docker-compose.prod.yml up -d
```

## Kubernetes Deployment

### Database (MySQL)

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_DATABASE
          value: "appdb"
        - name: MYSQL_USER
          value: "appuser"
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: root-password
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
```

### Backend Application

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: time-locked-release-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: time-locked-release-backend
  template:
    metadata:
      labels:
        app: time-locked-release-backend
    spec:
      containers:
      - name: app
        image: your-registry/time-locked-release:latest
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql:3306/appdb?useSSL=true"
        - name: SPRING_DATASOURCE_USERNAME
          value: "appuser"
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secret
              key: jwt-secret
        - name: JWT_ISSUER
          value: "yourdomain.com"
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: time-locked-release-backend
spec:
  selector:
    app: time-locked-release-backend
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

## Security Best Practices

### 1. Secrets Management

Never commit secrets to version control. Use:
- Kubernetes Secrets
- Docker Secrets
- Environment variables (injected securely)
- Secret management services (AWS Secrets Manager, HashiCorp Vault)

### 2. Database Security

- Use strong, unique passwords
- Enable SSL/TLS for database connections
- Restrict database access to application network only
- Regular security updates

### 3. Application Security

- Use HTTPS in production
- Configure CORS properly
- Rate limiting
- Input validation
- Regular dependency updates

### 4. JWT Security

- Use strong, randomly generated secrets
- Set appropriate token expiration
- Rotate secrets periodically
- Use HTTPS for token transmission

## Monitoring

### Health Checks

Configure health check endpoints:
- `/actuator/health` - Application health
- Database connectivity
- External service availability

### Logging

- Centralized logging (ELK, Loki, CloudWatch)
- Log aggregation
- Error tracking (Sentry, Rollbar)
- Performance monitoring (APM tools)

### Metrics

- Prometheus metrics endpoint: `/actuator/prometheus`
- Custom business metrics
- Database performance metrics
- Application performance metrics

## Backup Strategy

### Database Backups

```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec mysql-container mysqldump -u appuser -p appdb > backup_$DATE.sql
```

### Automated Backups

- Schedule regular backups
- Store backups in secure location
- Test restore procedures
- Retention policy

## Scaling

### Horizontal Scaling

- Stateless application design allows horizontal scaling
- Use load balancer
- Session management (stateless with JWT)
- Database connection pooling

### Vertical Scaling

- Monitor resource usage
- Adjust container resources as needed
- Database optimization

## CI/CD Pipeline

### Example GitHub Actions

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build and push Docker image
        run: |
          docker build -t your-registry/time-locked-release:${{ github.sha }} .
          docker push your-registry/time-locked-release:${{ github.sha }}
      - name: Deploy to production
        run: |
          kubectl set image deployment/time-locked-release-backend \
            app=your-registry/time-locked-release:${{ github.sha }}
```

## Troubleshooting Production Issues

### Application Won't Start

1. Check logs: `docker logs <container-name>`
2. Verify environment variables
3. Check database connectivity
4. Verify JWT configuration

### Database Connection Issues

1. Verify database is running
2. Check network connectivity
3. Verify credentials
4. Check firewall rules

### Performance Issues

1. Monitor resource usage
2. Check database performance
3. Review application logs
4. Database query optimization
5. Connection pool tuning

## Rollback Procedure

### Docker

```bash
# Rollback to previous version
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml up -d --scale app=0
# Restore previous image
docker compose -f docker-compose.prod.yml up -d
```

### Kubernetes

```bash
# Rollback deployment
kubectl rollout undo deployment/time-locked-release-backend
```

## Maintenance

### Regular Tasks

- [ ] Security updates
- [ ] Dependency updates
- [ ] Database optimization
- [ ] Log rotation
- [ ] Backup verification
- [ ] Performance review
- [ ] Capacity planning

---

For more details, see the main [README.md](./README.md) and [CONFIGURATION.md](./CONFIGURATION.md).

