# Pantheon Backend

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-green)
![Coverage](https://img.shields.io/badge/Coverage-80%25_Required-brightgreen)
![Build](https://img.shields.io/badge/Build-GitHub_Actions-blue)

The high-performance, event-driven backend for **Pantheon**, a unified game library manager. This project is architected as a **Modular Monolith** using **Hexagonal Principles (Ports & Adapters)** to strictly isolate business logic from infrastructure.

---

## Tech Stack

* **Language:** Java 25 (Preview Features Enabled)
* **Framework:** Spring Boot 3.4
* **Build Tool:** Gradle (Kotlin DSL)
* **Architecture:** Hexagonal (Package-by-Feature)
* **Database:** SQLite (via `sqlite-jdbc` & Hibernate)
* **Migration:** Flyway (Community Edition)
* **Real-time:** Server-Sent Events (SSE)
* **Mapping:** MapStruct 1.6
* **Quality Control:** JaCoCo (Coverage), ArchUnit (Architecture Testing)

---

## Architecture Overview

Pantheon has moved away from a traditional "Layered" structure (Controller/Service/Repo) to a **Domain-Centric** structure. Code is organized by **Feature** first, then by **Layer**.

### The "Core" Rule
The `core` package contains pure business logic and domain entities. It **never** depends on external frameworks (like File I/O, Third-party APIs, or Web Controllers). It defines interfaces (**Ports**) that the `external` layer implements (**Adapters**).

### Folder Structure
```text
com.pantheon.backend
|──config/
├── core/                        # THE BRAIN (Pure Business Logic)
│   ├── inventory/               # Feature: Game Inventory
│   │   ├── api/                 # REST Controllers (Inbound Adapters)
│   │   ├── model/               # Domain Entities (Game, LibraryEntry)
│   ├── platform/                # Feature: Platform Management (Steam/Epic)
│   └── notification/            # Feature: Event Ports
│
├── external/                    # THE LIMBS (Infrastructure)
│   ├── scanner/                 # Implements file scanning
│   └── notification/            # Implements SSE broadcasting
│
└── shared/                      # Shared Kernel (Value Objects, Utils)
```

## Data Flow: The Scanning Process

1.  **Request:** User hits `POST /api/inventory/scan`.
2.  **Orchestration:** `InventoryDiscoveryService` (Core) retrieves the list of enabled platforms.
3.  **Port Call:** The Service calls the `Scanner` interface. It does not know *how* scanning works, only that it gets a list of games back.
4.  **Adapter Execution:** The `LocalSteamScanner` (External) reads binary VDF files from the disk and maps them to `ScannedGameDTO`.
5.  **Processing:** The Core receives the DTOs, uses **MapStruct** to convert them to Entities, and persists them via JPA.
6.  **Notification:** As games are processed, the Core calls `NotificationPort.notifyBatch()`, which pushes real-time updates to the UI via SSE.

---

## Quality Assurance (The Gatekeeper)

This project enforces strict quality gates via **Gradle** and **GitHub Actions**.

### 1. Code Coverage (JaCoCo)
We do not rely on "vanity metrics." We enforce coverage on **Business Logic Only**.
* **Rule:** 80% Line Coverage & 90% Method Coverage required.
* **Exclusions:** DTOs, Entities, Configurations, and Mappers are excluded from the report to focus on real logic.

### 2. Architecture Tests (ArchUnit)
We use ArchUnit to ensure the architecture doesn't degrade over time.
* **Rule:** Classes in `core` may never import classes from `external`.
* **Rule:** Controllers should not talk to Repositories directly.

### 3. Continuous Integration
Every Pull Request triggers the following pipeline:
1.  **Test:** Runs all Unit Tests.
2.  **Verify:** Generates JaCoCo report and fails if coverage drops below thresholds.
3.  **Build:** Compiles the application to ensure integrity.

---

## Getting Started

### Prerequisites
* JDK 25 (Ensure your IDE is configured for Preview Features)
* Docker (Optional, for future dependencies)

### Build & Run
```bash
# Run the application
./gradlew bootRun

# Run tests and check coverage (The "Gatekeeper" task)
./gradlew testWithCoverage
```

## Contribution Workflow

- Create a feature branch (feat/gog-support).
- Implement your changes.
- Run ./gradlew testWithCoverage locally to ensure you didn't break the build.
- Open a PR to main.
- Squash & Merge once the GitHub Action turns green.