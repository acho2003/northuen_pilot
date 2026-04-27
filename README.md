# Northuen

Northuen is a Bhutan-focused delivery marketplace MVP for food delivery, shop delivery, and parcel delivery. The payment model is Cash on Delivery only: customers pay after delivery, drivers collect cash, and admins track collected and pending COD.

## Project Structure

- `northuen-backend/` - Java 17 Spring Boot REST API with JWT auth, BCrypt, role authorization, JPA repositories, Swagger, and COD business rules.
- `northuen_app/` - Flutter mobile app with role-based UI for Customer, Driver, Vendor, and Admin.
- `database/schema.sql` - Supabase PostgreSQL schema.
- `database/ERD.md` - Mermaid ERD.
- `northuen-backend/src/main/resources/db/migration/` - Flyway migrations.
- `docker-compose.yml` - Local Docker stack for backend and PostgreSQL.

## COD Business Rule

Orders start with:

- `payment_type = COD`
- `payment_status = PENDING`
- `payments.method = CASH`
- `payments.status = PENDING`

The order is complete only when the driver taps `Mark Delivered & Payment Collected`. The backend then updates the delivery, order, and payment in one transaction:

- `orders.status = DELIVERED`
- `orders.payment_status = PAID`
- `payments.status = PAID`
- `payments.collected_by_driver_id = driver.id`

No frontend screen writes directly to Supabase. All critical actions go through the Spring Boot API.

## Database Setup

1. Create a Supabase project.
2. Open Supabase SQL Editor.
3. Run `database/schema.sql`.
4. Use Supabase Storage for uploaded vendor/product images. The MVP stores image URLs in PostgreSQL and leaves upload signing to the backend as the next production step.

## Backend Setup

Requirements:

- Java 17
- Supabase PostgreSQL connection string

Create `northuen-backend/.env` from `northuen-backend/.env.example`, or export the variables in your shell.

PowerShell example:

```powershell
cd .\northuen-backend
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
$env:DATABASE_URL='jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres'
$env:DATABASE_USERNAME='postgres.your-project-ref'
$env:DATABASE_PASSWORD='your-password'
$env:PORT='8081'
$env:JWT_SECRET='replace-with-at-least-32-byte-secret-for-production'
$env:ADMIN_EMAIL='admin@northuen.bt'
$env:ADMIN_PASSWORD='change-this-admin-password'
.\gradlew.bat bootRun
```

Swagger is available at:

```text
http://localhost:8081/swagger-ui.html
```

Key endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/vendors`
- `GET /api/vendors?q=Kitchen&category=FOOD`
- `GET /api/vendors/{id}/products`
- `GET /api/products/search?q=momo`
- `GET /api/marketplace/stats`
- `GET /api/cart`
- `POST /api/cart/items`
- `PATCH /api/cart/items/{productId}`
- `DELETE /api/cart/items/{productId}`
- `POST /api/orders`
- `GET /api/orders/my`
- `PATCH /api/orders/{id}/status`
- `GET /api/orders/{id}/tracking`
- `POST /api/orders/{id}/review`
- `GET /api/vendor/orders`
- `GET /api/drivers/assigned-orders`
- `GET /api/drivers/available-orders`
- `PATCH /api/drivers/available-orders/{deliveryId}/accept`
- `PATCH /api/deliveries/{id}/status`
- `POST /api/deliveries/{id}/location`
- `PATCH /api/deliveries/{id}/complete`
- `GET /api/admin/orders`
- `GET /api/admin/dashboard`
- `GET /api/admin/drivers/available`
- `PATCH /api/admin/orders/{id}/assign-driver`
- `GET /api/admin/cash-report`
- `GET /api/admin/settlements?status=PENDING`
- `PATCH /api/admin/settlements/{id}/mark-paid`

## Flutter Setup

```powershell
cd .\northuen_app
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

For web testing against a local backend:

```powershell
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8081
```

The local backend `.env` uses `PORT=8081` to avoid collisions with other dev servers.

## Demo Accounts

When `DEMO_DATA_ENABLED=true`, the backend seeds demo marketplace data:

- Admin: `admin@northuen.bt` / `change-this-admin-password`
- Vendor: `sonam.kitchen@northuen.bt` / `password123`
- Vendor: `karma.store@northuen.bt` / `password123`
- Driver: `pema.driver@northuen.bt` / `password123`
- Driver: `deki.driver@northuen.bt` / `password123`

Seeded vendors include a restaurant and shop with products, plus available drivers for assignment testing.

## Roles

- Customer: browse vendors, view products, cart, COD checkout, parcel request, tracking map, history, reviews.
- Vendor: manage shop profile, add/edit products, view order inbox, accept/reject orders, update preparation status.
- Driver: go online/offline, update delivery status, send live location, mark delivered and payment collected.
- Admin: dashboard metrics, view all orders, assign drivers, manage users/vendors/drivers, monitor COD cash reports.

## Production Readiness

- Flyway migrations run on startup and JPA validates the schema.
- API rate limiting is enabled with `RATE_LIMIT_REQUESTS_PER_MINUTE`.
- Docker files are included for local/container deployment.
- Driver cash settlements are tracked separately from customer COD payment collection.
- Flutter writes critical order, payment, delivery, and settlement data only through backend APIs.

## Verification

These checks pass in the current workspace:

```powershell
cd .\northuen-backend
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
.\gradlew.bat test

cd ..\northuen_app
flutter analyze
flutter test
flutter build web --dart-define=API_BASE_URL=http://localhost:8080
```
