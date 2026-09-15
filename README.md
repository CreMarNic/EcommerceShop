# 🛒 EcommerceShop

A full-stack e-commerce application built with **Java, Spring Boot, Spring Security, React, and TypeScript**.

The application provides product browsing, user registration and authentication, shopping cart management, checkout, order history, and role-based administration. The backend exposes REST APIs using Spring Boot and Spring Data JPA, while the React frontend communicates with the API through Axios.

---

## ✨ Key Features

- User registration and authentication
- Product catalog
- Shopping cart management
- Checkout and order creation
- User order history
- Admin product management
- Admin user management
- Admin order management
- Role-based access for users and administrators
- Persistent H2 database
- REST API

---

## 🛠 Tech Stack

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- REST APIs
- Maven

### Frontend

- React
- TypeScript
- Vite
- React Router
- Axios
- HTML / CSS

### Database

- H2 file database

### Security

- Spring Security
- HTTP Basic authentication
- Role-based authorization

## 🔌 Backend API

The backend exposes REST endpoints under `/api`.

Important controllers:

- `UserController`: user registration and admin user management
- `ProductController`: public product catalog and admin product management
- `CartController`: authenticated cart actions
- `OrderController`: checkout, order history, and admin order management
- `AuthController`: returns the currently authenticated user

### User API

`UserController` is mapped with:

```java
@RequestMapping("/api")
```

Available user routes:

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/public/users` | Public | Register a new user |
| `GET` | `/api/admin/users` | Admin | Get all users |
| `GET` | `/api/admin/users/{userId}` | Admin | Get one user |
| `PUT` | `/api/admin/users/{userId}` | Admin | Update a user |
| `DELETE` | `/api/admin/users/{userId}` | Admin | Delete a user |

## 🔗 Frontend & Backend Integration

The frontend uses Axios in `frontend/src/api/ecommerceApi.ts`.

```ts
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? '/api',
    withCredentials: true,
});
```

During development, Vite proxies `/api` requests to the backend:

```ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080'
    }
  }
}
```

So a frontend call like:

```ts
api.post('/public/users', request)
```

is sent to:

```text
http://localhost:8080/api/public/users
```

## 🔐 Authentication & Authorization

The application uses **Spring Security with HTTP Basic authentication** and role-based authorization.

Access is divided into three areas:

- **Public endpoints** — registration and product browsing
- **Authenticated user endpoints** — cart management, checkout, order history, and current-user information
- **Admin endpoints** — user, product, and order administration

Backend routes are protected according to their required access level:

- `/api/public/**` — public access
- `/api/users/**`, `/api/orders/**`, `/api/auth/me` — authenticated users
- `/api/admin/**` — administrators

For this demo application, the frontend stores the Basic Authentication header in browser storage and includes it with protected API requests.

> **Security note:** This authentication approach is intended for demonstration purposes. A production version should use a more robust authentication strategy and avoid storing reusable authentication credentials in browser storage.

### Admin Configuration

Admin credentials can be configured through environment variables:

```text
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=your-password

## 📋 Prerequisites

- Java 25
- Maven
- Node.js and npm

## 🚀 Run Locally

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

Start the frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

## 🧪 Testing

The backend includes automated tests using **JUnit 5** and **Mockito**.

Current test coverage includes:

- **ProductServiceImplUnitTests** — unit tests for product service logic
- **UserServiceImplUnitTests** — unit tests for user service logic
- **BackendApplicationTests** — verifies that the Spring application context loads successfully

Run all backend tests with:

```bash
cd backend
mvn test
```

The frontend can be type-checked and built with:

```bash
cd frontend
npm run build
```

Frontend component and end-to-end tests are not currently included.

## 🗄 Database

The backend uses **Spring Data JPA** for persistence and an **H2 file database** for local data storage.

The main application data includes:

- Users and roles
- Products
- Shopping carts and cart items
- Orders and order items

The database is configured in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/ecommerce;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
```

Products are automatically seeded during application startup by `DataSeeder`.

The H2 console is disabled by default and can be enabled for local development with:

```text
H2_CONSOLE_ENABLED=true
```

## 🛒 Main User Flow

1. Open the frontend at `http://localhost:5173`.
2. Register a new user.
3. Sign in with the registered email and password.
4. Browse products.
5. Add products to the cart.
6. Checkout.
7. View orders.
