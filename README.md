# Smart Service Booking & Provider Management Platform

A full-stack web application that connects customers with service providers through a simple online booking system.

## Features

### Customer
- Customer registration and login
- Browse available services
- View available service providers
- Book services by selecting a provider, date, and time
- View personal booking history
- Cancel pending bookings

### Administrator
- Secure admin login
- View booking statistics and recent bookings
- View and manage all customer bookings
- Confirm, complete, or cancel bookings

### Security
- Session-based authentication
- Role-based access control
- Password hashing using BCrypt
- CSRF protection
- Booking ownership validation

## Technology Stack

**Frontend**
- HTML5
- CSS3
- JavaScript

**Backend**
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA

**Database**
- MySQL

**Tools**
- VS Code
- Postman
- MySQL Workbench
- Maven

## Project Structure

```text
SmartService/
├── backend/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
├── frontend/
│   ├── admin/
│   ├── css/
│   ├── js/
│   └── pages/
├── docs/
├── .gitignore
└── README.md
```

## Setup and Installation

### Prerequisites
- Java 17 or later
- MySQL
- Git

### 1. Clone the repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

### 2. Create the database

```sql
CREATE DATABASE smartservice_db;
```

### 3. Configure database credentials

Set the following environment variables before starting the backend:

```text
DB_URL=jdbc:mysql://localhost:3306/smartservice_db
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
```

### 4. Run the backend

Open a terminal inside the `backend` folder and run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs at:

`http://localhost:8080`

### 5. Run the frontend

Open the project using VS Code and launch the frontend through Live Server.

Open:

`http://127.0.0.1:5500/frontend/index.html`

## Future Enhancements

- Smart provider recommendations
- Location-based provider matching
- AI-assisted natural language booking
- Suspicious booking detection

## Current Limitations

- Provider registration and provider booking management are not yet implemented.
- User and provider counts are not currently available in the admin dashboard.
