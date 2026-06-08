# RabbitMQ Notification System

A distributed microservices architecture demonstrating asynchronous message communication using RabbitMQ. This project showcases user registration event publishing and event consumption across multiple services with proper message serialization and audit logging.

## 📋 Project Overview

This system implements a **publish-subscribe pattern** where:
- **Registration Service** acts as the message producer, accepting user registration requests
- **Email Service** and **SMS Service** (consumers) listen for registration events
- **Audit Service** logs all activities to a PostgreSQL database

The architecture demonstrates real-world microservices communication with RabbitMQ as the message broker.

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Message Broker** | RabbitMQ | 4.3.1 |
| **Framework** | Spring Boot | 4.0.6 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 16 |
| **Build Tool** | Maven | 3.x |
| **Container** | Docker & Docker Compose | - |
| **Message Serialization** | Jackson JSON | - |

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Message Broker (RabbitMQ)                  │
│                     (notification.exchange)                      │
└────────────────────────┬────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌─────────┐   ┌──────────────┐   ┌────────────┐
   │  Email  │   │    SMS       │   │   Audit    │
   │ Service │   │   Service    │   │  Service   │
   │ :8082   │   │   :8083      │   │   :8081    │
   └─────────┘   └──────────────┘   └────────────┘
        ▲                                   ▲
        └───────────────┬────────────────────┘
                        │
                        │ HTTP Requests
                        │
              ┌─────────────────────┐
              │ Registration Service│
              │      :8080          │
              │  (Message Producer) │
              └─────────────────────┘
```

### Services Description

#### 1. **Registration Service** (Port 8080)
- **Role**: Message Producer
- **Responsibility**: Accepts user registration requests via REST API
- **Endpoint**: `POST /register`
- **Publishes**: `UserRegistrationRequest` objects to `notification.exchange`
- **Also publishes**: Audit logs to `audit.exchange`

#### 2. **Email Service** (Port 8082)
- **Role**: Event Consumer
- **Responsibility**: Consumes registration events and processes email notifications
- **Queue**: `email.queue`
- **Exchange**: `notification.exchange` (Fanout)
- **Processes**: Incoming registration events as `Map<String, Object>`

#### 3. **Audit Service** (Port 8081)
- **Role**: Event Consumer & Data Persister
- **Responsibility**: Logs all system events to PostgreSQL database
- **Queue**: `audit.queue`
- **Exchange**: `audit.exchange` (Direct)
- **Database**: PostgreSQL (audit_db)
- **Endpoint**: `GET /api/audit/logs` - Retrieve all audit logs

#### 4. **Message Broker** (RabbitMQ)
- **Container**: rabbitmq:4.3.1-management
- **AMQP Port**: 5672
- **Management UI**: http://localhost:15672
- **Default Credentials**: root/root

#### 5. **Database** (PostgreSQL)
- **Container**: postgres:16
- **Port**: 5432
- **Database**: audit_db
- **Credentials**: root/root

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose installed
- Git (to clone the repository)
- No need to install Java, Maven, or PostgreSQL separately (all containerized)

### Installation & Running

#### Step 1: Clone or Navigate to Project Directory
```bash
cd "D:\ITI\Projects\RabbitMQ Project\Source\NotificationSystem"
```

#### Step 2: Start All Services with Docker Compose
```bash
docker-compose up --build
```

This command will:
- Build Docker images for all three microservices
- Start RabbitMQ container with management console
- Start PostgreSQL database
- Start all microservices and link them via `notification-network`

**Expected Output:**
```
Creating rabbitmq    ... done
Creating postgres    ... done
Creating registration-service ... done
Creating email-service        ... done
Creating audit-service        ... done
```

#### Step 3: Verify All Services Are Running

Check that all containers are healthy:
```bash
docker-compose ps
```

Wait for all services to show `Up` status (usually 20-30 seconds).

### Accessing the Services

| Service | URL | Purpose |
|---------|-----|---------|
| Registration Service | http://localhost:8080 | User registration & REST API |
| **Audit Logs UI** | **http://localhost:8080/logs.html** | **View system audit logs dashboard** |
| Audit Service API | http://localhost:8081/api/audit/logs | REST API for audit logs |
| Email Service | http://localhost:8082 | Email event consumer |
| RabbitMQ Management | http://localhost:15672 | Monitor RabbitMQ (root/root) |

## 📝 How to Test

### 1. Register a User

Send a POST request to the Registration Service:

**Using curl:**
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 28
  }'
```

