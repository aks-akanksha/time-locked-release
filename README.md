# Time-Locked Release System

A comprehensive, production-ready system for **creating**, **scheduling**, **approving**, and **executing** time-locked releases with full audit trails, webhook support, and a modern web interface.

## 🎯 Overview

Time-Locked Release is an enterprise-grade release management system that ensures releases are executed only after proper approval and at scheduled times. It provides role-based access control, comprehensive audit logging, webhook integrations, and a beautiful modern UI.

**Tech Stack:**
- **Backend**: Spring Boot 3.3.2, Java 21, MySQL 8.0, JWT, Flyway, OpenAPI
- **Frontend**: React 19, Vite, TypeScript
- **Infrastructure**: Docker, Docker Compose, Nginx

---

## ✨ Features

### Core Features
- ✅ **Create Releases** - Create releases with title, description, and optional JSON payload
- ✅ **Schedule Releases** - Schedule releases with UTC timestamps
- ✅ **Approval Workflow** - Role-based approval system
- ✅ **Time-Locked Execution** - Execute only when approved and scheduled time has passed
- ✅ **Auto-Execution** - Background jobs automatically execute due releases
- ✅ **Release Cancellation** - Cancel releases before execution
- ✅ **Audit Logging** - Complete audit trail of all release actions
- ✅ **Webhook Integration** - Automatic webhook calls on execution with retry mechanism

### Advanced Features
- 🔍 **Search & Filter** - Full-text search and status-based filtering
- 📄 **Pagination** - Efficient pagination for large release lists
- 📊 **Statistics Dashboard** - Real-time release statistics
- 🎨 **Modern UI** - Beautiful, responsive web interface
- 🔐 **Role-Based Access Control** - ADMIN, APPROVER, and USER roles
- 📝 **Input Validation** - Comprehensive validation with helpful error messages
- 🔄 **Webhook Retry** - Automatic retry with exponential backoff
- 📈 **Release History** - View complete audit log for each release

---

## 🏗️ Architecture

### Monorepo Structure

```
.
├── demo/                          # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/example/timelock/
│   │       ├── api/               # REST controllers & DTOs
│   │       ├── audit/             # Audit logging
│   │       ├── bootstrap/         # Data seeding
│   │       ├── entity/            # JPA entities
│   │       ├── exception/          # Custom exceptions
│   │       ├── execution/         # Webhook client
│   │       ├── policy/            # Security policies
│   │       ├── release/           # Release domain logic
│   │       ├── repo/              # JPA repositories
│   │       └── security/          # JWT & security config
│   ├── src/main/resources/
│   │   ├── application.yml         # Application configuration
│   │   └── db/migration/          # Flyway migrations
│   └── src/test/java/             # Unit & integration tests
├── release-ui/                    # React frontend
│   ├── src/
│   │   ├── lib/                   # API client & utilities
│   │   ├── pages/                 # React components
│   │   └── App.jsx                # Main app component
│   └── package.json
├── docker/                        # Docker configuration
│   ├── docker-compose.yml         # Multi-service setup
│   └── release-ui.Dockerfile      # Frontend Dockerfile
├── Dockerfile                     # Backend Dockerfile
├── postman/                       # API collection
└── README.md                      # This file
```

### Database Schema

- **releases** - Main release table
- **release_audit_log** - Audit trail for all actions
- **users** - User accounts with roles
- **route_scopes** - Route access control
- **release_templates** - Release templates (optional)

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose (recommended)
- OR JDK 21 + Maven + Node 18+ (for local development)

### Option 1: Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/aks-akanksha/time-locked-release.git
cd time-locked-release

# Start all services
cd docker
docker compose up -d --build

# Services will be available at:
# - API: http://localhost:8081
# - Swagger UI: http://localhost:8081/swagger-ui/index.html
# - Web UI: http://localhost:5173
```

### Option 2: Local Development

#### 1. Start Database

```bash
cd docker
docker compose up -d db
```

#### 2. Start Backend

```bash
cd demo

# Set required environment variables
export JWT_SECRET='8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0='
export JWT_ISSUER='example.com'

# Run the application
./mvnw spring-boot:run
```

Backend will be available at `http://localhost:8081`

