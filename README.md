# Payment Management

[![Built with Claude Code](https://img.shields.io/badge/Built%20with-Claude%20Code-blueviolet?logo=anthropic)](https://claude.ai/code)

API REST para gestionar proveedores y ordenes de pago usando Java 21, Spring Boot 3.x, Spring Web, Spring Data JPA, Validation, H2, Gradle, JUnit 5 y Mockito.

## Arquitectura

El proyecto sigue una arquitectura hexagonal con estos paquetes:

- `domain`: modelos, estados y reglas de negocio puras.
- `application`: casos de uso, comandos, resultados y puertos de salida.
- `infrastructure`: adaptadores REST, persistencia JPA, configuracion y manejo de errores.

La capa de aplicacion depende de puertos, no de repositorios JPA concretos. La infraestructura implementa esos puertos con Spring Data JPA.

## Decisiones tecnicas

- `spring-boot-starter-validation`: validacion declarativa de DTOs de entrada.
- `springdoc-openapi-starter-webmvc-ui`: documentacion OpenAPI/Swagger accesible localmente.
- `mockito-junit-jupiter`: pruebas unitarias explicitas con JUnit 5 y Mockito.
- H2 en memoria con consola en `/h2-console`.
- Concurrencia en ordenes mediante `@Version` y bloqueo optimista de JPA.
- Idempotencia en creacion de ordenes con header `Idempotency-Key`.
- Las ordenes registran `paidAt` al transicionar a `PAGADA`; el reporte de total pagado filtra por esa fecha de pago.

## Regla de vencimiento

Una orden vence 30 dias despues de su `createdAt`. El endpoint de ordenes proximas a vencer retorna ordenes en estado `BORRADOR` o `APROBADA` cuya fecha de vencimiento cae entre el momento actual y los proximos `days` dias. El valor por defecto es `7` y el maximo permitido es `90`.

## Ejecutar

```bash
./gradlew bootRun
```

En Windows:

```powershell
.\gradlew.bat bootRun
```

URLs locales:

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 console: `http://localhost:8080/h2-console`

Credenciales H2:

- JDBC URL: `jdbc:h2:mem:payment_management`
- Usuario: `sa`
- Password: vacio

## Endpoints principales

Proveedores:

- `POST /api/v1/suppliers`
- `GET /api/v1/suppliers?status=ACTIVO&page=0&size=20`
- `GET /api/v1/suppliers/{id}`
- `PUT /api/v1/suppliers/{id}`
- `PATCH /api/v1/suppliers/{id}/status`
- `GET /api/v1/suppliers/{id}/paid-total?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z`

Ordenes de pago:

- `POST /api/v1/payment-orders` con header opcional `Idempotency-Key`
- `GET /api/v1/payment-orders?status=BORRADOR&supplierId={supplierId}&page=0&size=20`
- `GET /api/v1/payment-orders/{id}`
- `PATCH /api/v1/payment-orders/{id}/status`
- `GET /api/v1/payment-orders/due-soon?days=7`

## Transiciones validas

- `BORRADOR -> APROBADA`
- `BORRADOR -> RECHAZADA`
- `APROBADA -> PAGADA`

Cualquier otra transicion retorna `400 Bad Request` con cuerpo JSON descriptivo.

## Errores

El manejo de errores esta centralizado y retorna JSON con:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors`

Codigos usados:

- `400`: validaciones de negocio, DTOs invalidos o parametros invalidos.
- `404`: recursos inexistentes.
- `409`: duplicados, conflictos de integridad o concurrencia optimista.

## Pruebas

```bash
./gradlew test
```

Incluye pruebas unitarias de reglas relevantes y una prueba de integracion del endpoint critico de creacion de ordenes.

## Alcance

Se implementaron los bloques de prioridad alta y los extras solicitados: reporte agregado, ordenes proximas a vencer, idempotencia y manejo de concurrencia.
