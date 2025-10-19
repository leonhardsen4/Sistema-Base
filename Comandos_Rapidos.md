# ⚡ COMANDOS RÁPIDOS

## 🚀 Desenvolvimento

### Executar sem build
```bash
mvn clean compile exec:java
```

### Executar com hot reload (JRebel ou similar)
```bash
mvn clean compile exec:java -Dexec.classpathScope=runtime
```

---

## 📦 Build

### JAR simples (10 MB)
```bash
mvn clean package
java -jar target/sistema-base.jar
```

### Native Image - GraalVM (30 MB, sem Java)
```bash
# Pré-requisito: GraalVM instalado
mvn package -Pnative
./target/sistema-base
```

### Instalador Windows (70 MB, profissional)
```batch
mvn clean package
jpackage --input target --name "Sistema" --main-jar sistema-base.jar --main-class com.sistema.Main --type exe --dest dist --win-console --win-shortcut
```

---

## 🗄️ Banco de Dados

### Resetar banco (apaga tudo)
```bash
# Parar aplicação, depois:
rm database.db  # Linux/Mac
del database.db  # Windows
# Reiniciar aplicação (recria automaticamente)
```

### Backup do banco
```bash
cp database.db database-backup-$(date +%Y%m%d).db  # Linux/Mac
copy database.db database-backup-%date:~-4,4%%date:~-7,2%%date:~-10,2%.db  # Windows
```

### Acessar SQLite manualmente
```bash
sqlite3 database.db
# Comandos úteis:
.tables              # Listar tabelas
.schema usuarios     # Ver estrutura da tabela
SELECT * FROM usuarios;  # Listar usuários
.quit                # Sair
```

---

## 🧪 Testes

### Testar API manualmente (curl)

**Login:**
```bash
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sistema.com","senha":"admin123"}'
```

**Criar usuário:**
```bash
curl -X POST http://localhost:7070/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","email":"joao@email.com","telefone":"11999999999","senha":"123456"}'
```

**Listar usuários (com token):**
```bash
curl -X GET http://localhost:7070/api/usuarios \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Verificar sessão:**
```bash
curl -X GET http://localhost:7070/api/auth/verificar \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## 🔧 Manutenção

### Limpar builds antigos
```bash
mvn clean
rm -rf target/  # Linux/Mac
rmdir /s target  # Windows
```

### Atualizar dependências
```bash
mvn versions:display-dependency-updates
```

### Ver dependências do projeto
```bash
mvn dependency:tree
```

---

## 🌐 Deploy

### Rodar em background (Linux/Mac)
```bash
nohup java -jar target/sistema-base.jar > sistema.log 2>&1 &
echo $! > sistema.pid
```

### Parar aplicação em background
```bash
kill $(cat sistema.pid)
rm sistema.pid
```

### Rodar como serviço Windows
```batch
# Usar NSSM (Non-Sucking Service Manager)
# Download: https://nssm.cc/download

nssm install SistemaBase "C:\Program Files\Java\jdk-21\bin\java.exe" "-jar C:\sistemas\sistema-base.jar"
nssm start SistemaBase
```

---

## 📊 Monitoramento

### Ver logs em tempo real
```bash
tail -f sistema.log  # Linux/Mac
Get-Content sistema.log -Wait  # Windows PowerShell
```

### Verificar porta em uso
```bash
# Linux/Mac
lsof -i :7070

# Windows
netstat -ano | findstr :7070
```

### Matar processo na porta
```bash
# Linux/Mac
kill -9 $(lsof -t -i:7070)

# Windows
# Pegar PID do comando anterior, depois:
taskkill /PID [PID] /F
```

---

## 🔒 Segurança

### Gerar hash BCrypt (teste)
```bash
# Via Java no terminal
jshell
import at.favre.lib.crypto.bcrypt.BCrypt;
BCrypt.withDefaults().hashToString(12, "minhaSenha".toCharArray());
```

### Verificar hash
```bash
# Via jshell
BCrypt.verifyer().verify("minhaSenha".toCharArray(), "HASH_AQUI").verified
```