#### 3. Start Frontend

```bash
cd release-ui

# Install dependencies
npm ci

# Start development server
npm run dev
```

Frontend will be available at `http://localhost:5173`

---

## 👥 User Roles & Permissions

The system includes three roles with different permissions:

| Role | Email | Password | Permissions |
|------|-------|-----------|-------------|
| **ADMIN** | `admin@example.com` | `admin123` | Full access: create, schedule, approve, execute, cancel |
| **APPROVER** | `approver@example.com` | `approver123` | Can approve releases |
| **USER** | `user@example.com` | `user123` | Can create and view releases |

> These users are automatically seeded on first boot.

---

## 📋 Release Workflow

### Standard Workflow

1. **Create Release** (USER/ADMIN)
   - Fill in title, description, and optional JSON payload
   - Release status: `DRAFT`

2. **Schedule Release** (ADMIN only)
   - Set scheduled execution time (UTC)
   - Release status: `SCHEDULED`

3. **Approve Release** (APPROVER/ADMIN)
   - Approver reviews and approves
   - Release status: `APPROVED`

4. **Execute Release** (ADMIN or Auto-execution)
   - Manual execution: Admin clicks "Execute"
   - Auto-execution: Background job runs every 5 seconds
   - Execution only succeeds if:
     - Status is `APPROVED`
     - `scheduledAt <= now()`
   - Release status: `EXECUTED`
   - Webhook is triggered (if configured)

### Alternative Actions

- **Cancel Release** (ADMIN): Cancel a release before execution
- **View History**: See complete audit log for any release

---

## 🔌 API Documentation

### Swagger UI

Interactive API documentation is available at:
```
http://localhost:8081/swagger-ui/index.html
```

### Key Endpoints

#### Authentication
- `POST /auth/login` - Login and get JWT token

#### Releases
- `GET /api/v1/releases` - List releases (with pagination, search, filtering)
  - Query params: `page`, `size`, `sortBy`, `sortDir`, `status`, `search`
- `GET /api/v1/releases/{id}` - Get release details
- `POST /api/v1/releases` - Create new release
- `POST /api/v1/releases/{id}/actions/schedule` - Schedule release
- `POST /api/v1/releases/{id}/actions/approve` - Approve release
- `POST /api/v1/releases/{id}/actions/execute` - Execute release
- `POST /api/v1/releases/{id}/actions/cancel` - Cancel release
- `GET /api/v1/releases/{id}/history` - Get audit log for release

#### Statistics
- `GET /api/v1/releases/statistics` - Get release statistics

### Example API Calls

#### Create Release

```bash
curl -X POST http://localhost:8081/api/v1/releases \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Production Release v1.0",
    "description": "Major feature release",
    "payloadJson": "{\"version\": \"1.0.0\", \"environment\": \"prod\"}"
  }'
```

#### Schedule Release

```bash
curl -X POST http://localhost:8081/api/v1/releases/1/actions/schedule \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduledAt": "2025-12-25T10:00:00Z"
  }'
```

#### Get Releases with Pagination

```bash
curl "http://localhost:8081/api/v1/releases?page=0&size=10&status=APPROVED&sortBy=createdAt&sortDir=desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎨 Frontend Features

### Modern UI Components

- **Statistics Dashboard**: Visual cards showing release counts by status
- **Search Bar**: Real-time search across titles and descriptions
- **Status Filter**: Filter releases by status
- **Sort Options**: Sort by date, title, or scheduled time
- **Pagination**: Navigate through pages of releases
- **Release Cards**: Beautiful cards with status badges and metadata
- **Action Buttons**: Context-aware action buttons based on role and status
- **Error/Success Messages**: Clear user feedback

### UI Screenshots

The UI features:
- Gradient background with modern card-based layout
- Color-coded status badges (DRAFT, SCHEDULED, APPROVED, EXECUTED, CANCELLED)
- Responsive design that works on all screen sizes
- Loading states and error handling
- Intuitive forms with validation

---

## 🔧 Configuration

### Environment Variables

#### Backend (Required)
- `JWT_SECRET` - Base64-encoded secret for JWT signing (32+ bytes)
- `JWT_ISSUER` - JWT issuer claim

#### Backend (Optional)
- `APP_RELEASE_WEBHOOK_URL` - Webhook URL for release execution
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SERVER_PORT` - Server port (default: 8081)

#### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8081)

### Application Configuration

See `demo/src/main/resources/application.yml` for detailed configuration options.

For complete configuration guide, see [CONFIGURATION.md](./CONFIGURATION.md).

---

## 🔐 Security

### Authentication

- JWT-based authentication
- Token expiration: 120 minutes (configurable)
- Stateless sessions

### Authorization

- Role-based access control (RBAC)
- Route-level permissions
- Policy-based interceptors

### Data Protection

- Password hashing with BCrypt
- SQL injection prevention (JPA)
- Input validation and sanitization
- CORS configuration

---

## 📊 Monitoring & Logging

### Audit Logging

Every release action is logged:
- **CREATED** - Release creation
- **SCHEDULED** - Release scheduling
- **APPROVED** - Release approval
- **EXECUTED** - Release execution
- **CANCELLED** - Release cancellation

View audit logs via: `GET /api/v1/releases/{id}/history`

### Application Logging

- Comprehensive logging throughout the application
- Log levels: DEBUG, INFO, WARN, ERROR
- Structured logging with context

### Health Checks

- Actuator health endpoint: `/actuator/health`
- Database health checks
- Service status monitoring

---

## 🧪 Testing

### Run Tests

```bash
cd demo

# Run all tests
./mvnw test

# Run specific test
./mvnw -Dtest=ReleaseServiceTest test

# Run with coverage
./mvnw test jacoco:report
```

### Test Types

- **Unit Tests**: Service layer with Mockito
- **Integration Tests**: Full stack with Testcontainers (MySQL)
- **API Tests**: Postman collection available

---

## 🐛 Troubleshooting

### Common Issues

#### 401 Unauthorized
- **Solution**: Login again and get a new token
- Token expires after 120 minutes

#### 403 Forbidden
- **Solution**: Check your role permissions
- Some actions require ADMIN or APPROVER role

#### 409 Conflict
- **Solution**: Check business rules
- Common causes:
  - Trying to execute before scheduled time
  - Trying to execute unapproved release
  - Trying to cancel executed release

#### Database Connection Issues
- **Solution**: Check database is running
- Verify connection settings in `application.yml`
- Check Docker container health: `docker ps`

#### Webhook Not Received
- **Solution**: 
  - Verify `APP_RELEASE_WEBHOOK_URL` is set
  - Check webhook URL is accessible
  - Review application logs for webhook errors

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.example.timelock: DEBUG
```

---

## 🚀 Deployment

### Production Checklist

- [ ] Change default database passwords
- [ ] Generate secure JWT secret
- [ ] Configure proper CORS origins
- [ ] Set up SSL/TLS for database
- [ ] Configure production logging
- [ ] Set up monitoring and alerts
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set up webhook endpoints
- [ ] Configure environment-specific settings

### Docker Production

```bash
# Build production images
docker compose -f docker/docker-compose.yml build

# Run with production environment
docker compose -f docker/docker-compose.yml up -d
```

### Environment-Specific Configuration

Use Spring profiles:

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 📚 Additional Documentation

- [IMPROVEMENTS.md](./IMPROVEMENTS.md) - Detailed list of improvements
- [CONFIGURATION.md](./CONFIGURATION.md) - Complete configuration guide
- [API Documentation](http://localhost:8081/swagger-ui/index.html) - Interactive API docs

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

---

## 📄 License

MIT License - see [LICENSE](./LICENSE) file for details.

---

## 🎯 Roadmap

Future enhancements planned:

- [ ] Email notifications
- [ ] Release templates
- [ ] Advanced scheduling (recurring releases)
- [ ] Release dependencies
- [ ] Rollback functionality
- [ ] Multi-stage approval workflows
- [ ] CI/CD integrations
- [ ] Advanced analytics and metrics
- [ ] User management UI
- [ ] Release comments and notes

---

## 📞 Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Check the documentation
- Review the troubleshooting section

---

## 🙏 Acknowledgments

Built with:
- Spring Boot
- React
- MySQL
- Docker
- And many other open-source libraries

---

**Made with ❤️ for better release management**
