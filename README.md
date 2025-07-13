# 🧾 Order System - Backend API

Sistema de gestión de pedidos construido con **Spring Boot 3.5.3**, autenticación con JWT y persistencia con **MySQL**. Soporta funcionalidades de administración de usuarios, autenticación, productos, categorías y órdenes.

---

## 📦 Tecnologías

- Java 21
- Spring Boot (Web, Security, JPA, Validation, WebSocket, WebFlux)
- MySQL
- JWT (JSON Web Tokens)
- Lombok
- MapStruct
- Cloudinary (para imágenes)
- Springdoc OpenAPI 3 (Swagger UI)
- Maven

---

## 🚀 Instalación

### 🔧 Requisitos previos

- Java 21
- Maven 3.9+
- MySQL
- Git

### 📥 Clonar el repositorio

    ```bash
    git clone https://github.com/francode-taype/order-system.git
    cd order-system

⚙️ Configuración del entorno

Crea un archivo .env o configura las siguientes variables en application.properties:

    DB_URL=jdbc:mysql://TU_HOST:PUERTO/NOMBRE_DE_BASE_DE_DATOS
    DB_USER_NAME=TU_USUARIO
    DB_PASSWORD=TU_CONTRASEÑA
    
    JWT_SECRET=TU_SECRETO_JWT
    JWT_EXPIRATION_MS=86400000
    JWT_REFRESH_EXPIRATION_MS=2000000000
    
    CLOUDINARY_CLOUD_NAME=TU_CLOUD_NAME
    CLOUDINARY_API_KEY=TU_API_KEY
    CLOUDINARY_API_SECRET=TU_API_SECRET



▶️ Ejecución local

    mvn clean install
    mvn spring-boot:run

  La API estará disponible en:
  📍 http://localhost:8080
  
  La documentación Swagger UI estará disponible en:
    📍 http://localhost:8080/swagger-ui/index.html

    
🔐 Autenticación

    JWT basado en roles (ADMIN, CLIENTE)

    Endpoints protegidos con @PreAuthorize

### 🔐 Endpoints de Autenticación (`/api/v1/auth`)

| Método | Endpoint              | Descripción                              | Rol     |
|--------|-----------------------|------------------------------------------|---------|
| POST   | `/register`           | Registro de usuario (admin)              | ADMIN   |
| POST   | `/register-customer`  | Registro de cliente                      | Público |
| POST   | `/login`              | Autenticación (devuelve token JWT)       | Público |
| POST   | `/refresh`            | Refrescar token JWT                      | Público |

---

### 🗂️ Endpoints de Categorías (`/api/v1/categories`)

| Método | Endpoint                 | Descripción                                | Rol   |
|--------|--------------------------|--------------------------------------------|-------|
| POST   | `/api/v1/categories`      | Crear categoría                            | ADMIN |
| PUT    | `/api/v1/categories/{id}` | Actualizar categoría                       | ADMIN |
| DELETE | `/api/v1/categories/{id}` | Eliminar categoría                         | ADMIN |
| GET    | `/api/v1/categories/{id}` | Obtener categoría por ID                   | Público |
| GET    | `/api/v1/categories`      | Listar categorías (paginado)               | Público |

---

### 🛍️ Endpoints de Productos (`/api/v1/products`)

| Método | Endpoint                       | Descripción                                | Rol   |
|--------|--------------------------------|--------------------------------------------|-------|
| POST   | `/api/v1/products`             | Crear producto (multipart/form-data)       | ADMIN |
| PUT    | `/api/v1/products/{id}`        | Actualizar producto (multipart/form-data)  | ADMIN |
| DELETE | `/api/v1/products/{id}`        | Eliminar producto                          | ADMIN |
| GET    | `/api/v1/products/{id}`        | Obtener producto por ID                    | Público |
| GET    | `/api/v1/products`             | Listar productos con filtros y paginación  | Público |

---

### 📦 Endpoints de Órdenes (`/api/v1/orders`)

| Método | Endpoint                             | Descripción                                                    | Rol             |
|--------|--------------------------------------|----------------------------------------------------------------|-----------------|
| POST   | `/api/v1/orders`                     | Crear orden (en cuerpo JSON)                                   | CLIENTE         |
| PUT    | `/api/v1/orders/{id}`                | Actualizar orden existente                                    | CLIENTE / ADMIN |
| PUT    | `/api/v1/orders/{id}/cancel`         | Cancelar orden                                                | CLIENTE / ADMIN |
| GET    | `/api/v1/orders/customer`            | Listar órdenes del cliente (filtros, paginado)               | CLIENTE         |
| GET    | `/api/v1/orders/admin`               | Listar todas las órdenes con filtros                         | ADMIN           |
| PUT    | `/api/v1/orders/{id}/confirm`        | Marcar orden como confirmada                                  | ADMIN           |
| PUT    | `/api/v1/orders/{id}/send`           | Marcar orden como enviada                                     | ADMIN           |
| PUT    | `/api/v1/orders/{id}/deliver`        | Marcar orden como entregada                                   | ADMIN           |
| GET    | `/api/v1/orders/{id}`                | Obtener orden por ID                                          | CLIENTE / ADMIN |



📸 Cloudinary (Carga de imágenes)

Productos soportan carga de imágenes mediante multipart/form-data. La integración se realiza con Cloudinary SDK para Java (cloudinary-http5).


📘 Documentación OpenAPI

Integrado con Springdoc (Swagger):

http://localhost:8080/swagger-ui.html
