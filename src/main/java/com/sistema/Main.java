package com.sistema;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.sistema.config.DatabaseConfig;
import com.sistema.controller.AuthController;
import com.sistema.controller.UsuarioController;
import com.sistema.controller.EtiquetaController;
import com.sistema.controller.StatusNotaController;
import com.sistema.controller.NotaController;
import com.sistema.controller.NotificacaoController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.json.JavalinJackson;

public class Main {
    
    public static void main(String[] args) {
        // Inicializar banco de dados
        DatabaseConfig.inicializar();
        
        // Criar controllers
        var authController = new AuthController();
        var usuarioController = new UsuarioController();
        var etiquetaController = new EtiquetaController();
        var statusController = new StatusNotaController();
        var notaController = new NotaController();
        var notificacaoController = new NotificacaoController();
        
        // Configurar e iniciar Javalin
        var app = Javalin.create(config -> {
            // JSON mapper (Jackson) com suporte a JavaTime (LocalDate/LocalDateTime)
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }));

            // Servir arquivos estáticos (HTML, CSS, JS)
            config.staticFiles.add("/public", Location.CLASSPATH);
            
            // CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
            
            // Logs de desenvolvimento
            config.bundledPlugins.enableDevLogging();
            
            // Configurar servidor
            config.jetty.defaultHost = "0.0.0.0"; // Permite acesso externo
        }).start(7070);
        
        // ====================================================================
        // ROTAS
        // ====================================================================
        
        // Página inicial (redireciona para login)
        app.get("/", ctx -> ctx.redirect("/login.html"));
        
        // Rotas de Autenticação
        app.post("/api/auth/login", authController::login);
        app.post("/api/auth/logout", authController::logout);
        app.get("/api/auth/verificar", authController::verificarSessao);
        
        // Rotas de Usuário
        app.post("/api/usuarios", usuarioController::criar);
        app.get("/api/usuarios", usuarioController::listar);
        app.get("/api/usuarios/{id}", usuarioController::buscarPorId);
        app.put("/api/usuarios/{id}", usuarioController::atualizar);
        app.delete("/api/usuarios/{id}", usuarioController::deletar);

        // Etiquetas
        app.get("/api/etiquetas", etiquetaController::listar);
        app.get("/api/etiquetas/{id}", etiquetaController::buscarPorId);
        app.post("/api/etiquetas", etiquetaController::criar);
        app.put("/api/etiquetas/{id}", etiquetaController::atualizar);
        app.delete("/api/etiquetas/{id}", etiquetaController::deletar);

        // Status
        app.get("/api/status", statusController::listar);
        app.get("/api/status/{id}", statusController::buscarPorId);
        app.post("/api/status", statusController::criar);
        app.put("/api/status/{id}", statusController::atualizar);
        app.delete("/api/status/{id}", statusController::deletar);

        // Notas
        app.get("/api/notas", notaController::listar);
        app.get("/api/notas/{id}", notaController::buscarPorId);
        app.get("/api/notas/etiqueta/{etiquetaId}", notaController::buscarPorEtiqueta);
        app.post("/api/notas", notaController::criar);
        app.put("/api/notas/{id}", notaController::atualizar);
        app.delete("/api/notas/{id}", notaController::deletar);

        // Notificações
        app.get("/api/notificacoes/alertas", notificacaoController::gerarAlertas);
        
        // Tratamento de erros
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(java.util.Map.of(
                "sucesso", false,
                "mensagem", "Erro interno do servidor: " + e.getMessage()
            ));
        });
        
        app.error(404, ctx -> {
            ctx.json(java.util.Map.of(
                "sucesso", false,
                "mensagem", "Rota não encontrada"
            ));
        });
        
        // Informações de inicialização
        mostrarInformacoes();
    }
    
    private static void mostrarInformacoes() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 SISTEMA INICIADO COM SUCESSO!");
        System.out.println("=".repeat(70));
        
        try {
            var ip = java.net.InetAddress.getLocalHost().getHostAddress();
            var hostname = java.net.InetAddress.getLocalHost().getHostName();
            
            System.out.println("\n📍 ACESSO LOCAL:");
            System.out.println("   http://localhost:7070");
            System.out.println("\n🌐 ACESSO NA REDE:");
            System.out.println("   http://" + ip + ":7070");
            System.out.println("   http://" + hostname + ":7070");
            System.out.println("\n💾 Banco de dados: database.db");
            System.out.println("\n⚠️  Para PARAR: pressione CTRL+C");
            System.out.println("=".repeat(70) + "\n");
            
        } catch (Exception e) {
            System.err.println("Erro ao obter informações de rede: " + e.getMessage());
        }
    }
}
