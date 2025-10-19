package com.sistema.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Nota {
    private Long id;
    private Long etiquetaId;
    private Long statusId;
    private String titulo;
    private String conteudo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDate prazoFinal; // LocalDate para armazenar apenas data

    // Construtor vazio
    public Nota() {}

    // Construtor completo
    public Nota(Long etiquetaId, Long statusId, String titulo, String conteudo, LocalDate prazoFinal) {
        this.etiquetaId = etiquetaId;
        this.statusId = statusId;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.prazoFinal = prazoFinal;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEtiquetaId() { return etiquetaId; }
    public void setEtiquetaId(Long etiquetaId) { this.etiquetaId = etiquetaId; }

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDate getPrazoFinal() { return prazoFinal; }
    public void setPrazoFinal(LocalDate prazoFinal) { this.prazoFinal = prazoFinal; }

    @Override
    public String toString() {
        return "Nota{" +
                "id=" + id +
                ", etiquetaId=" + etiquetaId +
                ", statusId=" + statusId +
                ", titulo='" + titulo + '\'' +
                ", conteudo='" + conteudo + '\'' +
                ", dataCriacao=" + dataCriacao +
                ", dataAtualizacao=" + dataAtualizacao +
                ", prazoFinal=" + prazoFinal +
                '}';
    }
}