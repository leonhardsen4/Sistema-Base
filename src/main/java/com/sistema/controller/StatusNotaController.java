package com.sistema.controller;

import com.sistema.model.StatusNota;
import com.sistema.repository.StatusNotaRepository;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatusNotaController {
    private final StatusNotaRepository repository = new StatusNotaRepository();

    public void listar(Context ctx) {
        try {
            List<StatusNota> status = repository.buscarTodos();
            ctx.json(Map.of(
                "sucesso", true,
                "dados", status
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao listar status: " + e.getMessage()
            ));
        }
    }

    public void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            var statusOpt = repository.buscarPorId(id);
            if (statusOpt.isPresent()) {
                ctx.json(Map.of("sucesso", true, "dados", statusOpt.get()));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Status não encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao buscar status: " + e.getMessage()));
        }
    }

    public static class StatusRequest {
        public String nome;
        public String corHex;
    }

    public void criar(Context ctx) {
        try {
            StatusRequest req = ctx.bodyAsClass(StatusRequest.class);
            String nome = req != null ? req.nome : null;
            String corHex = req != null ? req.corHex : null;

            if (nome == null || nome.trim().isEmpty()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Nome do status é obrigatório"));
                return;
            }
            if (corHex == null || !corHex.matches("^#([0-9a-fA-F]{6})$")) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Cor inválida. Use formato #RRGGBB"));
                return;
            }

            StatusNota s = new StatusNota(nome.trim(), corHex.toUpperCase());
            s = repository.salvar(s);
            ctx.status(201).json(Map.of(
                "sucesso", true,
                "mensagem", "Status criado com sucesso",
                "dados", s
            ));
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique")) {
                ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Status com este nome já existe"));
            } else {
                ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao criar status: " + e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao criar status: " + e.getMessage()));
        }
    }

    public void atualizar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            StatusRequest req = ctx.bodyAsClass(StatusRequest.class);
            String nome = req != null ? req.nome : null;
            String corHex = req != null ? req.corHex : null;

            if (nome == null || nome.trim().isEmpty()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Nome do status é obrigatório"));
                return;
            }
            if (corHex == null || !corHex.matches("^#([0-9a-fA-F]{6})$")) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Cor inválida. Use formato #RRGGBB"));
                return;
            }

            StatusNota s = new StatusNota(nome.trim(), corHex.toUpperCase());
            s.setId(id);
            boolean ok = repository.atualizar(s);
            if (ok) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Status atualizado"));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Status não encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique")) {
                ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Nome de status já existe"));
            } else {
                ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao atualizar status: " + e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao atualizar status: " + e.getMessage()));
        }
    }

    public void deletar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            boolean ok = repository.deletar(id);
            if (ok) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Status excluído"));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Status não encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao excluir status: " + e.getMessage()));
        }
    }
}