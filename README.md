# 🐾 Wakanda Social – A Microblogging Platform

**Wakanda Social** is a sleek microblogging platform inspired by the kingdom of Wakanda. It empowers users to post updates, follow others, like and comment on posts, and engage in private messaging—all in a sleek, secure, and scalable Java backend built with **Spring Boot**.

---

## 🚀 Features

- ✍️ **Post Microblogs** (up to 280 characters)
- 💬 **Comment & Like** on posts
- 🔗 **Follow/Unfollow** users
- 📬 **Direct Messaging** between users
- 📰 **Personalized Feed** based on followed users
- 🔐 **JWT Authentication** and role-based authorization
- 🔎 **Search Functionality** for users and posts
- ⚙️ **Profile Editing**: name, bio, etc.
- ☁️ In-memory database with **H2**
- 📤 Global exception handling and clean REST APIs

---

## 🛠️ Tech Stack

| Layer        | Technology                       |
|--------------|----------------------------------|
| Backend      | Spring Boot, Spring MVC          |
| Security     | Spring Security, JWT             |
| Database     | H2 (In-Memory)                   |
| ORM          | Spring Data JPA, Hibernate       |
| Architecture | RESTful APIs, MVC Design Pattern |

---

## 🧠 Software Design Principles

### 👩‍🏫 Object-Oriented Programming (OOP)
- **Encapsulation**: Private fields with public accessors
- **Inheritance**: Common fields via `BaseEntity`
- **Abstraction**: Interfaces for services and repositories
- **Polymorphism**: Multiple implementations via interfaces

### 🎯 Design Patterns
- Singleton – Service components
- Strategy – JWT-based auth logic
- Observer – Notification system (event hooks)
- DTO – Safe communication with frontend
- Factory – Service logic abstraction
- Builder – For constructing complex DTOs
- Repository – Data access abstraction
- MVC – Layered app structure
- Dependency Injection – Spring IoC

---

## 📂 Endpoints Overview

| Feature               | Endpoint                                 | Method |
|-----------------------|------------------------------------------|--------|
| Register/Login        | `/auth/register`, `/auth/login`          | POST   |
| Create Post           | `/posts/create`                          | POST   |
| View Posts            | `/posts/all`, `/posts/user/{id}`         | GET    |
| Like Post             | `/posts/{id}/like`                       | POST   |
| Comment on Post       | `/comments/create`                       | POST   |
| Follow/Unfollow       | `/users/{id}/follow`, `/unfollow`        | POST   |
| Send Message          | `/messages/send`                         | POST   |
| View Conversations    | `/messages/conversations`                | GET    |
| Search Users/Posts    | `/users/search`, `/posts/search`         | GET    |
| Update Profile        | `/users/updateProfile`                   | PUT    |
| User Feed             | `/posts/feed`                            | GET    |

---

## 📐 Architecture

- **Controllers**: Handle requests & responses
- **Services**: Business logic and transaction handling
- **Repositories**: CRUD operations via JPA
- **DTOs**: Abstract data exchange models
- **Security**: JWT filter, token provider, and access config
- **Exception Handling**: Centralized via `GlobalExceptionHandler`

---
## 🧪 Running the App

1. Clone the repository:
   ```bash
   git clone https://github.com/geeky-rish/wakanda-socia.git
   cd wakanda-social


