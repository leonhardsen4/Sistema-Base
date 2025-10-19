package com.sistema.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class NotaDTO {
    private Long id;
    private String titulo;
    private String conteudo;
    private String dataCriacao;      // Formato: dd/MM/yyyy HH:mm
    private String dataAtualizacao;  // Formato: dd/MM/yyyy HH:mm
    private String prazoFinal;       // Formato: dd/MM/yyyy
    private Long etiquetaId;
    private String etiquetaNome;
    private Long statusId;
    private String statusNome;
    private String statusCor;
    private Integer diasRestantes;   // Para cálculo de alertas

    public NotaDTO() {}

    // Construtor que converte Nota para NotaDTO com dados relacionados
    public NotaDTO(Nota nota, Etiqueta etiqueta, StatusNota status) {
        this.id = nota.getId();
        this.titulo = nota.getTitulo();
        this.conteudo = nota.getConteudo();

        // Formatar datas para padrão brasileiro
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        this.dataCriacao = nota.getDataCriacao() != null ? nota.getDataCriacao().format(dateTimeFormatter) : null;
        this.dataAtualizacao = nota.getDataAtualizacao() != null ? nota.getDataAtualizacao().format(dateTimeFormatter) : null;
        this.prazoFinal = nota.getPrazoFinal() != null ? nota.getPrazoFinal().format(dateFormatter) : null;

        // Dados da etiqueta
        if (etiqueta != null) {
            this.etiquetaId = etiqueta.getId();
            this.etiquetaNome = etiqueta.getNome();
        }

        // Dados do status
        if (status != null) {
            this.statusId = status.getId();
            this.statusNome = status.getNome();
            this.statusCor = status.getCorHex();
        }

        // Calcular dias restantes
        this.diasRestantes = nota.getPrazoFinal() != null
                ? (int) ChronoUnit.DAYS.between(LocalDate.now(), nota.getPrazoFinal())
                : null;
    }

    // Getters e Setters completos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(String dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getPrazoFinal() { return prazoFinal; }
    public void setPrazoFinal(String prazoFinal) { this.prazoFinal = prazoFinal; }

    public Long getEtiquetaId() { return etiquetaId; }
    public void setEtiquetaId(Long etiquetaId) { this.etiquetaId = etiquetaId; }

    public String getEtiquetaNome() { return etiquetaNome; }
    public void setEtiquetaNome(String etiquetaNome) { this.etiquetaNome = etiquetaNome; }

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getStatusNome() { return statusNome; }
    public void setStatusNome(String statusNome) { this.statusNome = statusNome; }

    public String getStatusCor() { return statusCor; }
    public void setStatusCor(String statusCor) { this.statusCor = statusCor; }

    public Integer getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(Integer diasRestantes) { this.diasRestantes = diasRestantes; }
}