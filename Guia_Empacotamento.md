# 📦 GUIA COMPLETO DE EMPACOTAMENTO

## 📋 Estrutura do Projeto

```
sistema-base/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sistema/
│   │   │       ├── Main.java
│   │   │       ├── config/
│   │   │       │   └── DatabaseConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   └── UsuarioController.java
│   │   │       ├── model/
│   │   │       │   ├── Usuario.java
│   │   │       │   └── UsuarioDTO.java
│   │   │       ├── repository/
│   │   │       │   └── UsuarioRepository.java
│   │   │       └── service/
│   │   │           ├── AuthService.java
│   │   │           └── UsuarioService.java
│   │   └── resources/
│   │       └── public/
│   │           ├── login.html
│   │           ├── cadastro.html
│   │           └── dashboard.html
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

---

## 🚀 OPÇÃO 1: JAR Executável (Mais Simples)

### ✅ Vantagens
- Simples e rápido
- Tamanho pequeno (~10 MB)
- Funciona em qualquer SO com Java instalado

### ⚙️ Como Fazer

#### 1. Compilar o projeto:
```bash
mvn clean package
```

#### 2. JAR gerado em:
```
target/sistema-base.jar
```

#### 3. Executar:
```bash
java -jar target/sistema-base.jar
```

#### 4. Criar script de inicialização:

**Windows (iniciar.bat):**
```batch
@echo off
title Sistema Base

echo ========================================
echo   INICIANDO SISTEMA
echo ========================================
echo.

java -jar sistema-base.jar

pause
```

**Linux/Mac (iniciar.sh):**
```bash
#!/bin/bash

echo "========================================"
echo "  INICIANDO SISTEMA"
echo "========================================"
echo ""

java -jar sistema-base.jar
```

---

## 🔥 OPÇÃO 2: GraalVM Native Image (RECOMENDADO)

### ✅ Vantagens
- Executável nativo (NÃO precisa Java instalado!)
- Inicia em milissegundos
- Tamanho pequeno (~25-50 MB)
- Alta performance

### 📥 Passo 1: Instalar GraalVM

#### Windows:

1. **Baixar GraalVM:**
   - Acesse: https://www.graalvm.org/downloads/
   - Baixe: GraalVM for JDK 21 (Windows)
   - Exemplo: `graalvm-jdk-21_windows-x64_bin.zip`

2. **Extrair:**
   ```
   C:\graalvm\graalvm-jdk-21
   ```

3. **Configurar Variáveis de Ambiente:**
   ```batch
   setx JAVA_HOME "C:\graalvm\graalvm-jdk-21"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

4. **Instalar Native Image:**
   ```batch
   gu install native-image
   ```

5. **Instalar Visual Studio Build Tools:**
   - Baixe: https://visualstudio.microsoft.com/downloads/
   - Instale: "Desktop development with C++"

6. **Abrir Developer Command Prompt:**
   - Procure: "x64 Native Tools Command Prompt for VS"

#### Linux:

```bash
# Baixar GraalVM
curl -L https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.1/graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz -o graalvm.tar.gz

# Extrair
tar -xzf graalvm.tar.gz
sudo mv graalvm-community-openjdk-21.0.1 /opt/graalvm

# Configurar
export JAVA_HOME=/opt/graalvm
export PATH=$JAVA_HOME/bin:$PATH

# Instalar native-image
gu install native-image
```

#### Mac:

```bash
# Instalar via Homebrew
brew install --cask graalvm/tap/graalvm-jdk21

# Ou baixar manualmente
curl -L https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.1/graalvm-community-jdk-21.0.1_macos-aarch64_bin.tar.gz -o graalvm.tar.gz

# Configurar
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Instalar native-image
gu install native-image
```

### ⚙️ Passo 2: Criar Arquivos de Configuração

Crie a pasta `src/main/resources/META-INF/native-image/` e adicione:

**reflect-config.json:**
```json
[
  {
    "name": "com.sistema.model.Usuario",
    "allDeclaredConstructors": true,
    "allPublicConstructors": true,
    "allDeclaredMethods": true,
    "allPublicMethods": true,
    "allDeclaredFields": true,
    "allPublicFields": true
  },
  {
    "name": "com.sistema.model.UsuarioDTO",
    "allDeclaredConstructors": true,
    "allPublicConstructors": true,
    "allDeclaredMethods": true,
    "allPublicMethods": true,
    "allDeclaredFields": true,
    "allPublicFields": true
  }
]
```

**resource-config.json:**
```json
{
  "resources": {
    "includes": [
      {
        "pattern": ".*\\.html$"
      },
      {
        "pattern": ".*\\.css$"
      },
      {
        "pattern": ".*\\.js$"
      },
      {
        "pattern": "public/.*"
      }
    ]
  }
}
```

