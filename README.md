# CeMIS Backend — Spring Boot + PostgreSQL

Certificate Management Information System for CICT-TeLS, University of Zambia.

## Requirements
- Java 21+
- PostgreSQL 14+
- Maven 3.9+

## Quick start

### 1. Create the PostgreSQL database
```sql
CREATE USER cemis_user WITH PASSWORD 'cemis_pass';
CREATE DATABASE cemis OWNER cemis_user;
```

### 2. Configure application.properties
Edit `src/main/resources/application.properties`:
- `spring.datasource.url` → your PostgreSQL host
- `spring.datasource.username` / `password`
- `app.jwt.secret` → run `openssl rand -hex 64` and paste the result
- `spring.mail.username` / `spring.mail.password` → Gmail app password
- `app.public-url` → deployed frontend URL
- `app.cert.pdf-dir` → absolute path for PDF storage (e.g. `/data/cemis-pdfs`)

### 3. Run
```bash
mvn spring-boot:run
```
Flyway will automatically create all tables on first start.

### 4. Default admin login
- Email: `admin@tels.unza.ac.zm`
- Password: `Admin@1234`
- **You will be prompted to change this on first login.**

## API endpoints (all under /api)

| Method | Path | Description |
|--------|------|-------------|
| POST | /auth/login | Login → returns JWT |
| GET  | /auth/me | Current user info |
| POST | /auth/change-password | Change password |
| GET  | /students | List all students |
| POST | /students | Create student |
| PUT  | /students/{id} | Update student |
| DELETE | /students/{id} | Delete student |
| POST | /students/import | CSV bulk import |
| GET  | /courses | List courses |
| POST | /courses | Create course |
| PUT  | /courses/{id} | Update course |
| GET  | /enrolments | List all enrolments |
| POST | /enrolments | Enrol a student |
| PATCH | /enrolments/{id}/status | Update status |
| PATCH | /enrolments/{id}/payment | Update payment status |
| POST | /enrolments/bulk-start | Start multiple enrolments |
| DELETE | /enrolments/{id} | Remove enrolment |
| POST | /certificates/generate | Generate certificate |
| POST | /certificates/{id}/pdf | Upload PDF from client |
| GET  | /certificates/{id}/pdf | Download PDF |
| POST | /certificates/{id}/send-email | Email certificate |
| GET  | /certificates/verify/{code} | Public verify (no auth) |
| GET  | /settings | Org settings |
| PUT  | /settings | Update org settings |
| GET  | /users | List admin users |
| POST | /users | Create admin user |
| GET  | /reports/stats | Overview statistics |
| GET  | /reports/audit-log | Student audit log |

## Frontend wiring
Set `VITE_API_URL=http://your-server:8080/api` in the frontend `.env`.
All API calls go through `src/lib/api.ts`.
