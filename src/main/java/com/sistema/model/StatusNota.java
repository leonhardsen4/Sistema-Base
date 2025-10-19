package com.sistema.model;

import java.time.LocalDateTime;

public class StatusNota {
    private Long id;
    private String nome;
    private String corHex; // Formato: #FF5733
    private LocalDateTime dataCriacao;

    // Construtores
    public StatusNota() {}

    public StatusNota(String nome, String corHex) {
        this.nome = nome;
        this.corHex = corHex;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        return "StatusNota{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", corHex='" + corHex + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}