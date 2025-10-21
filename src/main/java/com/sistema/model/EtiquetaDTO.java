package com.sistema.model;

import java.time.LocalDateTime;

public class EtiquetaDTO {
    private Long id;
    private String nome;
    private LocalDateTime dataCriacao;
    private Integer contador; // Número de notas com esta etiqueta

    public EtiquetaDTO() {}

    public EtiquetaDTO(Long id, String nome, LocalDateTime dataCriacao, Integer contador) {
        this.id = id;
        this.nome = nome;
        this.dataCriacao = dataCriacao;
        this.contador = contador;
    }

    // Construtor para criar DTO a partir de Etiqueta
    public EtiquetaDTO(Etiqueta etiqueta, Integer contador) {
        this.id = etiqueta.getId();
        this.nome = etiqueta.getNome();
        this.dataCriacao = etiqueta.getDataCriacao();
        this.contador = contador;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Integer getContador() {
        return contador;
    }

    public void setContador(Integer contador) {
        this.contador = contador;
    }
}
