# Deploying CeMIS to a Linux server

Covers both repos:
- Backend: `cemis-backend` (Spring Boot, this repo)
- Frontend: `Frontend Database Builder` (TanStack Start / Node)

Target: bare-metal/VM Linux server (e.g. `cemis.unza.ac.zm` → `192.168.99.120`), your own PostgreSQL instance, no Docker.

## 0. Prerequisites on the server

- Java 21 (`java -version`)
- Node.js ≥ 20.6 (`node --version`) — needed for `node --env-file`
- Maven 3.9+ (or use the Maven wrapper if the repo has one)
- PostgreSQL, with a database + user already created for CeMIS
- Nginx
- git

```bash
sudo useradd --system --home /opt/cemis --shell /usr/sbin/nologin cemis
sudo mkdir -p /opt/cemis/backend /opt/cemis/frontend \
              /opt/cemis/data/cert-pdfs /opt/cemis/data/branding-assets \
              /etc/cemis
sudo chown -R cemis:cemis /opt/cemis
```

## 1. Database

Create a database and a dedicated role (adjust names/password to your own):

```sql
CREATE USER cemis_user WITH PASSWORD 'change-me';
CREATE DATABASE cemis OWNER cemis_user;
```

Flyway creates all tables and seeds a default admin user automatically on the
backend's first start — nothing else to run by hand.

## 2. Backend

```bash
sudo -u cemis git clone <backend-repo-url> /opt/cemis/backend-src
cd /opt/cemis/backend-src

cp deploy/backend.env.example /etc/cemis/backend.env
sudo $EDITOR /etc/cemis/backend.env   # fill in DB_*, JWT_SECRET, SMTP_*, CERT_SIGNING_SECRET, PUBLIC_URL
sudo chmod 600 /etc/cemis/backend.env
sudo chown cemis:cemis /etc/cemis/backend.env

./deploy/build.sh                      # builds the jar, copies it to /opt/cemis/backend/app.jar

sudo cp deploy/cemis-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now cemis-backend
sudo systemctl status cemis-backend
```

Generate real secrets instead of the placeholders:
```bash
openssl rand -hex 64          # JWT_SECRET
node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"   # CERT_SIGNING_SECRET
```

Verify it's up: `curl http://127.0.0.1:8080/api/auth/me` should return `401` (not a connection error).

## 3. Frontend

```bash
sudo -u cemis git clone <frontend-repo-url> /opt/cemis/frontend
cd /opt/cemis/frontend

cp deploy/frontend.env.example /etc/cemis/frontend.env
sudo $EDITOR /etc/cemis/frontend.env   # fill in SMTP_*
sudo chmod 600 /etc/cemis/frontend.env
sudo chown cemis:cemis /etc/cemis/frontend.env

./deploy/build.sh                      # writes .env with the prod API URL, npm ci + build

sudo cp deploy/cemis-frontend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now cemis-frontend
sudo systemctl status cemis-frontend
```

Verify it's up: `curl -I http://127.0.0.1:4000/` should return `200`.

## 4. Nginx (single domain, `/api` reverse-proxied)

```bash
sudo cp /opt/cemis/frontend/deploy/nginx-cemis.conf /etc/nginx/sites-available/cemis.unza.ac.zm
sudo ln -s /etc/nginx/sites-available/cemis.unza.ac.zm /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

`192.168.99.120` is a private address, so this is presumably an internal-only
deployment — DNS (`cemis.unza.ac.zm`) is already confirmed pointing there.
For TLS: if the box is reachable from the public internet, run
`sudo certbot --nginx -d cemis.unza.ac.zm`; if it's internal-only, issue an
internal CA certificate instead — Let's Encrypt's HTTP-01 challenge needs
public reachability.

## 5. First login

- URL: `https://cemis.unza.ac.zm/auth` (or `http://...` until TLS is set up)
- Email: `admin@tels.unza.ac.zm`
- Password: `Admin@1234` — you'll be forced to change it on first login.

## 6. Redeploying after code changes

```bash
# backend
cd /opt/cemis/backend-src && git pull && ./deploy/build.sh && sudo systemctl restart cemis-backend

# frontend
cd /opt/cemis/frontend && git pull && ./deploy/build.sh && sudo systemctl restart cemis-frontend
```

## Notes / follow-ups

- Certificate PDFs and branding assets are stored on local disk
  (`/opt/cemis/data/...`). The admin Backup & export screen's "Download full
  backup archive" button produces one ZIP with a full `pg_dump` of the
  database plus every file in both directories — install `postgresql-client`
  (or set `PG_DUMP_PATH`) on the server so `pg_dump` is available to the
  backend process. The screen also still offers per-table CSV/JSON export and
  a storage manifest for quicker, partial exports.
- The frontend still ships with `SMTP_*` config duplicated in its own
  `.env.local`/`frontend.env` for a couple of legacy code paths
  (`lib/email.server.ts`) — only the backend's SMTP config is actually used
  for certificate delivery today (`CertificateController.sendEmail`).
