# Smart Inventory & Demand Analytics System

A full-stack inventory platform for product, customer, order, supplier, transaction, and demand analytics workflows.

## Architecture

- **Backend:** Java 17, Spring Boot 3.2.3, raw JDBC, MySQL 8
- **Frontend:** React 19, Vite 8
- **ML service:** Python, Flask, scikit-learn RandomForest forecasting
- **Deployment:** Docker Compose with backend, frontend, ML service, and MySQL

The backend follows a controller -> service -> DAO structure. Database access uses prepared JDBC statements; the project does not use JPA, Hibernate, or Spring Data.

## Features

- Product, customer, supplier, order, sales, and stock management
- Role-based access control and tenant isolation
- Stock adjustment and inventory transaction history
- Reorder recommendations, risk detection, and what-if simulations
- Forecast prediction and forecast accuracy telemetry
- ABC inventory classification, demand anomaly detection, and seasonal trends
- Public platform statistics and self-serve tenant onboarding
- Dockerized local deployment

## Repository Layout

```text
backend/       Spring Boot REST API and JUnit tests
frontend/      React/Vite dashboard and public landing page
ml-service/    Flask demand prediction service and Python tests
database/      Schema, seed data, initialization, and migrations
```

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 20+
- Python 3.11+
- MySQL 8, or Docker Desktop

## Run With Docker

From the repository root:

```bash
docker compose up --build
```

The services are then available at:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- ML service: http://localhost:5000
- MySQL: localhost:3306

## Run Locally

### Database

Create a MySQL database named `smart_inventory`, then apply the scripts in this order:

```text
database/schema.sql
database/seed.sql
database/migration_phase10.sql
database/migration_phase11.sql
database/migration_phase14.sql
database/migration_phase16.sql
database/migration_phase17.sql
database/migration_phase18.sql
database/migration_phase19.sql
database/migration_phase22.sql
database/migration_phase23.sql
database/migration_phase29_30.sql
database/migration_phase31.sql
```

Set database environment variables when needed:

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=smart_inventory
DB_USER=root
DB_PASSWORD=your-password
```

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API runs on port `8080`. The main API groups include:

```text
POST /api/auth/login
GET  /api/products
POST /api/orders
GET  /api/transactions
GET  /api/sales/report
GET  /api/analytics/recommendations/{productId}
GET  /api/analytics/risk
GET  /api/bi/abc-analysis
GET  /api/bi/demand-anomalies
GET  /api/bi/seasonal-trends
POST /api/onboarding/register-tenant
GET  /api/telemetry/forecast-accuracy
```

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

The Vite development server runs on port `5173`.

### ML Service

```bash
cd ml-service
python -m venv .venv
.venv\\Scripts\\activate
pip install -r requirements.txt
python app.py
```

The prediction service runs on port `5000`. Set `ML_SERVICE_URL` in the backend environment if it runs at another address.

## Tests and Builds

Backend tests:

```bash
cd backend
mvn test
```

Frontend lint and production build:

```bash
cd frontend
npm run lint
npm run build
```

ML-service tests:

```bash
cd ml-service
python -m unittest discover -s . -p "test_*.py"
```

These checks also run automatically through GitHub Actions in `.github/workflows/ci.yml`.

## Demo Accounts

The seed data contains these development accounts:

```text
admin / admin123
manager / manager123
staff / staff123
```

Change seeded credentials before using the application outside local development.
