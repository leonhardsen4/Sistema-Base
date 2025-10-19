package com.sistema.controller;

import com.sistema.service.NotaService;
import io.javalin.http.Context;

import java.util.Map;

public class NotaController {
    private final NotaService service = new NotaService();

    public void listar(Context ctx) {
        try {
            var notas = service.listarTodas();
            ctx.json(Map.of("sucesso", true, "dados", notas));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao listar notas: " + e.getMessage()));
        }
    }

    public void buscarPorId(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            var notaOpt = service.buscarPorId(id);
            if (notaOpt.isPresent()) {
                ctx.json(Map.of("sucesso", true, "dados", notaOpt.get()));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Nota não encontrada"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao buscar nota: " + e.getMessage()));
        }
    }

    public void buscarPorEtiqueta(Context ctx) {
        try {
            var etiquetaId = Long.parseLong(ctx.pathParam("etiquetaId"));
            var notas = service.listarPorEtiqueta(etiquetaId);
            ctx.json(Map.of("sucesso", true, "dados", notas));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao buscar notas por etiqueta: " + e.getMessage()));
        }
    }

    public void criar(Context ctx) {
        try {
            var req = ctx.bodyAsClass(NotaRequest.class);
            if (req == null || req.titulo == null || req.titulo.isBlank() ||
                req.etiquetaId == null || req.statusId == null || req.prazoFinal == null || req.prazoFinal.isBlank()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Dados obrigatórios ausentes"));
                return;
            }
            var dto = service.criar(req.etiquetaId, req.statusId, req.titulo, req.conteudo, req.prazoFinal);
            ctx.json(Map.of("sucesso", true, "mensagem", "Nota criada com sucesso", "dados", dto));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao criar nota: " + e.getMessage()));
        }
    }

    public void atualizar(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            var req = ctx.bodyAsClass(NotaRequest.class);
            if (req == null || req.titulo == null || req.titulo.isBlank() ||
                req.etiquetaId == null || req.statusId == null || req.prazoFinal == null || req.prazoFinal.isBlank()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Dados obrigatórios ausentes"));
                return;
            }
            var dtoOpt = service.atualizar(id, req.etiquetaId, req.statusId, req.titulo, req.conteudo, req.prazoFinal);
            if (dtoOpt.isPresent()) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Nota atualizada com sucesso", "dados", dtoOpt.get()));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Nota não encontrada"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao atualizar nota: " + e.getMessage()));
        }
    }

    public void deletar(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            var ok = service.deletar(id);
            if (ok) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Nota deletada com sucesso"));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Nota não encontrada"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao deletar nota: " + e.getMessage()));
        }
    }

    // DTO de request
    public static class NotaRequest {
        public String titulo;
        public Long etiquetaId;
        public Long statusId;
        public String prazoFinal; // ISO yyyy-MM-dd
        public String conteudo;
    }
}