package com.sistema.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sistema.model.Usuario;
import com.sistema.model.UsuarioDTO;
import com.sistema.repository.UsuarioRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuarioService {
    
    private final UsuarioRepository repository;
    
    public UsuarioService() {
        this.repository = new UsuarioRepository();
    }
    
    // Listar todos os usuários (sem senha)
    public List<UsuarioDTO> listarTodos() throws SQLException {
        return repository.buscarTodos()
            .stream()
            .map(UsuarioDTO::new)
            .collect(Collectors.toList());
    }
    
    // Buscar por ID (sem senha)
    public Optional<UsuarioDTO> buscarPorId(Long id) throws SQLException {
        return repository.buscarPorId(id)
            .map(UsuarioDTO::new);
    }
    
    // Criar novo usuário
    public UsuarioDTO criar(String nome, String email, String telefone, String senha) throws SQLException {
        // Validações
        validarDados(nome, email, senha);
        
        // Verificar se email já existe
        if (repository.emailExiste(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        
        // Hash da senha
        var senhaHash = BCrypt.withDefaults().hashToString(12, senha.toCharArray());
        
        // Criar usuário
        var usuario = new Usuario(nome, email, telefone, senhaHash);
        var usuarioSalvo = repository.salvar(usuario);
        
        return new UsuarioDTO(usuarioSalvo);
    }
    
    // Atualizar usuário (sem senha)
    public Optional<UsuarioDTO> atualizar(Long id, String nome, String email, String telefone) throws SQLException {
        var usuarioOpt = repository.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Validar dados
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Verificar se email já existe em outro usuário
        var usuarioPorEmail = repository.buscarPorEmail(email);
        if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email já cadastrado por outro usuário");
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);

        repository.atualizar(usuario);

        return Optional.of(new UsuarioDTO(usuario));
    }

    // Atualizar usuário com nova senha (sem exigir senha atual)
    public Optional<UsuarioDTO> atualizarComSenha(Long id, String nome, String email, String telefone, String novaSenha) throws SQLException {
        var usuarioOpt = repository.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Validar dados
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Validar senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }

        // Verificar se email já existe em outro usuário
        var usuarioPorEmail = repository.buscarPorEmail(email);
        if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email já cadastrado por outro usuário");
        }

        // Atualizar dados básicos
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);

        // Hash da nova senha
        var novaSenhaHash = BCrypt.withDefaults().hashToString(12, novaSenha.toCharArray());
        usuario.setSenhaHash(novaSenhaHash);

        repository.atualizar(usuario);

        return Optional.of(new UsuarioDTO(usuario));
    }

    // Atualizar usuário com status ativo/inativo
    public Optional<UsuarioDTO> atualizarComStatus(Long id, String nome, String email, String telefone, boolean ativo) throws SQLException {
        var usuarioOpt = repository.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Validar dados
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Verificar se email já existe em outro usuário
        var usuarioPorEmail = repository.buscarPorEmail(email);
        if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email já cadastrado por outro usuário");
        }

        // Atualizar dados
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setAtivo(ativo);

        repository.atualizar(usuario);

        return Optional.of(new UsuarioDTO(usuario));
    }

    // Atualizar usuário completo (dados + senha + status)
    public Optional<UsuarioDTO> atualizarCompleto(Long id, String nome, String email, String telefone, String novaSenha, boolean ativo) throws SQLException {
        var usuarioOpt = repository.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Validar dados
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Validar senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }

        // Verificar se email já existe em outro usuário
        var usuarioPorEmail = repository.buscarPorEmail(email);
        if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email já cadastrado por outro usuário");
        }

        // Atualizar dados
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setAtivo(ativo);

        // Hash da nova senha
        var novaSenhaHash = BCrypt.withDefaults().hashToString(12, novaSenha.toCharArray());
        usuario.setSenhaHash(novaSenhaHash);

        repository.atualizar(usuario);

        return Optional.of(new UsuarioDTO(usuario));
    }
    
    // Alterar senha
    public boolean alterarSenha(Long id, String senhaAtual, String novaSenha) throws SQLException {
        var usuarioOpt = repository.buscarPorId(id);
        
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        
        var usuario = usuarioOpt.get();
        
        // Verificar senha atual
        var verificador = BCrypt.verifyer();
        var resultado = verificador.verify(senhaAtual.toCharArray(), usuario.getSenhaHash());
        
        if (!resultado.verified) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        
        // Validar nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter no mínimo 6 caracteres");
        }
        
        // Hash da nova senha
        var novaSenhaHash = BCrypt.withDefaults().hashToString(12, novaSenha.toCharArray());
        
        return repository.atualizarSenha(id, novaSenhaHash);
    }
    
    // Deletar usuário
    public boolean deletar(Long id) throws SQLException {
        return repository.desativar(id); // Soft delete
    }
    
    // Autenticar usuário (retorna usuario se senha correta)
    public Optional<Usuario> autenticar(String email, String senha) throws SQLException {
        var usuarioOpt = repository.buscarPorEmail(email);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        var usuario = usuarioOpt.get();

        // Verificar senha primeiro
        var verificador = BCrypt.verifyer();
        var resultado = verificador.verify(senha.toCharArray(), usuario.getSenhaHash());

        if (!resultado.verified) {
            return Optional.empty();
        }

        // Senha correta, agora verifica se usuário está ativo
        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário desativado. Entre em contato com o administrador do sistema.");
        }

        return Optional.of(usuario);
    }
    
    // Validações
    private void validarDados(String nome, String email, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }
    }
}
