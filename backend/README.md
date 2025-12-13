# Pantheon Backend

## Tech Stack

* **Language:** Java 25 (Preview Features Enabled)
* **Framework:** Spring Boot 3.4.0
* **Build Tool:** Gradle (Kotlin DSL)
* **Database:** SQLite (via `sqlite-jdbc`)
* **Migration:** Flyway (Community Edition)
* **Object Mapping:** MapStruct 1.6
* **Boilerplate:** Project Lombok

---

## Architecture Overview

The backend follows a **Ports and Adapters (Hexagonal)** inspired architecture to strictly decouple business logic from external infrastructure (file systems, APIs).

* **Core Logic (`service`):** Contains pure business rules. It depends only on Interfaces (`client`), not concrete implementations.
* **Integration (`integration`):** Contains the "dirty" details of file I/O and API calls. These classes are often package-private and injected via Spring.
* **Data (`model` / `repository`):** Standard JPA/Hibernate layer for persistence.

### Folder Structure

```text
com.pantheon.backend
├── client/                  # Public Interfaces (Contracts)
│   └── LocalGameLibraryClient.java
│
├── integration/          # Concrete Implementations (Adapters)
│   └── scanner/             # Platform-specific logic (Steam, Epic, etc.)
│       └── LocalSteamLibraryScanner.java
│
├── service/                 # Business Logic (The Orchestrator)
│   └── LibraryService.java
│
├── model/                   # JPA Entities (Database Tables)
│   ├── Game.java
│   └── LibraryEntry.java
│
├── dto/                     # Immutable Java Records (Data Transfer)
│   └── ScannedGameDTO.java
│
├── mapper/                  # MapStruct Interfaces
│   └── GameMapper.java
│
├── repository/              # Spring Data JPA Repositories
└── web/                     # REST Controllers
```

## Abstract Data Flow

### Library Scan

* **Trigger**: LibraryService is requested to scan a specific platform (e.g., "Steam").
* **Strategy Selection**: Service looks up the correct Client implementation (e.g., SteamScanner) from a Map, keyed by the platform name.
* **IO Operation**: The Scanner reads the local filesystem (VDF files/Manifests) and returns a list of immutable ScannedGameDTO records.
* **Mapping**: LibraryService passes the DTOs to GameMapper.
* **Conversion**: MapStruct converts ScannedGameDTO → Game (Entity).
* **Persistence**: LibraryService saves the Entity via GameRepository to SQLite.

## Key Design Decisions

* **Strategy Pattern**: The LibraryService injects a List<LocalGameLibraryClient> and converts it to a Map<String, Client>. This allows adding new platforms (e.g., GOG, Epic) without modifying the Service code (Open/Closed Principle).
* **Entity Safety**: JPA Entities use @Getter/@Setter but strictly avoid @Data to prevent hashcode collisions and stack overflow errors.
* **Strict Schema**: spring.jpa.hibernate.ddl-auto=validate. Database schema changes are managed explicitly via Flyway SQL migrations (V1__...sql), ensuring the database is deterministic.
* **Hybrid DTOs**: DTOs are Java Records annotated with Lombok's @Builder, combining immutability with ergonomic instantiation.