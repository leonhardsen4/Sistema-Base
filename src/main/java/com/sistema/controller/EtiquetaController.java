package com.sistema.controller;

import com.sistema.model.Etiqueta;
import com.sistema.model.EtiquetaDTO;
import com.sistema.repository.EtiquetaRepository;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EtiquetaController {
    private final EtiquetaRepository repository = new EtiquetaRepository();

    public void listar(Context ctx) {
        try {
            List<Etiqueta> etiquetas = repository.buscarTodos();
            Map<Long, Integer> contadores = repository.contarNotasPorTodasEtiquetas();

            // Converter para DTOs com contadores
            List<EtiquetaDTO> dtos = new ArrayList<>();
            for (Etiqueta etiqueta : etiquetas) {
                Integer contador = contadores.getOrDefault(etiqueta.getId(), 0);
                dtos.add(new EtiquetaDTO(etiqueta, contador));
            }

            ctx.json(Map.of(
                "sucesso", true,
                "dados", dtos
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao listar etiquetas: " + e.getMessage()
            ));
        }
    }

    public void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            var etiquetaOpt = repository.buscarPorId(id);
            if (etiquetaOpt.isPresent()) {
                ctx.json(Map.of("sucesso", true, "dados", etiquetaOpt.get()));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Etiqueta não encontrada"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao buscar etiqueta: " + e.getMessage()));
        }
    }

    public static class EtiquetaRequest {
        public String nome;
    }

    public void criar(Context ctx) {
        try {
            EtiquetaRequest req = ctx.bodyAsClass(EtiquetaRequest.class);
            String nome = req != null ? req.nome : null;
            if (nome == null || nome.trim().isEmpty()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Nome da etiqueta é obrigatório"));
                return;
            }
            Etiqueta etiqueta = new Etiqueta(nome.trim());
            etiqueta = repository.salvar(etiqueta);
            ctx.status(201).json(Map.of(
                "sucesso", true,
                "mensagem", "Etiqueta criada com sucesso",
                "dados", etiqueta
            ));
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique")) {
                ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Etiqueta já existe"));
            } else {
                ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao criar etiqueta: " + e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao criar etiqueta: " + e.getMessage()));
        }
    }

    public void atualizar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            EtiquetaRequest req = ctx.bodyAsClass(EtiquetaRequest.class);
            String nome = req != null ? req.nome : null;
            if (nome == null || nome.trim().isEmpty()) {
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Nome da etiqueta é obrigatório"));
                return;
            }
            Etiqueta et = new Etiqueta(nome.trim());
            et.setId(id);
            boolean ok = repository.atualizar(et);
            if (ok) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Etiqueta atualizada"));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Etiqueta não encontrada"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique")) {
                ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Nome de etiqueta já existe"));
            } else {
                ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao atualizar etiqueta: " + e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao atualizar etiqueta: " + e.getMessage()));
        }
    }

    public void deletar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            boolean ok = repository.deletar(id);
            if (ok) {
                ctx.json(Map.of("sucesso", true, "mensagem", "Etiqueta excluída"));
            } else {
                ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Etiqueta não encontrada"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "ID inválido"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("sucesso", false, "mensagem", "Erro ao excluir etiqueta: " + e.getMessage()));
        }
    }
}