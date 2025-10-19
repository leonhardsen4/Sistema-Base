package com.sistema.controller;

import com.sistema.service.UsuarioService;
import io.javalin.http.Context;
import java.util.Map;

public class UsuarioController {
    
    private final UsuarioService service;
    
    public UsuarioController() {
        this.service = new UsuarioService();
    }
    
    // GET /api/usuarios - Listar todos
    public void listar(Context ctx) {
        try {
            var usuarios = service.listarTodos();
            ctx.json(Map.of(
                "sucesso", true,
                "dados", usuarios
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao listar usuários: " + e.getMessage()
            ));
        }
    }
    
    // GET /api/usuarios/{id} - Buscar por ID
    public void buscarPorId(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            var usuarioOpt = service.buscarPorId(id);
            
            if (usuarioOpt.isPresent()) {
                ctx.json(Map.of(
                    "sucesso", true,
                    "dados", usuarioOpt.get()
                ));
            } else {
                ctx.status(404).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Usuário não encontrado"
                ));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "sucesso", false,
                "mensagem", "ID inválido"
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao buscar usuário: " + e.getMessage()
            ));
        }
    }
    
    // POST /api/usuarios - Criar novo
    public void criar(Context ctx) {
        try {
            var body = ctx.bodyAsClass(UsuarioRequest.class);
            
            var usuario = service.criar(
                body.nome,
                body.email,
                body.telefone,
                body.senha
            );
            
            ctx.status(201).json(Map.of(
                "sucesso", true,
                "mensagem", "Usuário criado com sucesso",
                "dados", usuario
            ));
            
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                "sucesso", false,
                "mensagem", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao criar usuário: " + e.getMessage()
            ));
        }
    }
    
    // PUT /api/usuarios/{id} - Atualizar
    public void atualizar(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(UsuarioUpdateRequest.class);
            
            var usuarioOpt = service.atualizar(id, body.nome, body.email, body.telefone);
            
            if (usuarioOpt.isPresent()) {
                ctx.json(Map.of(
                    "sucesso", true,
                    "mensagem", "Usuário atualizado com sucesso",
                    "dados", usuarioOpt.get()
                ));
            } else {
                ctx.status(404).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Usuário não encontrado"
                ));
            }
            
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                "sucesso", false,
                "mensagem", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao atualizar usuário: " + e.getMessage()
            ));
        }
    }
    
    // DELETE /api/usuarios/{id} - Deletar
    public void deletar(Context ctx) {
        try {
            var id = Long.parseLong(ctx.pathParam("id"));
            
            if (service.deletar(id)) {
                ctx.json(Map.of(
                    "sucesso", true,
                    "mensagem", "Usuário deletado com sucesso"
                ));
            } else {
                ctx.status(404).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Usuário não encontrado"
                ));
            }
            
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao deletar usuário: " + e.getMessage()
            ));
        }
    }
    
    // Classes auxiliares para request
    public static class UsuarioRequest {
        public String nome;
        public String email;
        public String telefone;
        public String senha;
    }
    
    public static class UsuarioUpdateRequest {
        public String nome;
        public String email;
        public String telefone;
    }
}
