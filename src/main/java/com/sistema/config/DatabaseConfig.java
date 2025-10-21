package com.sistema.config;

import java.sql.*;

public class DatabaseConfig {
    
    private static final String URL = "jdbc:sqlite:database.db";
    
    // Obter conexão com o banco
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        // IMPORTANTE: Habilitar foreign keys no SQLite (por padrão vem desabilitado)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
    
    // Inicializar banco de dados (criar tabelas)
    public static void inicializar() {
        try (var conn = getConnection(); var stmt = conn.createStatement()) {
            
            // Criar tabela de usuários
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    telefone TEXT,
                    senha_hash TEXT NOT NULL,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    ativo INTEGER DEFAULT 1
                )
            """);
            
            // Criar tabela de sessões (para controle de login)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sessoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    usuario_id INTEGER NOT NULL,
                    token TEXT NOT NULL UNIQUE,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expira_em TIMESTAMP NOT NULL,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
            """);
            
            // Criar índices para melhor performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sessoes_token ON sessoes(token)");

            // Criar tabela de etiquetas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS etiquetas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Índice para etiquetas
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_etiquetas_nome ON etiquetas(nome)");

            // Criar tabela de status de nota
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS status_nota (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE,
                    cor_hex TEXT NOT NULL,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Índice para status_nota
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_status_nome ON status_nota(nome)");

            // Inserir status padrão
            try (var ps = conn.prepareStatement("INSERT OR IGNORE INTO status_nota (nome, cor_hex) VALUES (?, ?)") ) {
                String[][] padroes = new String[][] {
                    {"Pendente", "#FFA500"},
                    {"Em Andamento", "#4A90E2"},
                    {"Resolvido", "#10B981"},
                    {"Suspenso", "#9CA3AF"},
                    {"Cancelado", "#EF4444"}
                };
                for (String[] p : padroes) {
                    ps.setString(1, p[0]);
                    ps.setString(2, p[1]);
                    ps.executeUpdate();
                }
            }

            // Tabela de notas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    etiqueta_id INTEGER NOT NULL,
                    status_id INTEGER NOT NULL,
                    titulo TEXT NOT NULL,
                    conteudo TEXT,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    prazo_final DATE NOT NULL,
                    FOREIGN KEY (etiqueta_id) REFERENCES etiquetas(id) ON DELETE CASCADE,
                    FOREIGN KEY (status_id) REFERENCES status_nota(id) ON DELETE RESTRICT
                )
            """);

            // Índices de notas
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notas_etiqueta ON notas(etiqueta_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notas_status ON notas(status_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notas_prazo ON notas(prazo_final)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notas_titulo ON notas(titulo)");

            // Trigger de atualização de timestamp
            stmt.execute("""
                CREATE TRIGGER IF NOT EXISTS update_nota_timestamp
                AFTER UPDATE ON notas
                BEGIN
                    UPDATE notas SET data_atualizacao = CURRENT_TIMESTAMP
                    WHERE id = NEW.id;
                END;
            """);
            
            System.out.println("✅ Banco de dados inicializado com sucesso!");
            
            // Criar usuário admin padrão se não existir
            criarUsuarioAdmin();
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Criar usuário admin padrão (para testes)
    private static void criarUsuarioAdmin() {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE email = ?")) {
            
            stmt.setString(1, "admin@sistema.com");
            var rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Admin não existe, criar
                try (var insertStmt = conn.prepareStatement(
                    "INSERT INTO usuarios (nome, email, telefone, senha_hash) VALUES (?, ?, ?, ?)")) {
                    
                    insertStmt.setString(1, "Administrador");
                    insertStmt.setString(2, "admin@sistema.com");
                    insertStmt.setString(3, "(00) 00000-0000");
                    
                    // Senha: "admin123" (você pode mudar depois)
                    var senhaHash = at.favre.lib.crypto.bcrypt.BCrypt
                        .withDefaults()
                        .hashToString(12, "admin123".toCharArray());
                    insertStmt.setString(4, senhaHash);
                    
                    insertStmt.executeUpdate();
                    System.out.println("✅ Usuário admin criado!");
                    System.out.println("   Email: admin@sistema.com");
                    System.out.println("   Senha: admin123");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao criar usuário admin: " + e.getMessage());
        }
    }
    
    // Limpar sessões expiradas (pode ser chamado periodicamente)
    public static void limparSessoesExpiradas() {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("DELETE FROM sessoes WHERE expira_em < datetime('now')")) {
            
            int deletadas = stmt.executeUpdate();
            if (deletadas > 0) {
                System.out.println("🗑️  " + deletadas + " sessões expiradas removidas");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao limpar sessões: " + e.getMessage());
        }
    }
}