### 🔨 Passo 3: Compilar Native Image

#### Método 1: Via Maven (Recomendado)

```bash
# Compilar para JAR primeiro
mvn clean package

# Compilar para Native Image
mvn package -Pnative
```

#### Método 2: Manual

```bash
# 1. Compilar JAR
mvn clean package

# 2. Gerar Native Image
native-image \
    -jar target/sistema-base.jar \
    --no-fallback \
    -H:+ReportExceptionStackTraces \
    --enable-url-protocols=http,https \
    --initialize-at-build-time \
    -H:ResourceConfigurationFiles=src/main/resources/META-INF/native-image/resource-config.json \
    -H:ReflectionConfigurationFiles=src/main/resources/META-INF/native-image/reflect-config.json \
    sistema-base
```

### 📦 Resultado

- **Windows:** `sistema-base.exe` (~25-40 MB)
- **Linux:** `sistema-base` (~30-50 MB)
- **Mac:** `sistema-base` (~25-40 MB)

### ▶️ Executar

```bash
# Windows
sistema-base.exe

# Linux/Mac
./sistema-base
```

---

## 📦 OPÇÃO 3: jpackage (Java 14+)

### ✅ Vantagens
- Cria instalador profissional (.exe, .msi, .deb, .dmg)
- JRE embutido (não precisa Java instalado)
- Ícone personalizado
- Atalhos automáticos

### ⚙️ Como Fazer

#### 1. Compilar JAR:
```bash
mvn clean package
```

#### 2. Criar executável com jpackage:

**Windows (EXE):**
```batch
jpackage ^
    --input target ^
    --name "Sistema Base" ^
    --main-jar sistema-base.jar ^
    --main-class com.sistema.Main ^
    --type exe ^
    --dest dist ^
    --app-version 1.0 ^
    --vendor "Seu Nome/Empresa" ^
    --description "Sistema de gestão" ^
    --win-console ^
    --win-shortcut ^
    --win-menu
```

**Windows (Instalador MSI):**
```batch
jpackage ^
    --input target ^
    --name "Sistema Base" ^
    --main-jar sistema-base.jar ^
    --main-class com.sistema.Main ^
    --type msi ^
    --dest dist ^
    --app-version 1.0 ^
    --vendor "Seu Nome/Empresa" ^
    --description "Sistema de gestão" ^
    --win-shortcut ^
    --win-menu ^
    --win-dir-chooser
```

**Linux (DEB):**
```bash
jpackage \
    --input target \
    --name "sistema-base" \
    --main-jar sistema-base.jar \
    --main-class com.sistema.Main \
    --type deb \
    --dest dist \
    --app-version 1.0 \
    --vendor "Seu Nome/Empresa" \
    --description "Sistema de gestão" \
    --linux-shortcut
```

**Mac (DMG):**
```bash
jpackage \
    --input target \
    --name "Sistema Base" \
    --main-jar sistema-base.jar \
    --main-class com.sistema.Main \
    --type dmg \
    --dest dist \
    --app-version 1.0 \
    --vendor "Seu Nome/Empresa" \
    --description "Sistema de gestão" \
    --mac-package-name "Sistema Base"
```

#### 3. Adicionar Ícone (Opcional):

Coloque um arquivo `icone.ico` (Windows) ou `icone.png` (Linux/Mac) e adicione:

```batch
--icon recursos/icone.ico
```

### 📦 Resultado

- **Windows EXE:** `dist/Sistema Base-1.0.exe` (~60-80 MB)
- **Windows MSI:** `dist/Sistema Base-1.0.msi` (~60-80 MB) - Instalador profissional
- **Linux DEB:** `dist/sistema-base_1.0-1_amd64.deb`
- **Mac DMG:** `dist/Sistema Base-1.0.dmg`

---

## 🎯 Comparação das Opções

| Método | Tamanho | Precisa Java? | Instalação | Performance | Dificuldade |
|--------|---------|---------------|------------|-------------|-------------|
| **JAR** | ~10 MB | ✅ Sim | Copiar arquivo | Boa | ⭐ Fácil |
| **GraalVM** | ~30 MB | ❌ Não | Copiar arquivo | Excelente | ⭐⭐⭐ Média |
| **jpackage** | ~70 MB | ❌ Não | Instalador | Boa | ⭐⭐ Fácil |

---

## 🚀 Scripts Prontos

### build-all.bat (Windows)

