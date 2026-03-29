<!-- ```markdown -->
#  ProjectMatrix

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A **full-stack Project Management System** built with modern technologies to help teams collaborate efficiently. Features secure authentication, role-based access control, and real-time task tracking.

![ProjectMatrix Banner](assets/banner.png) <!-- Add a banner image if available -->

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Troubleshooting](#-troubleshooting)
- [Future Enhancements](#-future-enhancements)
- [License](#-license)

---

## 🎯 Overview

ProjectMatrix is a comprehensive project management solution designed for teams of all sizes. It provides a robust backend API with a clean, responsive frontend interface.

### Key Capabilities
- 👥 **Team Collaboration**: Multi-user environment with role-based permissions
- 📊 **Project Tracking**: Visual task boards (Kanban-style) with status updates
- 🔐 **Enterprise Security**: JWT-based authentication with encrypted passwords
- 📱 **Responsive Design**: Works on desktop, tablet, and mobile devices

---

## ✨ Features

### 🔐 Authentication & Security
- **JWT Authentication**: Stateless, secure token-based authentication
- **Password Encryption**: BCrypt hashing for user credentials
- **Role-Based Access Control (RBAC)**:
    - **ADMIN**: Full system access, user management
    - **MANAGER**: Project and task management
    - **USER**: Task execution and updates

### 📁 Project Management
- Create, read, update, and delete projects
- Assign multiple users to projects
- Set project timelines (start/end dates)
- Track project status (Active, Completed, On Hold)

### ✅ Task Management
- **Kanban Board**: Visual task organization (TODO → IN_PROGRESS → DONE)
- **Task Assignment**: Assign tasks to specific team members
- **Priority Levels**: LOW, MEDIUM, HIGH priority settings
- **Due Dates**: Deadline tracking with visual indicators
- **File Attachments**: Upload documents to tasks

### 💬 Collaboration Tools
- **Comments**: Threaded discussions on tasks
- **Activity Logs**: Track all changes and updates

### 📊 Reporting & Analytics
- User workload overview
- Project progress tracking
- Activity history

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Core language |
| Spring Boot | 3.2.x | Application framework |
| Spring Security | 6.2.x | Authentication & authorization |
| Spring Data JPA | 3.2.x | Database access |
| Hibernate | 6.3.x | ORM framework |
| MySQL | 8.0 | Relational database |
| JWT (JJWT) | 0.11.5 | Token-based security |
| Lombok | 1.18.x | Boilerplate code reduction |
| Swagger | 2.3.x | API documentation |

### Frontend
| Technology | Purpose |
|------------|---------|
| HTML5 | Semantic markup |
| CSS3 | Styling & animations |
| Bootstrap 5 | Responsive UI framework |
| Vanilla JavaScript | Dynamic functionality |
| Fetch API | HTTP requests |

---

## 🏗️ Architecture

### System Architecture
```
┌─────────────────┐      ┌──────────────────┐      ┌──────────────┐
│   Frontend      │      │   Backend        │      │   Database   │
│   (HTML/JS)     │◄────►│   (Spring Boot)  │◄────►│   (MySQL)    │
└─────────────────┘      └──────────────────┘      └──────────────┘
        │                         │
        └───────────┬─────────────┘
                    │
            ┌───────▼────────┐
            │  JWT Filter    │
            │  Security      │
            └────────────────┘
```

### Backend Layer Structure
```
Controller Layer (REST API)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (Data Access)
        ↓
Database (MySQL)
```

### Project Structure
```
projectmatrix-backend/
├── 📂 src/main/java/com/projectmatrix/
│   ├── 📂 controller/          # REST API endpoints
│   ├── 📂 service/             # Business logic
│   ├── 📂 repository/          # Data access layer
│   ├── 📂 entity/              # JPA entities
│   ├── 📂 dto/                 # Data Transfer Objects
│   ├── 📂 security/            # JWT & Security config
│   ├── 📂 config/              # App configurations
│   ├── 📂 exception/           # Global exception handling
│   ├── 📂 util/                # Utility classes
│   └── 📂 initializer/         # Default data setup
└── 📄 application.properties   # Configuration

