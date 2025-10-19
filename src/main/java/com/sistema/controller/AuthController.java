package com.sistema.controller;

//import com.sistema.model.UsuarioDTO;
import com.sistema.service.AuthService;
import io.javalin.http.Context;
import java.util.Map;

public class AuthController {
    
    private final AuthService authService;
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    // POST /api/auth/login
    public void login(Context ctx) {
        try {
            var body = ctx.bodyAsClass(LoginRequest.class);
            
            var resultado = authService.login(body.email, body.senha);
            
            if (resultado.isPresent()) {
                var dados = resultado.get();
                
                // Adicionar token no cookie (opcional)
                ctx.cookie("auth_token", dados.get("token").toString(), 86400); // 24 horas
                
                ctx.json(Map.of(
                    "sucesso", true,
                    "mensagem", "Login realizado com sucesso",
                    "dados", dados
                ));
            } else {
                ctx.status(401).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Email ou senha incorretos"
                ));
            }
            
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao realizar login: " + e.getMessage()
            ));
        }
    }
    
    // POST /api/auth/logout
    public void logout(Context ctx) {
        try {
            var token = obterToken(ctx);
            
            if (token != null) {
                authService.logout(token);
            }
            
            // Remover cookie
            ctx.removeCookie("auth_token");
            
            ctx.json(Map.of(
                "sucesso", true,
                "mensagem", "Logout realizado com sucesso"
            ));
            
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao realizar logout: " + e.getMessage()
            ));
        }
    }
    
    // GET /api/auth/verificar
    public void verificarSessao(Context ctx) {
        try {
            var token = obterToken(ctx);
            
            if (token == null) {
                ctx.status(401).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Token não fornecido"
                ));
                return;
            }
            
            var usuarioOpt = authService.verificarToken(token);
            
            if (usuarioOpt.isPresent()) {
                ctx.json(Map.of(
                    "sucesso", true,
                    "usuario", usuarioOpt.get()
                ));
            } else {
                ctx.status(401).json(Map.of(
                    "sucesso", false,
                    "mensagem", "Sessão inválida ou expirada"
                ));
            }
            
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao verificar sessão: " + e.getMessage()
            ));
        }
    }
    
    // Obter token do header ou cookie
    private String obterToken(Context ctx) {
        // Tentar obter do header Authorization
        var authHeader = ctx.header("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Tentar obter do cookie
        return ctx.cookie("auth_token");
    }
    
    // Classes auxiliares para request
    public static class LoginRequest {
        public String email;
        public String senha;
    }
}
