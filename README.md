# Trada 📈

Trada is a professional-grade, cross-platform trading journal designed to track, analyze, and optimize trading performance. Built with a modern full-stack architecture, it uses **PostgreSQL** as its mandatory source of truth and **FastAPI** as its high-performance engine.

**Key Feature:** Trada is **fully customizable**. It includes an **Admin Panel** to manage dynamic forms, strategies, and custom fields without code changes.

---

## 🏗 Project Organization

This repository is managed as a monorepo to ensure seamless synchronization between the backend logic and the multi-platform clients.

```text
.
├── backend-api/             # Python FastAPI backend (Trada Engine)
│   ├── app/                 # Logic, models (SQLAlchemy), schemas (Pydantic)
│   ├── Dockerfile           # Backend containerization
│   └── requirements.txt     # Python dependencies (fastapi, sqlalchemy, psycopg2)
├── mobile-desktop-app/      # Kotlin Multiplatform project (Trada Client)
│   ├── composeApp/          # Shared UI code (Dynamic form rendering)
│   ├── shared/              # Core business logic & Ktor API Client
│   └── build.gradle.kts     # KMP configuration
├── docker-compose.yml       # Infrastructure (PostgreSQL 15 + API)
└── .gitignore               # Unified exclusion rules (Python + Kotlin)

```

---

## 🛠 Technology Stack

### Backend & Infrastructure

* **Framework**: [FastAPI](https://fastapi.tiangolo.com/) (Asynchronous Python)
* **Database**: **PostgreSQL 15** (Mandatory)
* **ORM**: SQLAlchemy 2.0
* **Validation**: Pydantic v2 (Rust-powered validation)
* **DevOps**: Docker & Docker Compose

### Frontend (KMP & Compose)

* **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
* **Targets**: Android, iOS, Desktop (JVM), and **Web (WebAssembly/Wasm)**
* **Networking**: [Ktor Client](https://ktor.io/)
* **Serialization**: Kotlinx Serialization (JSON)
* **Navigation**: [Voyager](https://voyager.adriel.cafe/)

---

## 🔄 Gitflow Workflow

Trada follows the **Gitflow** branching model to ensure stability and clean versioning:

* **main**: Official release history (Production stable).
* **develop**: Main integration branch for features.
* **feature/**: New functionalities (e.g., `feature/admin-panel`). Created from `develop`.
* **hotfix/**: Urgent production fixes. Created from `main`, merged to `main` and `develop`.
* **release/**: Preparation for a new production release.

---

## 🚀 Installation & Setup

### 1. Prerequisites

* **Python 3.11+**
* **JDK 17+** (Required for Kotlin and Android)
* **Docker & Docker Compose** (Required for PostgreSQL)
* **Android Studio** (Ladybug or newer)

### 2. Environment Configuration

Create a `.env` file in the **root** or `backend-api/` directory:

```env
DATABASE_URL=postgresql://admin:password@db:5432/trada_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=password
POSTGRES_DB=trada_db

```

### 3. Running the Infrastructure (PostgreSQL + API)

From the root directory:

```bash
docker-compose up -d --build

```

* **API Swagger Docs**: `http://localhost:8000/docs`
* **PostgreSQL**: Internal port `5432`

### 4. Running the Client (Trada App)

1. Open `mobile-desktop-app` in **Android Studio**.
2. **Web (Wasm)**: Run `./gradlew :composeApp:wasmJsRun`
3. **Android**: Select `composeApp` and run on emulator.
4. **Desktop**: Run `./gradlew :composeApp:run`

---

## 📊 Development Workflow

### API Design

FastAPI is optimized to work with KMP. Define your Pydantic schemas in `backend-api/app/schemas.py` first. KMP's **Ktor client** will then consume these JSON models asynchronously using **Coroutines**.

### Adaptive UI

All UI code resides in `mobile-desktop-app/composeApp/src/commonMain`. Use **Adaptive Layouts** to automatically switch between mobile and web/desktop views. UI forms are generated dynamically based on the Admin Panel settings.

---

## 🛡 Issue Board & Labels

### Criticity (MoSCoW)

* `criticity: critical`: Blocker, immediate action required.
* `criticity: must-have`: Essential for the MVP/Milestone.
* `criticity: should-have`: Important feature with high value.
* `criticity: could-have`: Bonus or aesthetic improvement.

### Content & Gitflow Tags

* `type: user-story`: High-level requirement.
* `type: technical-task`: Detailed implementation task.
* `type: feature`, `type: bug`, `type: hotfix`, `type: release`, `type: chore`.

---

## 🛡 Pull Request Strategy

Trada uses a **rebase and squash** strategy:

* **Rebase**: `git pull origin develop --rebase`
* **Squash**: Merge PRs into a single clean commit on the **develop** (for features) or **main** (for releases) branch.

---

## 📚 Additional Resources

* [FastAPI Documentation](https://fastapi.tiangolo.com/)
* [PostgreSQL 15 Official Docs](https://www.postgresql.org/docs/15/index.html)
* [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
* [Kotlin Wasm (WebAssembly) Guide](https://kotl.in/wasm)
* [Gitflow Workflow Guide](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
