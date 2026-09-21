# Reactive Product Service

Microservicio CRUD reactivo para gestión de productos, desarrollado como solución al **Challenge Técnico: Backend Java Developer**.

## Stack

- Java 21
- Spring Boot 3.5.16
- Spring WebFlux
- Spring Data R2DBC
- H2 con driver R2DBC
- Bean Validation
- JUnit 5, Mockito, Reactor Test y WebTestClient
- JaCoCo
- Docker y Docker Compose

## Decisiones de diseño

La solución mantiene un dominio acotado y evita sobreingeniería. Se separan responsabilidades por capas:

```text
HTTP
  -> ProductController
      -> ProductService
          -> ProductRepository (ReactiveCrudRepository)
              -> H2 / R2DBC
```

Componentes adicionales:

- `ProductMapper`: transformación entre entidad y Records de API.
- `ProductBusinessValidator`: reglas de negocio declaradas con lambdas, `Stream` y `Optional`.
- `GlobalExceptionHandler`: contrato de errores uniforme con `@RestControllerAdvice`.
- `schema.sql`: creación automática de la tabla y constraints.

No se usa `block()`, JDBC ni APIs bloqueantes en el flujo de negocio.

## Reglas de negocio incluidas

- SKU único.
- SKU compuesto únicamente por letras, números y guiones.
- Precio mayor o igual a `0.01` y con máximo 2 decimales.
- Stock entre `0` y `1,000,000`.
- La base de datos también protege unicidad, precio positivo y stock no negativo.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/products` | Crear producto |
| GET | `/api/v1/products` | Listar productos (`Flux`) |
| GET | `/api/v1/products/{id}` | Obtener producto (`Mono`) |
| PUT | `/api/v1/products/{id}` | Actualizar producto |
| DELETE | `/api/v1/products/{id}` | Eliminar producto |

## Ejecutar con Docker Compose

Requisito: Docker con Docker Compose.

```bash
docker compose up --build
```

La API queda disponible en:

```text
http://localhost:8080/api/v1/products
```

Para detenerla:

```bash
docker compose down
```

## Ejecutar localmente

Requisitos:

- JDK 21
- Maven 3.9+

```bash
mvn spring-boot:run
```

## Tests y cobertura

Ejecutar todos los tests:

```bash
mvn test
```

Ejecutar tests, generar reporte JaCoCo y validar el mínimo de cobertura:

```bash
mvn clean verify
```

Reporte HTML:

```text
target/site/jacoco/index.html
```

El `pom.xml` exige **mínimo 70% de cobertura de líneas en el package `service`**. Si no se alcanza, `mvn verify` falla.

### Qué se prueba

- `ProductServiceImplTest`: creación, duplicados, validación funcional, búsqueda, actualización y eliminación usando Mockito + StepVerifier.
- `ProductControllerTest`: endpoints con WebTestClient, HTTP status, validación de request y manejo estándar de errores.

## Ejemplos cURL

### Crear

```bash
curl --request POST 'http://localhost:8080/api/v1/products' \
  --header 'Content-Type: application/json' \
  --data '{
    "sku": "SKU-001",
    "name": "Mechanical Keyboard",
    "description": "75% keyboard",
    "price": 149.90,
    "stock": 20
  }'
```

### Listar

```bash
curl 'http://localhost:8080/api/v1/products'
```

### Buscar por id

```bash
curl 'http://localhost:8080/api/v1/products/1'
```

### Actualizar

```bash
curl --request PUT 'http://localhost:8080/api/v1/products/1' \
  --header 'Content-Type: application/json' \
  --data '{
    "sku": "SKU-001",
    "name": "Mechanical Keyboard Pro",
    "description": "Updated product",
    "price": 179.90,
    "stock": 12
  }'
```

### Eliminar

```bash
curl --request DELETE 'http://localhost:8080/api/v1/products/1'
```

## Ejemplo de error estándar

```json
{
  "timestamp": "2026-09-20T06:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 99 was not found",
  "path": "/api/v1/products/99",
  "details": []
}
```

## Colección Postman

Se incluye:

```text
postman/Product-Service.postman_collection.json
```

## Estructura principal

```text
reactive-product-service
├── src
│   ├── main
│   │   ├── java/com/challenge/productservice
│   │   │   ├── controller
│   │   │   ├── domain
│   │   │   ├── dto
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   ├── service
│   │   │   │   └── impl
│   │   │   └── validation
│   │   └── resources
│   │       ├── application.yml
│   │       └── schema.sql
│   └── test
│       └── java/com/challenge/productservice
│           ├── controller
│           └── service
├── postman
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```