---

## 📱 Acesso Remoto

### Descobrir IP local
```bash
# Linux/Mac
ifconfig | grep "inet "
ip addr show

# Windows
ipconfig
```

### Liberar porta no firewall (Windows)
```batch
netsh advfirewall firewall add rule name="Sistema Base" dir=in action=allow protocol=TCP localport=7070
```

### Liberar porta no firewall (Linux)
```bash
sudo ufw allow 7070/tcp
sudo ufw reload
```

---

## 🎨 Personalização

### Mudar porta
Edite `Main.java`:
```java
.start(8080); // Mude 7070 para sua porta
```

### Mudar nome do banco
Edite `DatabaseConfig.java`:
```java
private static final String URL = "jdbc:sqlite:meu-banco.db";
```

### Adicionar logo personalizado
Coloque arquivo `logo.png` em `src/main/resources/public/`

---

## 🚨 Troubleshooting Rápido

### "Address already in use"
```bash
# Algo já usa porta 7070
# Opção 1: Mude a porta no código
# Opção 2: Mate o processo usando a porta (comandos acima)
```

### "Cannot write to database"
```bash
# Problema de permissão
chmod 666 database.db  # Linux/Mac
# Ou rode como administrador (Windows)
```

### "Class not found"
```bash
# Recompilar
mvn clean compile
```

### "Out of memory"
```bash
# Aumentar memória JVM
java -Xmx512m -jar target/sistema-base.jar
```

---

## 📈 Performance

### Ver uso de memória
```bash
# Linux/Mac
ps aux | grep java

# Windows
tasklist | findstr java
```

### Profile da aplicação
```bash
java -jar -agentlib:hprof=cpu=samples target/sistema-base.jar
```

---

## 🎯 Atalhos Maven

```bash
mvn clean           # Limpar
mvn compile         # Compilar
mvn test            # Rodar testes
mvn package         # Gerar JAR
mvn install         # Instalar no repo local
mvn exec:java       # Executar Main
mvn dependency:tree # Ver dependências
```

---

## 📦 Distribuição Rápida

### Criar pacote completo para distribuir
```bash
# 1. Compilar
mvn clean package

# 2. Criar pasta de distribuição
mkdir distribuicao
cp target/sistema-base.jar distribuicao/
cp iniciar.bat distribuicao/  # ou iniciar.sh
cp README.md distribuicao/

# 3. Compactar
# Linux/Mac
tar -czf sistema-base-v1.0.tar.gz distribuicao/
# Windows
# Usar 7-Zip ou WinRAR
```

---

## 🔄 Atualização Rápida

### Atualizar sistema em produção
```bash
# 1. Fazer backup
cp database.db database-backup.db
cp sistema-base.jar sistema-base-old.jar

# 2. Parar aplicação
# (CTRL+C ou kill processo)

# 3. Substituir JAR
cp target/sistema-base.jar ./

# 4. Reiniciar
java -jar sistema-base.jar
```

---

## 📚 Links Úteis

- **Javalin Docs:** https://javalin.io/documentation
- **SQLite Docs:** https://www.sqlite.org/docs.html
- **BCrypt:** https://github.com/patrickfav/bcrypt
- **GraalVM:** https://www.graalvm.org/
- **Maven:** https://maven.apache.org/

---

## 💡 Dicas Finais

### Desenvolvimento mais rápido
```bash
# Use Ctrl+C para parar
# mvn compile exec:java para rodar
# Edite código e repita
```

### Debug
```bash
# Adicione pontos de debug com:
System.out.println("Debug: " + variavel);

# Ou use IDE (IntelliJ, Eclipse, VS Code)
```

### Git (controle de versão)
```bash
git init
git add .
git commit -m "Projeto inicial"

# Ignorar arquivos
echo "target/" >> .gitignore
echo "database.db" >> .gitignore
echo "*.log" >> .gitignore
```

---

**Pronto! Agora você tem todos os comandos na ponta dos dedos! 🚀**