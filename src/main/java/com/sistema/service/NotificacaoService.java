package com.sistema.service;

import com.sistema.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class NotificacaoService {
    
    private final NotaService notaService;
    
    public NotificacaoService() {
        this.notaService = new NotaService();
    }
    
    /**
     * Gerar alertas agrupados por urgência
     * Retorna lista ordenada por prioridade (mais urgente primeiro)
     */
    public List<AlertaDTO> gerarAlertas() throws Exception {
        List<AlertaDTO> alertas = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        
        // Buscar apenas notas pendentes (status diferente de "Resolvido" e "Cancelado")
        List<NotaDTO> notasPendentes = notaService.listarTodas()
            .stream()
            .filter(n -> n.getStatusNome() != null
                      && !n.getStatusNome().equalsIgnoreCase("Resolvido")
                      && !n.getStatusNome().equalsIgnoreCase("Cancelado"))
            .collect(Collectors.toList());
        
        // 1. CRÍTICO - Atrasadas (prazo vencido)
        List<NotaDTO> atrasadas = notasPendentes.stream()
            .filter(n -> n.getDiasRestantes() != null && n.getDiasRestantes() < 0)
            .sorted(Comparator.comparing(NotaDTO::getDiasRestantes))
            .collect(Collectors.toList());
        
        if (!atrasadas.isEmpty()) {
            alertas.add(new AlertaDTO(
                "critico",
                "#000000", // Preto
                "#DC2626", // Texto vermelho
                "⚠️ CRÍTICO: " + atrasadas.size() + " nota(s) atrasada(s)!",
                atrasadas.size(),
                atrasadas 
            ));
        }
        
        // 2. URGENTE - Vence em 1 dia
        List<NotaDTO> urgente = notasPendentes.stream()
            .filter(n -> n.getDiasRestantes() != null && n.getDiasRestantes() >= 0 && n.getDiasRestantes() <= 1)
            .sorted(Comparator.comparing(NotaDTO::getPrazoFinal))
            .collect(Collectors.toList());
        
        if (!urgente.isEmpty()) {
            alertas.add(new AlertaDTO(
                "urgente",
                "#DC2626", // Vermelho
                "#FFFFFF", // Texto branco
                "🔴 URGENTE: " + urgente.size() + " nota(s) vencem hoje ou amanhã!",
                urgente.size(),
                urgente 
            ));
        }
        
        // 3. ATENÇÃO - Vence em 2-3 dias
        List<NotaDTO> atencao = notasPendentes.stream()
            .filter(n -> n.getDiasRestantes() != null && n.getDiasRestantes() >= 2 && n.getDiasRestantes() <= 3)
            .sorted(Comparator.comparing(NotaDTO::getPrazoFinal))
            .collect(Collectors.toList());
        
        if (!atencao.isEmpty()) {
            alertas.add(new AlertaDTO(
                "atencao",
                "#EA580C", // Laranja
                "#FFFFFF", // Texto branco
                "🟠 ATENÇÃO: " + atencao.size() + " nota(s) vencem em 2-3 dias",
                atencao.size(),
                atencao 
            ));
        }
        
        // 4. AVISO - Vence em 4-5 dias
        List<NotaDTO> aviso = notasPendentes.stream()
            .filter(n -> n.getDiasRestantes() != null && n.getDiasRestantes() >= 4 && n.getDiasRestantes() <= 5)
            .sorted(Comparator.comparing(NotaDTO::getPrazoFinal))
            .collect(Collectors.toList());
        
        if (!aviso.isEmpty()) {
            alertas.add(new AlertaDTO(
                "aviso",
                "#FBBF24", // Amarelo
                "#000000", // Texto preto
                "🟡 AVISO: " + aviso.size() + " nota(s) vencem em 4-5 dias",
                aviso.size(),
                aviso 
            ));
        }
        
        return alertas;
    } 
}