```batch
@echo off
echo ========================================
echo   BUILD COMPLETO
echo ========================================
echo.

echo [1/3] Compilando JAR...
call mvn clean package
if %errorlevel% neq 0 exit /b 1

echo.
echo [2/3] Criando Native Image (GraalVM)...
call mvn package -Pnative
if %errorlevel% neq 0 echo Native Image falhou (opcional)

echo.
echo [3/3] Criando Instalador (jpackage)...
call jpackage ^
    --input target ^
    --name "Sistema Base" ^
    --main-jar sistema-base.jar ^
    --main-class com.sistema.Main ^
    --type exe ^
    --dest dist ^
    --app-version 1.0 ^
    --vendor "Orgao" ^
    --win-console ^
    --win-shortcut ^
    --win-menu

echo.
echo ========================================
echo   BUILD CONCLUIDO!
echo ========================================
echo.
echo Arquivos gerados:
echo   - JAR: target\sistema-base.jar
echo   - Native: target\sistema-base.exe (se GraalVM instalado)
echo   - Instalador: dist\Sistema Base-1.0.exe
echo.
pause
```

### build-all.sh (Linux/Mac)

```bash
#!/bin/bash

echo "========================================"
echo "  BUILD COMPLETO"
echo "========================================"
echo ""

echo "[1/3] Compilando JAR..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "Erro ao compilar JAR"
    exit 1
fi

echo ""
echo "[2/3] Criando Native Image (GraalVM)..."
mvn package -Pnative
if [ $? -ne 0 ]; then
    echo "Native Image falhou (opcional)"
fi

echo ""
echo "[3/3] Criando Instalador (jpackage)..."
jpackage \
    --input target \
    --name "sistema-base" \
    --main-jar sistema-base.jar \
    --main-class com.sistema.Main \
    --type deb \
    --dest dist \
    --app-version 1.0 \
    --vendor "Orgao" \
    --linux-shortcut

echo ""
echo "========================================"
echo "  BUILD CONCLUIDO!"
echo "========================================"
echo ""
echo "Arquivos gerados:"
echo "  - JAR: target/sistema-base.jar"
echo "  - Native: target/sistema-base (se GraalVM instalado)"
echo "  - Instalador: dist/sistema-base_1.0-1_amd64.deb"
echo ""
```

---

## 📋 README.md do Projeto

Crie um arquivo `README.md` na raiz:

```markdown
# Sistema Base - Java 21 + Javalin + SQLite

Sistema web moderno com autenticação de usuários.

## 🚀 Funcionalidades

- ✅ Login e autenticação
- ✅ Cadastro de usuários
- ✅ Dashboard responsivo
- ✅ Banco de dados SQLite embutido
- ✅ Senhas criptografadas (BCrypt)
- ✅ API REST completa

## 🛠️ Tecnologias

- Java 21
- Javalin 6.1.3
- SQLite
- BCrypt
- HTML/CSS/JavaScript

## 📦 Como Executar

### Opção 1: Com Maven
```bash
mvn clean package
java -jar target/sistema-base.jar
```

### Opção 2: Com Maven Run
```bash
mvn clean compile exec:java
```

### Opção 3: Executável (se gerado)
```bash
# Windows
sistema-base.exe

# Linux/Mac
./sistema-base
```

## 🌐 Acessar

Após iniciar, acesse:
- http://localhost:7070

**Usuário padrão:**
- Email: admin@sistema.com
- Senha: admin123

## 📂 Estrutura

```
src/
├── main/
│   ├── java/com/sistema/
│   │   ├── Main.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── resources/public/
│       ├── login.html
│       ├── cadastro.html
│       └── dashboard.html
└── test/
```

## 🔧 Desenvolvimento

### Adicionar Nova Funcionalidade

1. **Criar Model** (se necessário):
```java
// src/main/java/com/sistema/model/MinhaEntidade.java
```

2. **Criar Repository**:
```java
// src/main/java/com/sistema/repository/MinhaRepository.java
```

3. **Criar Service**:
```java
// src/main/java/com/sistema/service/MinhaService.java
```

4. **Criar Controller**:
```java
// src/main/java/com/sistema/controller/MinhaController.java
```

5. **Registrar Rotas** em `Main.java`:
```java
app.get("/api/minha-rota", minhaController::metodo);
```

6. **Criar Interface** (opcional):
```html
<!-- src/main/resources/public/minha-tela.html -->
```

## 📦 Build para Produção

### JAR Executável
```bash
mvn clean package
# Resultado: target/sistema-base.jar
```

### Native Image (GraalVM)
```bash
mvn package -Pnative
# Resultado: target/sistema-base.exe (Windows) ou target/sistema-base (Linux/Mac)
```

### Instalador (jpackage)
```bash
# Ver guia-empacotamento.md para instruções detalhadas
```

## 🗄️ Banco de Dados

O sistema usa SQLite com arquivo `database.db` criado automaticamente.

### Tabelas:
- `usuarios` - Dados dos usuários
- `sessoes` - Controle de login

### Backup:
Basta copiar o arquivo `database.db`

## 🔒 Segurança

- Senhas criptografadas com BCrypt
- Tokens de sessão únicos
- Validação de dados
- SQL Injection prevention (PreparedStatements)

## 📝 Licença

MIT License

## 👨‍💻 Autor

Seu Nome
```

