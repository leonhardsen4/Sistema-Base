package com.sistema.repository;

import com.sistema.config.DatabaseConfig;
import com.sistema.model.Usuario;
import java.sql.*;
//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    
    // Buscar todos os usuários (incluindo inativos)
    public List<Usuario> buscarTodos() throws SQLException {
        var usuarios = new ArrayList<Usuario>();

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM usuarios ORDER BY nome")) {

            var rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
        }

        return usuarios;
    }
    
    // Buscar por ID
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM usuarios WHERE id = ?")) {
            
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }
        }
        
        return Optional.empty();
    }
    
    // Buscar por email
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM usuarios WHERE email = ?")) {
            
            stmt.setString(1, email);
            var rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }
        }
        
        return Optional.empty();
    }
    
    // Criar novo usuário
    public Usuario salvar(Usuario usuario) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                 "INSERT INTO usuarios (nome, email, telefone, senha_hash) VALUES (?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            stmt.setString(4, usuario.getSenhaHash());
            
            stmt.executeUpdate();
            
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getLong(1));
            }
            
            return usuario;
        }
    }
    
    // Atualizar usuário
    public boolean atualizar(Usuario usuario) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                 "UPDATE usuarios SET nome = ?, email = ?, telefone = ?, senha_hash = ?, ativo = ? WHERE id = ?")) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setInt(5, usuario.isAtivo() ? 1 : 0);
            stmt.setLong(6, usuario.getId());

            return stmt.executeUpdate() > 0;
        }
    }
    
    // Atualizar senha
    public boolean atualizarSenha(Long id, String novaSenhaHash) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("UPDATE usuarios SET senha_hash = ? WHERE id = ?")) {
            
            stmt.setString(1, novaSenhaHash);
            stmt.setLong(2, id);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Desativar usuário (soft delete)
    public boolean desativar(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("UPDATE usuarios SET ativo = 0 WHERE id = ?")) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Deletar permanentemente
    public boolean deletar(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Verificar se email já existe
    public boolean emailExiste(String email) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE email = ?")) {
            
            stmt.setString(1, email);
            var rs = stmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    // Mapear ResultSet para Usuario
    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        var usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        
        var timestamp = rs.getTimestamp("data_criacao");
        if (timestamp != null) {
            usuario.setDataCriacao(timestamp.toLocalDateTime());
        }
        
        usuario.setAtivo(rs.getInt("ativo") == 1);
        
        return usuario;
    }
}
