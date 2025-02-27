# Spring Boot Security Demo with Keycloak Integration

## Overview

This is a comprehensive Spring Boot application demonstrating advanced security configuration using Keycloak for authentication and authorization. The project showcases role-based access control, resource management, and integration with an OAuth2 resource server.

## Features

- **Authentication**: Keycloak-based authentication
- **Authorization**: Role-based access control (ADMIN, USER roles)
- **Resource Management**: CRUD operations with fine-grained access control
- **JWT Token Support**: Converts Keycloak JWT tokens to Spring Security authorities
- **User Synchronization**: Periodic synchronization of user information from Keycloak
- **CORS Configuration**: Configured cross-origin resource sharing
- **Public and Protected Endpoints**

## Technology Stack

- Java 17
- Spring Boot 3.4.3
- Keycloak 25.0.3
- H2 Database (In-memory)
- Spring Security
- OAuth2 Resource Server
- Lombok

## Prerequisites

- Java 17
- Maven
- Keycloak Server (local or remote)

## Configuration

### Keycloak Setup

1. Create a realm named `security-demo`
2. Create a client `security-demo-app`
3. Configure client roles: `ADMIN`, `USER`
4. Create users with respective roles

### Application Configuration

Key configuration files:
- `application.yml`: Main application settings
- `pom.xml`: Project dependencies and build configuration

### Important Configuration Parameters

```yaml
keycloak:
  realm: security-demo
  auth-server-url: http://localhost:8180/auth
  resource: security-demo-app
```

## Running the Application

### Start Keycloak

```bash
# Typical Keycloak startup command
docker run -p 8180:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:25.0.3 start-dev
```

### Run Spring Boot Application

```bash
mvn clean spring-boot:run
```

## API Endpoints

### Public Endpoints
- `GET /api/public/resources`: Retrieve public resources
- `GET /api/public/health`: Health check
- `GET /api/public/info`: Application information

### User Endpoints (Requires USER role)
- `GET /api/user/resources`: Get user's resources
- `GET /api/user/resources/all`: Get all resources
- `POST /api/user/resources`: Create a resource
- `PUT /api/user/resources/{id}`: Update a resource
- `DELETE /api/user/resources/{id}`: Delete a resource
- `GET /api/user/profile`: Get user profile

### Admin Endpoints (Requires ADMIN role)
- `GET /api/admin/users/sync`: Synchronize users
- `DELETE /api/admin/users/cache`: Clear user cache
- `GET /api/admin/system/info`: Get system information

## Testing the API

### Obtain Access Token

```bash
# User token (peyman - standard user)
curl -X POST http://localhost:8180/auth/realms/security-demo/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=security-demo-app" \
  -d "client_secret=zEKlFSU7lpuIJdOqSb8N3XtmMJu3ISCC" \
  -d "username=peyman" \
  -d "password=123" \
  -H "Content-Type: application/x-www-form-urlencoded"

# Admin token
curl -X POST http://localhost:8180/auth/realms/security-demo/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=security-demo-app" \
  -d "client_secret=zEKlFSU7lpuIJdOqSb8N3XtmMJu3ISCC" \
  -d "username=admin" \
  -d "password=123" \
  -H "Content-Type: application/x-www-form-urlencoded"
```

### Example API Calls

```bash
# Get public resources (no token needed)
curl http://localhost:8080/api/public/resources

# Get user resources (requires USER token)
curl -H "Authorization: Bearer YOUR_USER_TOKEN" http://localhost:8080/api/user/resources

# Create a resource (requires USER token)
curl -X POST -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_USER_TOKEN" \
     -d '{"name":"My Resource","description":"Test Resource","publicResource":false}' \
     http://localhost:8080/api/user/resources

# Admin system info (requires ADMIN token)
curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" http://localhost:8080/api/admin/system/info
```

## Security Concepts Demonstrated

- JWT Token parsing and role extraction
- Method-level security annotations
- Role-based access control
- Stateless authentication
- CORS configuration
- Resource-level authorization

## Logging

Detailed logging is configured for security-related components. Check application logs for authentication and authorization details.

## Troubleshooting

- Ensure Keycloak is running
- Verify client credentials
- Check application logs
- Validate token generation and roles

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