**Using PowerShell:**
```powershell
$body = @{
    name = "John Doe"
    email = "john.doe@example.com"
    age = 28
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/register" `
  -Method POST `
  -Headers @{"Content-Type" = "application/json"} `
  -Body $body
```

**Expected Response:**
```json
{
  "message": "User registered successfully"
}
```

### 2. Verify Message Processing

**Check Email Service Logs:**
```bash
docker-compose logs email-service
```

You should see:
```
email-service | 2026-06-08T20:41:43.214Z INFO 1 --- [EmailService] Received registration event: {name=John Doe, email=john.doe@example.com, age=28}
```

### 3. View Audit Logs

Open the logs dashboard in your browser:

**URL:**
```
http://localhost:8080/logs.html
```

This page will display a formatted table of all system audit logs with auto-refresh every 5 seconds.

**Alternative - Using REST API:**

You can also fetch logs directly via the Registration Service endpoint:

**Using curl:**
```bash
curl http://localhost:8080/logs
```

**Using PowerShell:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/logs" -Method GET | ConvertFrom-Json
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "message": "User registered: John Doe with email: john.doe@example.com",
    "createdAt": "2026-06-08T20:41:43.214Z"
  }
]
```

### 4. Monitor RabbitMQ

Visit http://localhost:15672 and log in with:
- **Username**: root
- **Password**: root

In the RabbitMQ Management Console, you can:
- View exchanges: `notification.exchange` (Fanout) and `audit.exchange` (Direct)
- Monitor queues: `email.queue`, `sms.queue`, `audit.queue`
- Track message throughput and bindings

## 📸 Screenshots

### Screenshot 1: Registration Service API Response
![Screenshot 1 - API Response]
*Add screenshot here showing successful user registration API response*

### Screenshot 2: Audit Logs Dashboard
![Screenshot 2 - Audit Logs Dashboard]
*Add screenshot here showing the System Activity Logs dashboard at http://localhost:8080/logs.html with the audit log table*

### Screenshot 3: RabbitMQ Management Console
![Screenshot 3 - RabbitMQ Dashboard]
*Add screenshot here showing RabbitMQ dashboard with exchanges, queues, and message delivery metrics*

## 🔄 Message Flow Diagram

```
1. User submits registration form
   ▼
2. Registration Service receives POST /register
   ▼
3. RegistrationProducer publishes UserRegistrationRequest to notification.exchange
   ▼
4. Message is fanout to both:
   ├─► email.queue ──► Email Service consumes event ──► Process email notification
   └─► audit.queue ──► Audit Service consumes event ──► Save to PostgreSQL
   ▼
5. Audit logs are also published to audit.exchange
   ▼
6. Audit Service retrieves logs via REST API (GET /api/audit/logs)
```

## 🛠️ Key Technologies Explained

### RabbitMQ
**Purpose**: Asynchronous message broker  
**Pattern**: Publish-Subscribe (Fanout & Direct exchanges)
- **Notification Exchange** (Fanout): Broadcasts registration events to all bound queues
- **Audit Exchange** (Direct): Routes audit messages with routing keys

**Benefits**:
- Decouples services - services don't need to know about each other
- Reliability - messages are persistent and not lost if consumer is down
- Scalability - easy to add more consumers without changing producers

### Spring Boot
**Purpose**: Java framework for building microservices  
**Used for**:
- RESTful API endpoints
- RabbitMQ integration via Spring AMQP
- Dependency injection and auto-configuration
- Logging and error handling

### Jackson JSON Message Converter
**Purpose**: Serializes/Deserializes objects to/from JSON  
**Key Fix**: Both Email and Audit services now properly convert JSON messages to objects

### PostgreSQL
**Purpose**: Persistent storage for audit logs  
**Benefits**:
- Durable audit trail
- Query historical data
- Transaction support

## 🐛 Troubleshooting

### Issue: Services can't connect to RabbitMQ
**Solution**: Ensure RabbitMQ container is running and healthy:
```bash
docker-compose ps
docker-compose logs rabbitmq
```

