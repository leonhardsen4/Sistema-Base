package com.sistema.service;

import com.sistema.config.DatabaseConfig;
import com.sistema.model.UsuarioDTO;
import java.sql.*;
//import java.time.LocalDateTime;
import java.util.*;

public class AuthService {
    
    private final UsuarioService usuarioService;
    
    public AuthService() {
        this.usuarioService = new UsuarioService();
    }
    
    // Login - retorna token e dados do usuário
    public Optional<Map<String, Object>> login(String email, String senha) throws SQLException, IllegalArgumentException {
        // Autenticar usuário (pode lançar IllegalArgumentException se usuário inativo)
        var usuarioOpt = usuarioService.autenticar(email, senha);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Gerar token único
        var token = gerarToken();

        // Salvar sessão no banco
        salvarSessao(usuario.getId(), token);

        // Retornar dados
        var dados = new HashMap<String, Object>();
        dados.put("token", token);
        dados.put("usuario", new UsuarioDTO(usuario));

        return Optional.of(dados);
    }
    
    // Logout - invalidar token
    public void logout(String token) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM sessoes WHERE token = ?")) {
            
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }
    
    // Verificar se token é válido
    public Optional<UsuarioDTO> verificarToken(String token) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("""
                 SELECT u.* FROM usuarios u
                 INNER JOIN sessoes s ON u.id = s.usuario_id
                 WHERE s.token = ? AND s.expira_em > datetime('now') AND u.ativo = 1
                 """)) {
            
            stmt.setString(1, token);
            var rs = stmt.executeQuery();
            
            if (rs.next()) {
                var usuario = mapearUsuario(rs);
                return Optional.of(new UsuarioDTO(usuario));
            }
        }
        
        return Optional.empty();
    }
    
    // Salvar sessão no banco
    private void salvarSessao(Long usuarioId, String token) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                 "INSERT INTO sessoes (usuario_id, token, expira_em) VALUES (?, ?, datetime('now', '+24 hours'))")) {
            
            stmt.setLong(1, usuarioId);
            stmt.setString(2, token);
            stmt.executeUpdate();
        }
    }
    
    // Gerar token único
    private String gerarToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    // Mapear ResultSet para Usuario
    private com.sistema.model.Usuario mapearUsuario(ResultSet rs) throws SQLException {
        var usuario = new com.sistema.model.Usuario();
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
