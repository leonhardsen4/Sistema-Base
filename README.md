# Sistema Base

Sistema de gerenciamento de notas com autenticação de usuários, etiquetas, status e alertas de notificações.

## 📋 Visão Geral

Sistema web desenvolvido em Java com Javalin, SQLite e BCrypt para gerenciar notas com funcionalidades de:

- Autenticação e gerenciamento de usuários
- Criação e edição de notas com prazos
- Sistema de etiquetas para organização
- Status personalizáveis com cores
- Alertas de notificações para notas próximas do prazo
- Interface responsiva e moderna

## 🚀 Tecnologias

- **Java 21** - Linguagem principal
- **Javalin 6.1.3** - Framework web leve e moderno
- **SQLite** - Banco de dados embarcado
- **BCrypt** - Hashing seguro de senhas
- **Jackson** - Serialização JSON com suporte JavaTime
- **Maven** - Gerenciamento de dependências e build

## 📦 Instalação e Execução

### Pré-requisitos

- Java 21 ou superior
- Maven 3.8+

### Executar em Desenvolvimento

```bash
# Compilar e executar (sem gerar JAR)
mvn clean compile exec:java

# OU construir JAR e executar
mvn clean package
java -jar target/sistema-base.jar
```

O servidor iniciará em `http://localhost:7070`

### Credenciais Padrão

- **Email:** admin@sistema.com
- **Senha:** admin123

## 🏗️ Arquitetura

### Estrutura em Camadas (MVC)

```
Controller → Service → Repository → Database
     ↓          ↓          ↓
   HTTP      Business    Data
  Routing     Logic      Access
```

### Estrutura de Diretórios

```
src/main/java/com/sistema/
├── Main.java                 # Entry point e configuração de rotas
├── config/
│   └── DatabaseConfig.java   # Inicialização do banco
├── controller/              # Controladores HTTP
│   ├── AuthController.java
│   ├── UsuarioController.java
│   ├── NotaController.java
│   ├── EtiquetaController.java
│   └── StatusController.java
├── service/                 # Lógica de negócio
│   ├── NotaService.java
│   └── ...
├── repository/              # Acesso a dados
│   ├── UsuarioRepository.java
│   ├── NotaRepository.java
│   └── ...
└── model/                   # Modelos de domínio
    ├── Usuario.java
    ├── Nota.java
    ├── NotaDTO.java
    ├── Etiqueta.java
    └── StatusNota.java

src/main/resources/public/   # Arquivos estáticos
├── login.html
├── dashboard.html
├── notisblokk.html
├── usuarios.html
├── css/
└── js/
```

## 🗄️ Banco de Dados

### Tabelas Principais

- **usuarios** - Contas de usuário com senhas hasheadas
- **sessoes** - Tokens de autenticação
- **etiquetas** - Tags para organização
- **status_nota** - Status com cores customizáveis
- **notas** - Notas com título, conteúdo, prazo, etiqueta e status

### Relacionamentos

- `notas.etiqueta_id` → `etiquetas.id` (CASCADE)
- `notas.status_id` → `status_nota.id` (RESTRICT)
- `sessoes.usuario_id` → `usuarios.id`

### Gerenciar Banco de Dados

```bash
# Resetar banco (apaga todos os dados)
rm database.db  # Linux/Mac
del database.db # Windows

# Acessar SQLite
sqlite3 database.db
.tables
.schema notas
SELECT * FROM notas;
```

## 🔌 API REST

### Autenticação

```bash
# Login
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sistema.com","senha":"admin123"}'

# Verificar sessão
curl -X GET http://localhost:7070/api/auth/verificar \
  -H "Authorization: Bearer TOKEN"
```

### Endpoints Principais

- `POST /api/auth/login` - Autenticação
- `GET /api/auth/verificar` - Validar token
- `GET /api/usuarios` - Listar usuários
- `POST /api/usuarios` - Criar usuário
- `GET /api/notas` - Listar notas
- `POST /api/notas` - Criar nota
- `PUT /api/notas/:id` - Atualizar nota
- `DELETE /api/notas/:id` - Deletar nota
- `GET /api/etiquetas` - Listar etiquetas
- `GET /api/status` - Listar status

## 📦 Build para Produção

### JAR Padrão

```bash
mvn clean package
# Gera target/sistema-base.jar (~10 MB)
```

### GraalVM Native Image

```bash
mvn package -Pnative
# Gera executável nativo (~30 MB, sem JVM)
```

### Instalador Windows

```bash
jpackage --input target \
  --name "Sistema Base" \
  --main-jar sistema-base.jar \
  --main-class com.sistema.Main \
  --type exe \
  --dest dist \
  --win-console \
  --win-shortcut
```

## 🔒 Segurança

- Senhas hasheadas com BCrypt (cost factor 12)
- Sessões com tokens e expiração
- PreparedStatements para prevenir SQL Injection
- CORS configurado (restringir em produção)

## 🎨 Interface

- Dashboard com visão geral de notas
- Sistema de alertas para notas próximas do prazo
- Filtros por etiquetas
- Links clicáveis para edição rápida
- Layout responsivo e consistente

## 📚 Documentação Adicional

- **CLAUDE.md** - Guia técnico completo para desenvolvimento
- **Comandos_Rapidos.md** - Referência rápida de comandos
- **Guia_Empacotamento.md** - Guia detalhado de distribuição

## 🛠️ Desenvolvimento

### Adicionar Nova Funcionalidade

1. Criar Model em `model/`
2. Criar Repository em `repository/`
3. Criar Service em `service/`
4. Criar Controller em `controller/`
5. Registrar rotas em `Main.java`
6. Atualizar schema em `DatabaseConfig.java`

### Convenções de Código

- Usar recursos do Java 21
- Controllers retornam JSON via `ctx.json()`
- Respostas de erro: `{"sucesso": false, "mensagem": "..."}`
- Respostas de sucesso: `{"sucesso": true, "dados": {...}}`
- Datas em formato ISO-8601
- Repository usa `Optional<T>` para resultados nullable

## 📝 Licença

Este projeto é de uso interno/educacional.

## 👤 Autor

Sistema desenvolvido para gerenciamento de notas e tarefas.