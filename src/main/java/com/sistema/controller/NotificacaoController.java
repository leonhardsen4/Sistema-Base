package com.sistema.controller;

import io.javalin.http.Context;
import com.sistema.service.NotificacaoService;
import java.util.Map;

public class NotificacaoController {
    private final NotificacaoService notificacaoService = new NotificacaoService();

    public void gerarAlertas(Context ctx) {
        try {
            var alertas = notificacaoService.gerarAlertas();
            ctx.json(Map.of("sucesso", true, "dados", alertas));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "sucesso", false,
                "mensagem", "Erro ao gerar alertas: " + e.getMessage()
            ));
        }
    }
}