---

## ⚡ GUIA RÁPIDO - Começar Agora

### 1. Criar projeto:
```bash
mkdir sistema-base
cd sistema-base
```

### 2. Copiar arquivos:
- Copie todos os arquivos Java para `src/main/java/com/sistema/`
- Copie HTML para `src/main/resources/public/`
- Copie `pom.xml` para raiz

### 3. Compilar e executar:
```bash
mvn clean compile exec:java
```

### 4. Acessar:
```
http://localhost:7070
```

### 5. Login:
```
Email: admin@sistema.com
Senha: admin123
```

---

## 🎯 DECISÃO RÁPIDA - Qual usar?

### Para DESENVOLVIMENTO e TESTES:
```bash
mvn clean compile exec:java
```
✅ Rápido, simples, sem build

### Para DISTRIBUIÇÃO INTERNA (órgão):
```bash
mvn clean package
```
✅ JAR de 10MB, funciona em qualquer Java 8+

### Para DISTRIBUIÇÃO EXTERNA (sem Java):
```bash
# GraalVM Native Image
mvn package -Pnative
```
✅ Executável nativo, não precisa Java, 30MB

### Para INSTALAÇÃO PROFISSIONAL:
```bash
# jpackage - Instalador com JRE embutido
jpackage --input target --name "Sistema" --main-jar sistema-base.jar ...
```
✅ Instalador MSI/DEB/DMG, 70MB, experiência profissional

---

## 🐛 Solução de Problemas

### Erro: "Java não encontrado"
```bash
# Verificar instalação
java -version

# Configurar JAVA_HOME (Windows)
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
```

### Erro: "Port 7070 already in use"
Edite `Main.java` e mude a porta:
```java
.start(8080); // ou outra porta
```

### Erro: "Cannot access database"
- Verifique permissões da pasta
- Apague `database.db` e reinicie (recria automaticamente)

### Native Image não compila
1. Verifique se GraalVM está instalado: `gu list`
2. Verifique Visual Studio Build Tools (Windows)
3. Use Developer Command Prompt (Windows)

### jpackage não encontrado
- Certifique-se de usar Java 14+: `java -version`
- jpackage vem incluído no JDK

---

## 📚 Próximos Passos

### Melhorias Sugeridas:

1. **Adicionar paginação** na listagem de usuários
2. **Upload de arquivos** (avatar do usuário)
3. **Relatórios em PDF** (usando iText)
4. **Logs de auditoria** (quem fez o quê, quando)
5. **Permissões por papel** (admin, usuário, etc)
6. **Recuperação de senha** via email
7. **Dashboard com gráficos** (Chart.js)
8. **Temas claro/escuro**
9. **Notificações em tempo real** (WebSocket)
10. **API REST documentada** (OpenAPI/Swagger)

### Próximas Funcionalidades (Exemplos):

#### Módulo de Processos:
```java
// model/Processo.java
// repository/ProcessoRepository.java
// service/ProcessoService.java
// controller/ProcessoController.java
```

#### Módulo de Relatórios:
```java
// service/RelatorioService.java
// Usar Apache POI para Excel ou iText para PDF
```

#### Módulo de Arquivos:
```java
// service/ArquivoService.java
// Upload/Download de documentos
```

---

## ✅ CHECKLIST FINAL

Antes de distribuir:

- [ ] Código compila sem erros
- [ ] Todos os testes passam
- [ ] Banco de dados funciona
- [ ] Login funciona
- [ ] Cadastro funciona
- [ ] Todas as rotas respondem
- [ ] Testado em rede local
- [ ] README.md atualizado
- [ ] Scripts de inicialização funcionam
- [ ] Build para produção gerado
- [ ] Testado em máquina limpa (sem desenvolvimento)

---

## 🎓 CONCLUSÃO

Você agora tem:

✅ **Projeto base funcional** com login e cadastro
✅ **3 formas de distribuição** (JAR, Native, Instalador)
✅ **Estrutura escalável** para adicionar funcionalidades
✅ **Código limpo e organizado** (MVC pattern)
✅ **Segurança implementada** (BCrypt, tokens)
✅ **Pronto para produção** no ambiente corporativo

**Próximo passo:** Adicione suas funcionalidades específicas! 🚀
```

---

Finalmente, vou criar um último arquivo com comandos rápidos: