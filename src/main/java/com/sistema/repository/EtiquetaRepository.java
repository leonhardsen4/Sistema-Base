package com.sistema.repository;

import com.sistema.config.DatabaseConfig;
import com.sistema.model.Etiqueta;
import java.sql.*;
import java.util.*;

public class EtiquetaRepository {
    public List<Etiqueta> buscarTodos() throws SQLException {
        var resultado = new ArrayList<Etiqueta>();
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, nome, data_criacao FROM etiquetas ORDER BY nome ASC")) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    public Optional<Etiqueta> buscarPorId(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, nome, data_criacao FROM etiquetas WHERE id = ?")) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        }
        return Optional.empty();
    }

    public Etiqueta salvar(Etiqueta e) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                     "INSERT INTO etiquetas (nome) VALUES (?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, e.getNome());
            stmt.executeUpdate();
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) e.setId(rs.getLong(1));
        }
        return e;
    }

    public boolean atualizar(Etiqueta e) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("UPDATE etiquetas SET nome = ? WHERE id = ?")) {
            stmt.setString(1, e.getNome());
            stmt.setLong(2, e.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM etiquetas WHERE id = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Conta quantas notas estão associadas a uma etiqueta específica
     */
    public int contarNotasPorEtiqueta(Long etiquetaId) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM notas WHERE etiqueta_id = ?")) {
            stmt.setLong(1, etiquetaId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Retorna um mapa com o contador de notas para cada etiqueta
     */
    public Map<Long, Integer> contarNotasPorTodasEtiquetas() throws SQLException {
        Map<Long, Integer> contadores = new HashMap<>();
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                     "SELECT etiqueta_id, COUNT(*) as total FROM notas GROUP BY etiqueta_id")) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                contadores.put(rs.getLong("etiqueta_id"), rs.getInt("total"));
            }
        }
        return contadores;
    }

    private Etiqueta mapear(ResultSet rs) throws SQLException {
        var e = new Etiqueta();
        e.setId(rs.getLong("id"));
        e.setNome(rs.getString("nome"));
        var ts = rs.getTimestamp("data_criacao");
        if (ts != null) e.setDataCriacao(ts.toLocalDateTime());
        return e;
    }
}