### Issue: "Message conversion error"
**Solution**: Make sure message converters are configured in all services (fixed in latest version)

### Issue: Audit logs not appearing
**Solution**: 
1. Check PostgreSQL is running: `docker-compose ps postgres`
2. Verify audit-service logs: `docker-compose logs audit-service`
3. Ensure audit.exchange and audit.queue exist in RabbitMQ

### Issue: Can't access RabbitMQ Management Console
**Solution**: 
1. Wait 30 seconds for RabbitMQ to start
2. Try http://localhost:15672 in browser
3. Check firewall isn't blocking port 15672

## 📌 API Endpoints

### Registration Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register a new user |
| GET | `/logs` | Fetch all audit logs (JSON API) |
| GET | `/logs.html` | View audit logs in web dashboard |

**Registration Endpoint Request Body:**
```json
{
  "name": "string",
  "email": "string",
  "age": "number"
}
```

### Audit Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/audit/logs` | Retrieve all audit logs (internal API) |

## 🔧 Configuration Files

### RabbitMQ Configuration
- **Producer** (Registration Service): `RegistrationService/src/main/java/org/registrationservice/config/RabbitConfig.java`
- **Consumers** (Email & Audit): Respective config files with message converter beans

### Application Properties
Each service has its own `application.properties`:
- Exchange names
- Queue names
- Routing keys
- RabbitMQ connection details

## 📊 Message Format

### UserRegistrationRequest
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 28
}
```

### Audit Log Format (in PostgreSQL)
```json
{
  "id": 1,
  "message": "User registered: John Doe with email: john.doe@example.com",
  "createdAt": "2026-06-08T20:41:43.214Z"
}
```

## 🧹 Cleanup

To stop all services and remove containers:
```bash
docker-compose down
```

To remove volumes as well (delete all data):
```bash
docker-compose down -v
```

## 📚 Learning Resources

- [RabbitMQ Official Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring Boot AMQP Guide](https://spring.io/guides/gs/messaging-rabbitmq/)
- [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
- [Microservices Patterns](https://microservices.io/)

## 👨‍💻 Project Structure

```
NotificationSystem/
├── RegistrationService/          # Producer Service
│   ├── src/main/java/org/registrationservice/
│   │   ├── config/               # RabbitMQ configuration
│   │   ├── controller/           # REST API endpoints
│   │   ├── service/              # Business logic
│   │   └── model/                # Data models
│   ├── pom.xml                   # Maven dependencies
│   └── Dockerfile
├── EmailService/                 # Consumer Service
│   ├── src/main/java/org/emailservice/
│   │   ├── config/               # RabbitMQ configuration
│   │   ├── listener/             # Message listener
│   │   └── service/              # Email processing logic
│   ├── pom.xml
│   └── Dockerfile
├── AuditService/                 # Consumer & Database Service
│   ├── src/main/java/org/auditservice/
│   │   ├── config/               # RabbitMQ & Database configuration
│   │   ├── controller/           # REST API endpoints
│   │   ├── listener/             # Message listener
│   │   ├── model/                # Entity models
│   │   ├── repository/           # Data access layer
│   │   └── service/              # Business logic
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yaml           # Multi-container orchestration
└── README.md                      # This file
```

## 🎯 Key Concepts Demonstrated

1. **Microservices Architecture**: Independent, loosely coupled services
2. **Asynchronous Communication**: Non-blocking, event-driven processing
3. **Message Queue Pattern**: Reliable message delivery and processing
4. **Fanout Exchange**: Broadcasting messages to multiple consumers
5. **Direct Exchange**: Routing messages with routing keys
6. **Persistence**: Audit logging to relational database
7. **Container Orchestration**: Docker Compose for local multi-service setup
8. **Message Serialization**: JSON conversion between producer and consumers

## 📝 Notes

- All services automatically connect to RabbitMQ via the `notification-network` bridge
- Services are configured to restart automatically if they crash
- Audit logs are persisted in PostgreSQL and survive container restarts
- RabbitMQ data persists in the `./data` volume
- Logs are written to `./log` volume for debugging

## ✅ Version Info

- **Project Version**: 1.0.0
- **Spring Boot**: 4.0.6
- **Java**: 21
- **Last Updated**: June 2026

---

**Happy Messaging! 🚀**
