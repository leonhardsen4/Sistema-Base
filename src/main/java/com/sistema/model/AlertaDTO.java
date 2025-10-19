package com.sistema.model;

import java.util.List;

public class AlertaDTO {
    private String nivel;        // "critico", "atrasado", "urgente", "atencao", "normal"
    private String cor;          // #000000, #DC2626, #EA580C, #FBBF24
    private String corTexto;     // #FFFFFF ou #000000
    private String mensagem;
    private Integer quantidade;
    private List<NotaDTO> notas;

    public AlertaDTO() {}

    // Construtor completo
    public AlertaDTO(String nivel, String cor, String corTexto, String mensagem, Integer quantidade, List<NotaDTO> notas) {
        this.nivel = nivel;
        this.cor = cor;
        this.corTexto = corTexto;
        this.mensagem = mensagem;
        this.quantidade = quantidade;
        this.notas = notas;
    }

    // Getters e Setters
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getCorTexto() { return corTexto; }
    public void setCorTexto(String corTexto) { this.corTexto = corTexto; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public List<NotaDTO> getNotas() { return notas; }
    public void setNotas(List<NotaDTO> notas) { this.notas = notas; }
}