package com.sistema.repository;

import com.sistema.config.DatabaseConfig;
import com.sistema.model.StatusNota;
import java.sql.*;
import java.util.*;

public class StatusNotaRepository {
    public List<StatusNota> buscarTodos() throws SQLException {
        var lista = new ArrayList<StatusNota>();
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, nome, cor_hex, data_criacao FROM status_nota ORDER BY nome ASC")) {
            var rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<StatusNota> buscarPorId(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, nome, cor_hex, data_criacao FROM status_nota WHERE id = ?")) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        }
        return Optional.empty();
    }

    public StatusNota salvar(StatusNota s) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                     "INSERT INTO status_nota (nome, cor_hex) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, s.getNome());
            stmt.setString(2, s.getCorHex());
            stmt.executeUpdate();
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) s.setId(rs.getLong(1));
        }
        return s;
    }

    public boolean atualizar(StatusNota s) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("UPDATE status_nota SET nome = ?, cor_hex = ? WHERE id = ?")) {
            stmt.setString(1, s.getNome());
            stmt.setString(2, s.getCorHex());
            stmt.setLong(3, s.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM status_nota WHERE id = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private StatusNota mapear(ResultSet rs) throws SQLException {
        var s = new StatusNota();
        s.setId(rs.getLong("id"));
        s.setNome(rs.getString("nome"));
        s.setCorHex(rs.getString("cor_hex"));
        var ts = rs.getTimestamp("data_criacao");
        if (ts != null) s.setDataCriacao(ts.toLocalDateTime());
        return s;
    }
}