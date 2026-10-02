# EVE Healthcare Backend

A small Spring Boot backend for diagnostic test bookings and simulated payments.

The implementation covers the assignment requirements:

- User signup/login with JWT authentication
- Diagnostic centres and tests
- Authenticated test bookings
- Simulated payment processing
- Idempotent payment webhooks
- Booking ownership checks
- Request validation and useful error responses
- PostgreSQL persistence
- OpenAPI/Swagger UI
- Unit tests
- Docker and docker-compose

## Tech stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- PostgreSQL
- JJWT
- Springdoc OpenAPI
- JUnit / Mockito
- Testcontainers (for integration-test support)

## Run locally

### Option 1: Docker Compose

```bash
docker compose up --build
```

The API starts at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Option 2: Run PostgreSQL yourself

Create a database called `eve_healthcare`, then run:

```bash
mvn spring-boot:run
```

Maven 3.9+ is required for the local Maven commands.

Environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/eve_healthcare
DB_USERNAME=eve
DB_PASSWORD=eve
JWT_SECRET=your-long-secret
JWT_EXPIRATION_MS=86400000
```

## API overview

### Authentication

`POST /auth/signup`

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "password123"
}
```

`POST /auth/login`

```json
{
  "email": "rahul@example.com",
  "password": "password123"
}
```

Both return a JWT. Send it on protected endpoints:

```text
Authorization: Bearer <token>
```

### Centres and tests

`GET /centres`

`GET /centres/{id}`

`POST /centres` (authenticated)

```json
{
  "name": "EVE Diagnostics - Noida",
  "location": "Sector 62, Noida",
  "tests": [
    {
      "name": "Complete Blood Count",
      "description": "Basic blood count test",
      "price": 450.00
    },
    {
      "name": "HbA1c",
      "description": "Average blood glucose test",
      "price": 600.00
    }
  ]
}
```

`GET /centres/{centreId}/tests`

`POST /centres/{centreId}/tests` (authenticated)

### Bookings

`POST /bookings` (authenticated)

```json
{
  "testId": 1,
  "centreId": 1,
  "appointmentAt": "2026-10-10T10:30:00Z"
}
```

A new booking starts as `PENDING`. The amount is copied from the selected test at booking time.

`GET /bookings` returns the current user's bookings.

`GET /bookings/{id}` only allows the booking owner to view it.

`POST /bookings/{id}/cancel` cancels an owned booking unless it has already been completed by a successful payment.

### Simulated payments

`POST /payments`

```json
{
  "bookingId": 1
}
```

The mock service randomly returns `SUCCESS` or `FAILED` and updates the booking to `CONFIRMED` or `FAILED`.

`POST /payments/webhook`

```json
{
  "eventId": "evt_10001",
  "paymentId": "pay_10001",
  "bookingId": 1,
  "status": "SUCCESS"
}
```

The `eventId` has a unique database constraint. Replaying the same webhook is treated as a no-op, which keeps payment and booking state from being duplicated.

## Database design

- `users`: application users with a unique email and BCrypt password hash.
- `diagnostic_centres`: centre name and location.
- `diagnostic_tests`: tests offered by a centre, including current price.
- `bookings`: user, test, centre, appointment time, amount and booking status.
- `payments`: one payment record per payment attempt, with provider/event identifiers.

Booking amount is stored separately from the test price so a historical booking does not change if the centre changes its current test price later.

## Important assumptions

1. Appointment slots are represented by an appointment timestamp; the assignment does not define a slot inventory model.
2. The mock payment endpoint simulates the provider rather than calling an external gateway.
3. A webhook is trusted after basic payload validation. In a real provider integration, signature verification would be added.
4. A successful payment moves a pending booking to `CONFIRMED`; a failed payment moves it to `FAILED`.
5. Users can only access their own bookings.
6. Centre/test management is authenticated, but this small assignment does not introduce an admin role.

## Edge cases handled

- Duplicate signup email
- Invalid request bodies
- Missing/invalid JWT
- Missing centre, test or booking IDs
- Test must belong to the selected centre
- Booking ownership violations
- Invalid booking state transitions
- Duplicate webhook event IDs
- Webhook/payment references to missing bookings
- Duplicate payment attempts for the same booking while a payment is already successful

## Testing

Run:

```bash
mvn test
```

The test suite includes service-level checks for booking validation and payment/webhook idempotency, plus controller/security coverage where practical.

## What I would improve with more time

- Add Flyway migrations instead of relying on Hibernate `ddl-auto`.
- Add proper centre/admin roles and permissions.
- Add appointment slot availability and conflict detection.
- Add provider webhook signature verification.
- Add Redis for frequently-read centre/test data.
- Move payment simulation to a background job and add retry/dead-letter handling.
- Add structured JSON logging and request correlation IDs.
- Add pagination and filtering to centre and booking endpoints.
