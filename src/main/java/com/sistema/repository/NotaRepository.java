package com.sistema.repository;

import com.sistema.config.DatabaseConfig;
import com.sistema.model.Nota;

import java.sql.*;
import java.util.*;

public class NotaRepository {
    public List<Nota> buscarTodos() throws SQLException {
        var lista = new ArrayList<Nota>();
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, etiqueta_id, status_id, titulo, conteudo, data_criacao, data_atualizacao, prazo_final FROM notas ORDER BY data_criacao DESC")) {
            var rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Nota> buscarPorId(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, etiqueta_id, status_id, titulo, conteudo, data_criacao, data_atualizacao, prazo_final FROM notas WHERE id = ?")) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        }
        return Optional.empty();
    }

    public List<Nota> buscarPorEtiqueta(Long etiquetaId) throws SQLException {
        var lista = new ArrayList<Nota>();
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id, etiqueta_id, status_id, titulo, conteudo, data_criacao, data_atualizacao, prazo_final FROM notas WHERE etiqueta_id = ? ORDER BY data_criacao DESC")) {
            stmt.setLong(1, etiquetaId);
            var rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Nota salvar(Nota n) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                     "INSERT INTO notas (etiqueta_id, status_id, titulo, conteudo, prazo_final) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, n.getEtiquetaId());
            stmt.setLong(2, n.getStatusId());
            stmt.setString(3, n.getTitulo());
            stmt.setString(4, n.getConteudo());
            stmt.setDate(5, java.sql.Date.valueOf(n.getPrazoFinal()));
            stmt.executeUpdate();
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) n.setId(rs.getLong(1));
        }
        return n;
    }

    public boolean atualizar(Nota n) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(
                     "UPDATE notas SET etiqueta_id = ?, status_id = ?, titulo = ?, conteudo = ?, prazo_final = ? WHERE id = ?")) {
            stmt.setLong(1, n.getEtiquetaId());
            stmt.setLong(2, n.getStatusId());
            stmt.setString(3, n.getTitulo());
            stmt.setString(4, n.getConteudo());
            stmt.setDate(5, java.sql.Date.valueOf(n.getPrazoFinal()));
            stmt.setLong(6, n.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM notas WHERE id = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Nota mapear(ResultSet rs) throws SQLException {
        var n = new Nota();
        n.setId(rs.getLong("id"));
        n.setEtiquetaId(rs.getLong("etiqueta_id"));
        n.setStatusId(rs.getLong("status_id"));
        n.setTitulo(rs.getString("titulo"));
        n.setConteudo(rs.getString("conteudo"));
        var tsCriacao = rs.getTimestamp("data_criacao");
        if (tsCriacao != null) n.setDataCriacao(tsCriacao.toLocalDateTime());
        var tsAtualizacao = rs.getTimestamp("data_atualizacao");
        if (tsAtualizacao != null) n.setDataAtualizacao(tsAtualizacao.toLocalDateTime());
        var dtPrazo = rs.getDate("prazo_final");
        if (dtPrazo != null) n.setPrazoFinal(dtPrazo.toLocalDate());
        return n;
    }
}