# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java web application built with Javalin 6.1.3, SQLite, and BCrypt. It's a note management system with user authentication, tags (etiquetas), status tracking, and notification alerts.

**Tech Stack:**
- Java 21
- Javalin (web framework)
- SQLite (embedded database)
- BCrypt (password hashing)
- Jackson (JSON serialization with JavaTime support)
- Maven (build tool)

## Development Commands

### Running the Application

```bash
# Run without building JAR (fastest for development)
mvn clean compile exec:java

# Build and run JAR
mvn clean package
java -jar target/sistema-base.jar
```

The application starts on port 7070 at `http://localhost:7070`

**Default admin credentials:**
- Email: `admin@sistema.com`
- Password: `admin123`

### Building for Production

```bash
# Standard JAR (~10 MB)
mvn clean package

# GraalVM Native Image (~30 MB, no JVM required)
mvn package -Pnative

# Windows Installer with jpackage
jpackage --input target --name "Sistema Base" --main-jar sistema-base.jar --main-class com.sistema.Main --type exe --dest dist --win-console --win-shortcut
```

### Testing API Endpoints

```bash
# Login
curl -X POST http://localhost:7070/api/auth/login -H "Content-Type: application/json" -d '{"email":"admin@sistema.com","senha":"admin123"}'

# Verify session
curl -X GET http://localhost:7070/api/auth/verificar -H "Authorization: Bearer YOUR_TOKEN"

# List users (requires auth)
curl -X GET http://localhost:7070/api/usuarios -H "Authorization: Bearer YOUR_TOKEN"
```

### Database Management

```bash
# Reset database (deletes all data)
rm database.db  # Linux/Mac
del database.db # Windows
# Database is recreated automatically on next startup

# Access SQLite directly
sqlite3 database.db
.tables
.schema usuarios
SELECT * FROM usuarios;
.quit
```

## Architecture

### Layered Architecture (MVC Pattern)

The application follows a strict layered architecture:

```
Controller → Service → Repository → Database
     ↓          ↓          ↓
   HTTP      Business    Data
  Routing     Logic      Access
```

**Key principle:** Controllers should ONLY handle HTTP concerns (request/response), Services contain business logic, and Repositories handle database operations. Never access the database directly from Controllers.

### Core Components

**Main.java (src/main/java/com/sistema/Main.java:1)**
- Application entry point
- Route registration (lines 62-96)
- Javalin configuration with CORS, static files, and Jackson JSON mapper
- Starts server on port 7070, binds to 0.0.0.0 for external access

**DatabaseConfig.java (src/main/java/com/sistema/config/DatabaseConfig.java:1)**
- Database initialization and schema creation (lines 15-128)
- Creates all tables: usuarios, sessoes, etiquetas, status_nota, notas
- Creates indexes for performance
- Sets up database triggers for timestamp updates
- Creates default admin user and default status values

### Domain Models

The system has 5 main entities:

1. **Usuario** - User accounts with BCrypt-hashed passwords
2. **Etiqueta** - Tags/labels for organizing notes
3. **StatusNota** - Status types with hex colors (Pendente, Em Andamento, Resolvido, etc.)
4. **Nota** - Main entity: notes with title, content, deadline (prazoFinal), linked to etiqueta and status
5. **NotaDTO** - Data transfer object that combines Nota with embedded Etiqueta and StatusNota objects

### Key Architectural Patterns

**DTO Pattern:**
- `NotaDTO` enriches `Nota` with full etiqueta and status objects instead of just IDs
- Controllers return DTOs, not raw entities
- Services handle DTO conversion (see NotaService.java:16-26)

**Repository Pattern:**
- All database access goes through Repository classes
- Repositories return `Optional<T>` for single-item queries
- Use PreparedStatements to prevent SQL injection

**Service Layer:**
- Services coordinate between multiple repositories
- Example: `NotaService.listarTodas()` queries notas, etiquetas, and status, then builds DTOs (NotaService.java:16-27)

**Authentication:**
- Token-based session management
- Tokens stored in `sessoes` table with expiration
- `AuthController.verificarSessao()` validates tokens
- Controllers should check authorization before allowing operations

### Database Schema

**Key relationships:**
- `notas.etiqueta_id` → `etiquetas.id` (ON DELETE CASCADE)
- `notas.status_id` → `status_nota.id` (ON DELETE RESTRICT)
- `sessoes.usuario_id` → `usuarios.id`

**Important fields:**
- `notas.prazo_final` is a DATE (uses LocalDate in Java)
- `usuarios.senha_hash` stores BCrypt hashes (never plain text)
- Timestamps use SQLite's CURRENT_TIMESTAMP

### Jackson Configuration

The ObjectMapper is configured in Main.java:34-37 with:
- JavaTimeModule for LocalDate/LocalDateTime serialization
- Dates serialized as ISO-8601 strings, not timestamps
- This allows proper JSON handling of `prazoFinal` and other date fields

## Adding New Features

### Adding a New Entity

1. **Create Model** in `src/main/java/com/sistema/model/`
2. **Create Repository** in `src/main/java/com/sistema/repository/`
   - Follow pattern: buscarTodos(), buscarPorId(), salvar(), atualizar(), deletar()
3. **Create Service** in `src/main/java/com/sistema/service/`
   - Implement business logic and DTO conversions
4. **Create Controller** in `src/main/java/com/sistema/controller/`
   - Handle HTTP requests/responses
5. **Register routes** in Main.java (around line 73)
6. **Update DatabaseConfig** to create the table (line 15)

### Adding a New Route

Edit Main.java and add route in the ROTAS section (lines 54-97):

```java
app.get("/api/myentity", myController::list);
app.post("/api/myentity", myController::create);
```

## Important Files

- **pom.xml** - Maven configuration, dependencies, build profiles
- **Comandos_Rapidos.md** - Quick reference commands in Portuguese
- **Guia_Empacotamento.md** - Detailed packaging/distribution guide
- **database.db** - SQLite database file (auto-created, gitignored)

## Code Conventions

- Use Java 21 features (text blocks, var, records where applicable)
- Controllers return JSON via ctx.json()
- Error responses use consistent format: `{"sucesso": false, "mensagem": "error message"}`
- Success responses use: `{"sucesso": true, "dados": {...}}`
- Date fields use ISO-8601 format (yyyy-MM-dd for dates, ISO datetime for timestamps)
- Always use BCrypt for password hashing with cost factor 12
- Repository methods use Optional for nullable results
- Service layer throws Exception (caught by Javalin's exception handler in Main.java:99-106)

## Security Notes

- CORS is enabled for all origins (Main.java:43-45) - restrict in production
- Server binds to 0.0.0.0 for network access (Main.java:51)
- Session tokens expire and should be validated on protected routes
- DatabaseConfig.limparSessoesExpiradas() can clean expired sessions (not called automatically)
- Never commit database.db or files with credentials

## Port Configuration

The server runs on port **7070** (Main.java:52). To change:
1. Edit Main.java line 52: `.start(7070)`
2. Recompile and restart

## Static Files

HTML/CSS/JS files in `src/main/resources/public/` are served automatically:
- login.html, cadastro.html, dashboard.html, notisblokk.html
- CSS in public/css/
- JS in public/js/

Root path `/` redirects to `/login.html` (Main.java:59)