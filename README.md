# CyMarket

A campus-focused marketplace that connects Iowa State University students so they can buy, sell, and manage items safely within the ISU community. The project ships with a Spring Boot REST API, an Android client, and integrations for image uploads, seller analytics, and password recovery via email.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Backend API](#backend-api)
  - [Android App](#android-app)
- [API Highlights](#api-highlights)
- [Project Status](#project-status)
- [Authors](#authors)

## Overview
CyMarket enables students with an ISU email address to create accounts, browse or post listings, mark favorites, chat with other users, and manage their seller profile. Administrators can review marketplace activity from a dedicated dashboard. The backend persists data in MySQL and uses SendGrid for transactional email, while the Android app consumes the REST API over HTTPS.

## Features
### User experience
- Email + password authentication flow with persistent login state and navigation to sign-up or password recovery screens in the Android client.【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/LoginActivity.java†L18-L108】
- Post items for sale directly from the app, including price, description, and quantity fields backed by the `/items` API.【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/SellActivity.java†L24-L112】【F:Backend/backend_code/src/main/java/onetomany/Items/ItemsController.java†L39-L106】
- Admin dashboard lists all users, displays profile photos, and toggles between user and sales views using Retrofit-powered API calls.【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/AdminDashboardActivity.java†L20-L88】【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/ApiService.java†L16-L46】

### Marketplace platform
- Comprehensive user management endpoints for registration, login, password recovery emails, password resets, and profile image CRUD operations.【F:Backend/backend_code/src/main/java/onetomany/Users/UserController.java†L45-L387】
- Item catalogue endpoints for CRUD operations, seller associations, quantity tracking, and bulk image uploads with Base64 responses.【F:Backend/backend_code/src/main/java/onetomany/Items/ItemsController.java†L39-L278】
- Seller services for onboarding, profile updates, ratings, sales counts, and inventory access control.【F:Backend/backend_code/src/main/java/onetomany/Sellers/SellerController.java†L23-L200】

## Architecture
- **Android application** – Java-based app targeting API level 33+ with Retrofit, Volley, Glide, and RecyclerView for networking and UI.【F:Frontend/CyMarket/app/build.gradle†L1-L42】 The API client centralizes the base URL in `RetroClient` for easy environment changes.【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/RetroClient.java†L6-L18】
- **Spring Boot backend** – Java 17 service built on Spring MVC, Spring Data JPA, WebSocket, and Swagger/OpenAPI for interactive documentation.【F:Backend/backend_code/pom.xml†L1-L92】
- **Infrastructure** – Configured for a MySQL instance and SendGrid SMTP credentials; replace these values with environment-specific secrets before deploying.【F:Backend/backend_code/src/main/resources/application.properties†L1-L30】

## Getting Started
Clone the repository and configure each component separately.

### Backend API
1. **Prerequisites**
   - Java 17 JDK and Maven installed (or use the Maven wrapper).
   - Access to a MySQL database and SMTP credentials (recommended to supply via environment variables rather than committing secrets).【F:Backend/backend_code/src/main/resources/application.properties†L1-L28】
2. **Configuration**
   - Copy `Backend/backend_code/src/main/resources/application.properties` and update the JDBC URL, username, password, and mail settings for your environment. Prefer using `SPRING_DATASOURCE_*` and `SPRING_MAIL_*` environment variables in production.
3. **Run locally**
   ```bash
   cd Backend/backend_code
   mvn spring-boot:run
   ```
   The API starts on port 8080 by default.【F:Backend/backend_code/src/main/resources/application.properties†L1-L2】
4. **Documentation**
   - With the server running, access `/swagger-ui/index.html` to explore the OpenAPI docs provided by Springdoc.【F:Backend/backend_code/pom.xml†L61-L66】

### Android App
1. **Prerequisites**
   - Android Studio Giraffe or newer.
   - Android SDK Platform 33+ and build tools matching the Gradle configuration.【F:Frontend/CyMarket/app/build.gradle†L5-L24】
2. **Import & sync**
   - Open `Frontend/CyMarket` in Android Studio and let Gradle sync dependencies.【F:Frontend/CyMarket/app/build.gradle†L26-L42】
3. **Configure API endpoint**
   - Update `RetroClient.BASE_URL` if your backend runs on a different host/port.【F:Frontend/CyMarket/app/src/main/java/com/example/cymarket/RetroClient.java†L6-L18】
4. **Run the app**
   - Choose an emulator or physical device running Android 13 (API 33) or higher and click **Run**. The login screen will appear first, and you can navigate through buy/sell, messaging, and admin flows using seed data from the backend.

## API Highlights
- `POST /users` – Register a new user and initialize login metadata.【F:Backend/backend_code/src/main/java/onetomany/Users/UserController.java†L133-L150】
- `POST /users/recovery-code` / `POST /users/recover-password` – Email a recovery code and reset forgotten passwords.【F:Backend/backend_code/src/main/java/onetomany/Users/UserController.java†L152-L189】
- `POST /users/{username}/profile-image` – Upload or replace a profile picture using multipart form data.【F:Backend/backend_code/src/main/java/onetomany/Users/UserController.java†L252-L273】
- `POST /items` – Create a standalone item listing with automatic availability and timestamping.【F:Backend/backend_code/src/main/java/onetomany/Items/ItemsController.java†L54-L63】
- `POST /items/seller/{sellerId}` – Create an item that is immediately linked to a seller profile.【F:Backend/backend_code/src/main/java/onetomany/Items/ItemsController.java†L65-L83】
- `POST /sellers/{id}/rate` – Submit a rating that recalculates a seller’s aggregate score.【F:Backend/backend_code/src/main/java/onetomany/Sellers/SellerController.java†L120-L144】

## Project Status
Actively developed. Core account management, item listings, seller metrics, and administrative tooling are functional, with planned improvements for richer listings (images from the mobile client), in-app messaging, and tighter secrets management.

## Authors
Tyler Mestery, Daniel Vergara, Jason Steigerwald, Xander Lefeber
