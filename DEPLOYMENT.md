# 🚀 Production Deployment Guide — HRMS Portal

This document provides complete instructions for deploying the **HRMS Portal** across all major environments:
1. **Cloud Platforms (Render / Railway / Fly.io)**
2. **Docker Compose on VPS (DigitalOcean / AWS EC2 / Linode / Hetzner)**
3. **Native Linux VPS (Systemd Service + Nginx)**
4. **Automated CI/CD with GitHub Actions**
5. **Domain & SSL Setup (Let's Encrypt)**

Production requires `HRMS_BOOTSTRAP_ADMIN_USERNAME` and a unique `HRMS_BOOTSTRAP_ADMIN_PASSWORD` of at least 16 characters for the first startup. Configure them as platform environment variables or secrets; once an enabled administrator exists, the bootstrap values can be removed.

---

## 📋 Pre-Deployment Checklist

- [x] Java 17 compatible (`pom.xml`)
- [x] Multi-stage production `Dockerfile` with unprivileged user
- [x] Production properties file (`src/main/resources/application-prod.properties`)
- [x] Multi-database support: **MySQL 8**, **PostgreSQL**, or **H2**
- [x] Health checks exposed at `/actuator/health`
- [x] Production environment template (`.env.example`)

---

## 🌐 Option 1: 1-Click Cloud Deployment (Render.com)

Render allows you to deploy both the web application and a managed database effortlessly.

### Steps:
1. Push your code to **GitHub** or **GitLab**.
2. Go to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** ➔ **Blueprint**.
4. Select your repository. Render will automatically detect [`render.yaml`](file:///d:/Javaaaa/hrms-portal/render.yaml).
5. Render will automatically provision:
   - **PostgreSQL Database** (`hrms-postgres`)
   - **HRMS Web Service** (`hrms-portal`) built directly from the `Dockerfile`.
6. Enter a bootstrap administrator username and a unique password of at least 16 characters when prompted for `HRMS_BOOTSTRAP_ADMIN_USERNAME` and `HRMS_BOOTSTRAP_ADMIN_PASSWORD`.
7. Click **Apply**.
8. Once deployed, Render provides an HTTPS URL (e.g., `https://hrms-portal.onrender.com`).

---

## 🚂 Option 2: Deploy to Railway

Railway detects the included [`Dockerfile`](file:///d:/Javaaaa/hrms-portal/Dockerfile) and [`railway.json`](file:///d:/Javaaaa/hrms-portal/railway.json).

### Steps:
1. Go to [Railway.app](https://railway.app) and create a **New Project**.
2. Select **Deploy from GitHub repo** and pick your HRMS repository.
3. Click **+ New** inside the project to add a **Database**:
   - Choose **MySQL** or **PostgreSQL**.
4. Link the database variables:
   - In your Web Service settings, add variable `DB_URL` referencing your database connection URL (or Railway's `DATABASE_URL`).
   - Set `SPRING_PROFILES_ACTIVE=prod`.
   - Add `HRMS_BOOTSTRAP_ADMIN_USERNAME` and `HRMS_BOOTSTRAP_ADMIN_PASSWORD`, using a unique password of at least 16 characters.
5. Railway will build and deploy automatically with zero manual server maintenance.

For Fly.io, set the bootstrap values as secrets before the first deployment:
```bash
fly secrets set HRMS_BOOTSTRAP_ADMIN_USERNAME=your-admin HRMS_BOOTSTRAP_ADMIN_PASSWORD=your-unique-16-character-or-longer-secret
```

---

## 🐳 Option 3: Docker & Docker Compose (Any Linux VPS / Cloud Server)

Ideal for **DigitalOcean Droplets**, **AWS EC2**, **Linode**, or **Hetzner**.

### Steps:

1. **Connect to your server via SSH:**
   ```bash
   ssh root@your-server-ip
   ```

2. **Install Docker & Docker Compose** (if not installed):
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh
   apt-get install -y docker-compose-plugin
   ```

3. **Clone your repository:**
   ```bash
   git clone https://github.com/your-username/hrms-portal.git /opt/hrms-portal
   cd /opt/hrms-portal
   ```

4. **Configure Environment Variables:**
   ```bash
   cp .env.example .env
   nano .env
   ```
   Set `DB_PASSWORD` to a strong database password and set `HRMS_BOOTSTRAP_ADMIN_USERNAME` plus a unique `HRMS_BOOTSTRAP_ADMIN_PASSWORD` of at least 16 characters.

5. **Build and Start Containers:**
   ```bash
   docker compose up -d --build
   ```

6. **Verify Running Containers:**
   ```bash
   docker compose ps
   docker compose logs -f hrms-portal
   ```

   After the first successful startup, remove the bootstrap admin values from `.env`; keep the database password.

Your portal will be live at `http://your-server-ip:8080`.

---

## 🐧 Option 4: Native Linux VPS Deployment (Systemd Service)

If you prefer running without Docker directly on Ubuntu/Debian:

### 1. Install Java 17 & MySQL Server:
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven mysql-server
```

### 2. Configure MySQL Database:
```sql
sudo mysql
CREATE DATABASE hrms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hrms_user'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';
GRANT ALL PRIVILEGES ON hrms_db.* TO 'hrms_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Create Dedicated Linux User:
```bash
sudo useradd -r -s /bin/false hrms
```

### 4. Deploy using the included script:
```bash
cd /opt/hrms-portal
sudo cp deployment/hrms.service /etc/systemd/system/hrms.service

# Create the private environment file referenced by the service
sudo install -d -m 700 /etc/hrms
sudo install -m 600 /dev/null /etc/hrms/hrms.env
sudo nano /etc/hrms/hrms.env
```
Add `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `HRMS_BOOTSTRAP_ADMIN_USERNAME`, and `HRMS_BOOTSTRAP_ADMIN_PASSWORD` to `/etc/hrms/hrms.env`. Use a unique bootstrap password of at least 16 characters.

```bash
# Run deployment script
sudo chmod +x deployment/deploy.sh
sudo ./deployment/deploy.sh
```
After the first successful startup, remove the bootstrap admin values from `/etc/hrms/hrms.env`; keep the database settings.

---

## 🔒 Option 5: Domain & SSL Reverse Proxy (Nginx + Let's Encrypt)

To serve your portal over HTTPS with a custom domain (e.g. `hrms.yourcompany.com`):

### 1. Install Nginx & Certbot:
```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

### 2. Copy and Configure Nginx Site:
```bash
sudo cp deployment/nginx-hrms.conf /etc/nginx/sites-available/hrms.conf
sudo sed -i 's/hrms.yourdomain.com/hrms.yourcompany.com/g' /etc/nginx/sites-available/hrms.conf
sudo ln -s /etc/nginx/sites-available/hrms.conf /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 3. Issue Free Let's Encrypt SSL Certificate:
```bash
sudo certbot --nginx -d hrms.yourcompany.com
```
*Certbot will automatically configure HTTPS, renewal crons, and SSL encryption.*

---

## 🔄 Option 6: CI/CD Pipeline (GitHub Actions)

The repository includes a ready-to-use GitHub Actions workflow in [`.github/workflows/deploy.yml`](file:///d:/Javaaaa/hrms-portal/.github/workflows/deploy.yml).

### Features:
- **On Every Pull Request / Push:** Automatically runs unit and integration tests with Maven.
- **On Push to `main`:**
  - Builds the production executable `.jar` file.
  - Builds the production multi-stage Docker container.
  - Publishes the container image directly to **GitHub Container Registry (GHCR)** (`ghcr.io/your-user/hrms-portal:latest`).

---

## 🛡️ Production Security Recommendations

1. **Secure Initial Admin Access:**
   Production does not enable public demo credentials. Use the bootstrap admin variables for the first startup, then remove them and manage accounts through the portal.
2. **Restrict Actuator in Production:**
   In `application-prod.properties`, actuator endpoints are limited to `/health` and `/info` with details hidden for unauthenticated traffic.
3. **Database Backups:**
   Set up an automated daily MySQL dump cron:
   ```bash
   0 2 * * * mysqldump -u hrms_user -p'YourPassword' hrms_db | gzip > /backups/hrms_db_$(date +\%F).sql.gz
   ```
