package com.sistema.model;

/**
 * DTO (Data Transfer Object) para transferir dados de usuário
 * sem expor a senha hash para a API/frontend
 */
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String dataCriacao;
    private boolean ativo;
    
    // Construtor vazio (necessário para JSON)
    public UsuarioDTO() {}
    
    // Construtor que converte Usuario para UsuarioDTO
    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.dataCriacao = usuario.getDataCriacao() != null 
            ? usuario.getDataCriacao().toString() 
            : null;
        this.ativo = usuario.isAtivo();
    }
    
    // Getters e Setters
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", dataCriacao='" + dataCriacao + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
