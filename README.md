

# 🧑‍💼 Job Application Tracking System (JATS)

A **production-ready Job Application Tracking System** built with **Spring Boot**, featuring **JWT authentication**, **role-based access control**, and a **strict application status workflow**.

This project demonstrates real-world backend engineering practices suitable for **SDE backend roles**.

---

## 🚀 Features

* **User Roles**

  * Applicant
  * Recruiter
  * Admin

* **Authentication & Security**

  * JWT-based authentication
  * Role-based access control (RBAC)
  * Secure API endpoints using Spring Security

* **Job Management**

  * Create, update, delete jobs (Recruiter)
  * Search and list jobs (Applicants)
  * Pagination & filtering support

* **Application Tracking**

  * Apply for jobs
  * Track application status
  * Recruiter-controlled status updates

* **Status Workflow Enforcement**

  * Prevents invalid state transitions

* **RESTful API Design**

  * Clean, structured endpoints
  * Global exception handling

* **Database Integration**

  * MySQL with JPA/Hibernate
  * Optimized entity relationships

---

## 📊 Application Status Workflow

The system enforces the following valid transitions:

```
APPLIED
   ↓
SHORTLISTED
   ↓
INTERVIEW_SCHEDULED
   ↓
INTERVIEW_COMPLETED
   ↓
OFFERED / REJECTED
```

Invalid transitions are automatically blocked.

---

## 🛠 Tech Stack

* **Java 17**
* **Spring Boot 3.2.0**
* **Spring Security**
* **JWT (JSON Web Tokens)**
* **MySQL 8**
* **JPA / Hibernate**
* **Lombok**
* **Maven**

---

## 🏗 Project Structure

```
jats/
├── src/main/java/com/jats/
│   ├── JobApplicationTrackingSystemApplication.java
│   ├── entity/        # Database entities
│   ├── repository/    # JPA repositories
│   ├── service/       # Business logic
│   ├── controller/    # REST controllers
│   ├── security/      # JWT & security config
│   ├── dto/           # Request/response DTOs
│   └── exception/     # Global exception handling
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## ⚙️ Setup Instructions

### Prerequisites

* Java **17+**
* MySQL **8.0+**
* Maven **3.6+**

---

### Database Setup

Create the database:

```sql
CREATE DATABASE jats_db;
```

Update `application.properties` with your credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jats_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### Run the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Application starts at:

```
http://localhost:8080
```

---

## 📚 API Endpoints

### 🔐 Authentication

| Method | Endpoint             | Description                        |
| ------ | -------------------- | ---------------------------------- |
| POST   | `/api/auth/register` | Register as Applicant or Recruiter |
| POST   | `/api/auth/login`    | Login & receive JWT token          |

---

### 💼 Jobs

| Method | Endpoint                    | Access             |
| ------ | --------------------------- | ------------------ |
| GET    | `/api/jobs`                 | Public (Paginated) |
| POST   | `/api/jobs`                 | Recruiter only     |
| GET    | `/api/jobs/search?keyword=` | Public             |

---

### 📄 Applications

| Method | Endpoint                            | Access    |
| ------ | ----------------------------------- | --------- |
| POST   | `/api/applications`                 | Applicant |
| GET    | `/api/applications/my-applications` | Applicant |
| PUT    | `/api/applications/{id}/status`     | Recruiter |

---

## 🔒 Security Highlights

* JWT stored and validated on each request
* Role-based endpoint protection
* Stateless authentication
* Passwords encrypted using BCrypt

---

## 🎯 Why This Project Matters

* Real-world **backend system design**
* Strong **Spring Security + JWT** implementation
* Clean **REST API architecture**
* Excellent **resume & placement project**
* Easily extensible (notifications, admin analytics, email service)

---

## 📌 Future Enhancements

* Swagger / OpenAPI documentation
* Email notifications
* Admin analytics dashboard
* Docker support
* CI/CD integration

---

## ❤️ Built With

Built with **Spring Boot** and backend best practices.