projectmatrix-frontend/
├── 📄 index.html               # Login page
├── 📄 dashboard.html           # Main dashboard
├── 📄 projects.html            # Project management
├── 📄 tasks.html               # Task board
├── 📄 admin.html               # Admin panel
├── 📂 css/
│   └── styles.css              # Custom styles
└── 📂 js/
    ├── api.js                  # API communication
    ├── auth.js                 # Authentication logic
    ├── project.js              # Project functions
    ├── task.js                 # Task functions
    └── utils.js                # Utility functions
```

---

## 🚀 Getting Started

### Prerequisites
- ☕ Java 17 or higher
- 🐬 MySQL 8.0
- 🛠️ Maven 3.8+
- 🌐 Modern web browser

### 1️⃣ Database Setup

```sql
-- Create database
CREATE DATABASE projectmatrix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


```

### 2️⃣ Backend Configuration

Clone and configure:
```bash
git clone https://github.com/your-username/projectmatrix.git
cd projectmatrix/projectmatrix-backend
```

Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/projectmatrix?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=YourSecretKeyHereMinimum256BitsLongForHS256Algorithm
jwt.expiration=86400000

# File Upload
file.upload-dir=uploads/
```

Run the application:
```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

The backend will start at `http://localhost:8080`

### 3️⃣ Frontend Setup

Navigate to frontend:
```bash
cd ../projectmatrix-frontend
```

Option 1: Open directly
```bash
# Simply open index.html in your browser
# Or use Python's simple server
python -m http.server 5500
```

Option 2: Use Live Server (VS Code)
```bash
# Install Live Server extension
# Right-click on index.html → "Open with Live Server"
```

Access the application at `http://localhost:5500`

### 4️⃣ Default Login

The application creates a default admin user on startup:

| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@pm.com | admin123 |

---

## 📡 API Documentation

### Authentication Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | User registration |

### Project Endpoints
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/projects` | All Users | List all projects |
| POST | `/projects` | ADMIN, MANAGER | Create project |
| PUT | `/projects/{id}` | ADMIN, MANAGER | Update project |
| DELETE | `/projects/{id}` | ADMIN, MANAGER | Delete project |
| GET | `/projects/{id}` | All Users | Get project details |

### Task Endpoints
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/tasks` | All Users | List user tasks |
| POST | `/tasks` | ADMIN, MANAGER | Create task |
| PUT | `/tasks/{id}` | All Users | Update task |
| PUT | `/tasks/{id}/status` | All Users | Update task status |
| DELETE | `/tasks/{id}` | ADMIN, MANAGER | Delete task |

### Full API Docs
Access Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🗄️ Database Schema

### Entity Relationships
```
User (1) ───────< (N) Project (via project_users)
Project (1) ────< (N) Task
Task (1) ───────< (N) Comment
Task (1) ───────< (N) FileAttachment
User (1) ───────< (N) ActivityLog
```

### Key Tables
- **users**: Store user information and roles
- **projects**: Project details and metadata
- **project_users**: Many-to-many relationship table
- **tasks**: Task details with status and priority
- **comments**: Task comments
- **activity_logs**: System audit trail


---

## 🔧 Troubleshooting

### Common Issues

**1. Database Connection Error**
```
Error: Could not create connection to database server
```
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.properties`
- Ensure database `projectmatrix` exists

**2. JWT Secret Key Error**
```
io.jsonwebtoken.security.WeakKeyException: The verification key's size is less than 256 bits
```
- Ensure `jwt.secret` is at least 32 characters long

**3. CORS Errors in Browser**
```
Access to fetch at 'http://localhost:8080/...' from origin 'null' has been blocked
```
- Use a local server (Live Server, Python HTTP server) instead of opening files directly
- Check CORS configuration in `SecurityConfig.java`

**4. Port Already in Use**
```
Port 8080 was already in use
```
```bash
# Find and kill process
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux:
lsof -ti:8080 | xargs kill -9
```

---

## 🗺️ Future Enhancements

- [ ] Email notifications for task assignments
- [ ] Real-time updates with WebSockets
- [ ] Docker containerization
- [ ] React/Vue frontend migration
- [ ] Mobile app (React Native/Flutter)
- [ ] Advanced reporting with charts
- [ ] Calendar integration
- [ ] File storage with AWS S3

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---


<p align="center">
  <b>⭐ Star this repo if you found it helpful! ⭐</b>
</p>
