# EcommerceShop

EcommerceShop is a full-stack ecommerce demo application with a React frontend and a Spring Boot backend. Users can browse seeded products, register, sign in, manage a cart, checkout, and view orders.

## Tech Stack

- Frontend: React, Vite, TypeScript, Axios, React Router
- Backend: Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- Database: H2 file database
- Build tools: npm, Maven

## Project Structure

```text
EcommerceShop/
  backend/    Spring Boot REST API
  frontend/   React/Vite web app
  data/       Local H2 database files
```

## Backend

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

## Frontend and Backend Connection

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

## Authentication

The app uses HTTP Basic authentication.

- Public routes are available under `/api/public/**`.
- Authenticated user routes are under `/api/users/**`, `/api/orders/**`, and `/api/auth/me`.
- Admin routes are under `/api/admin/**`.
- The frontend stores the Basic Auth header in browser storage and sends it with protected API requests.

Admin credentials can be configured with environment variables:

```text
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=your-password
```

If `ADMIN_PASSWORD` is set, the backend seeds or updates an admin user on startup.

## Prerequisites

- Java 25
- Maven
- Node.js and npm

## Run Locally

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

## Useful Commands

Run backend tests:

```bash
cd backend
mvn test
```

Build the frontend:

```bash
cd frontend
npm run build
```

## Database

The backend uses an H2 file database configured in `backend/src/main/resources/application.properties`:

```text
spring.datasource.url=jdbc:h2:file:./data/ecommerce;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
```

Products are seeded automatically on startup by `DataSeeder`.

The H2 console is disabled by default. It can be enabled with:

```text
H2_CONSOLE_ENABLED=true
```

## Main User Flow

1. Open the frontend at `http://localhost:5173`.
2. Register a new user.
3. Sign in with the registered email and password.
4. Browse products.
5. Add products to the cart.
6. Checkout.
7. View orders.
