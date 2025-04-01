#  Backend Overview — FocusTrack 🎯

The **FocusTrack** backend is a robust, scalable, and secure Spring Boot application that allows users to plan goals, manage tasks, collaborate with contacts, and stay accountable.

This backend was designed with industry-level practices and includes advanced features like user authentication, goal-sharing, commenting, and password reset via email. It’s structured to be clean, modular, and testable.

---

## 🔧 Technologies & Tools

- **Java 17** & **Spring Boot** – main framework for backend REST API
- **Spring Security** & **JWT** – for stateless authentication and endpoint protection
- **PostgreSQL** – as the main relational database
- **JPA (Hibernate)** – for ORM and database interaction
- **Swagger/OpenAPI** – for live API documentation and testing
- **JUnit & Mockito** – for unit and service-level testing
- **EmailService (JavaMailSender)** – for password reset functionality

---

## 🧩 Key Functionalities

###  User Management
- User registration & secure login (BCrypt + JWT)
- Update profile, delete account, view contacts
- Send and respond to contact requests

###  Goal Management
- Create main goals with attributes like:
  - Description, priority, progress, due/start dates, and order
- Add sub-goals ("steps") to break goals into manageable tasks
- Mark goals as done/undone, edit or delete them

###  Goal Sharing
- Share main goals with existing contacts
- Contacts can:
  - View shared goals & their sub-steps
  - Post and view comments on shared goals
- The goal owner can view which users the goal is shared with
- All users with access can comment

###  Commenting System
- Add, edit, delete comments on own goals or shared goals
- Goal owners can moderate all comments

###  Password Reset Flow
- Request password reset by email
- Secure reset link generated via token with expiration time
- Reset password without requiring login

###  API Docs
- Full API documentation available via Swagger at `/swagger-ui/index.html`

---

##  Testing

- Unit tests for service-layer logic (UserService tested with Mockito)
- Password reset, registration, and login fully tested
- Code follows TDD principles with modular service classes

---

##  Developer Notes

- Built following RESTful principles and layered architecture (Controller → Service → Repository)
- Clean separation of concerns to ensure easy maintenance and future scaling
- Security best practices implemented via Spring Security with JWT


