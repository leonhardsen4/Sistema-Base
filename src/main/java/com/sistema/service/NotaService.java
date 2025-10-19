package com.sistema.service;

import com.sistema.model.*;
import com.sistema.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class NotaService {
    private final NotaRepository notaRepository = new NotaRepository();
    private final EtiquetaRepository etiquetaRepository = new EtiquetaRepository();
    private final StatusNotaRepository statusRepository = new StatusNotaRepository();

    public List<NotaDTO> listarTodas() throws Exception {
        var notas = notaRepository.buscarTodos();
        var etiquetas = etiquetaRepository.buscarTodos().stream()
                .collect(Collectors.toMap(Etiqueta::getId, e -> e));
        var status = statusRepository.buscarTodos().stream()
                .collect(Collectors.toMap(StatusNota::getId, s -> s));
        var dtos = new ArrayList<NotaDTO>();
        for (var n : notas) {
            dtos.add(new NotaDTO(n, etiquetas.get(n.getEtiquetaId()), status.get(n.getStatusId())));
        }
        return dtos;
    }

    public List<NotaDTO> listarPorEtiqueta(Long etiquetaId) throws Exception {
        var notas = notaRepository.buscarPorEtiqueta(etiquetaId);
        var etiquetasOpt = etiquetaRepository.buscarPorId(etiquetaId);
        var etiquetas = new HashMap<Long, Etiqueta>();
        etiquetasOpt.ifPresent(e -> etiquetas.put(e.getId(), e));
        var status = statusRepository.buscarTodos().stream()
                .collect(Collectors.toMap(StatusNota::getId, s -> s));
        var dtos = new ArrayList<NotaDTO>();
        for (var n : notas) {
            dtos.add(new NotaDTO(n, etiquetas.get(n.getEtiquetaId()), status.get(n.getStatusId())));
        }
        return dtos;
    }

    public Optional<NotaDTO> buscarPorId(Long id) throws Exception {
        var notaOpt = notaRepository.buscarPorId(id);
        if (notaOpt.isEmpty()) return Optional.empty();
        var nota = notaOpt.get();
        var etiquetaOpt = etiquetaRepository.buscarPorId(nota.getEtiquetaId());
        var statusOpt = statusRepository.buscarPorId(nota.getStatusId());
        return Optional.of(new NotaDTO(nota, etiquetaOpt.orElse(null), statusOpt.orElse(null)));
    }

    public NotaDTO criar(Long etiquetaId, Long statusId, String titulo, String conteudo, String prazoFinalISO) throws Exception {
        var nota = new Nota();
        nota.setEtiquetaId(etiquetaId);
        nota.setStatusId(statusId);
        nota.setTitulo(titulo);
        nota.setConteudo(conteudo);
        var prazo = LocalDate.parse(prazoFinalISO, DateTimeFormatter.ISO_LOCAL_DATE);
        nota.setPrazoFinal(prazo);
        nota = notaRepository.salvar(nota);
        var etiquetaOpt = etiquetaRepository.buscarPorId(etiquetaId);
        var statusOpt = statusRepository.buscarPorId(statusId);
        return new NotaDTO(nota, etiquetaOpt.orElse(null), statusOpt.orElse(null));
    }

    public Optional<NotaDTO> atualizar(Long id, Long etiquetaId, Long statusId, String titulo, String conteudo, String prazoFinalISO) throws Exception {
        var notaOpt = notaRepository.buscarPorId(id);
        if (notaOpt.isEmpty()) return Optional.empty();
        var nota = notaOpt.get();
        nota.setEtiquetaId(etiquetaId);
        nota.setStatusId(statusId);
        nota.setTitulo(titulo);
        nota.setConteudo(conteudo);
        var prazo = LocalDate.parse(prazoFinalISO, DateTimeFormatter.ISO_LOCAL_DATE);
        nota.setPrazoFinal(prazo);
        var ok = notaRepository.atualizar(nota);
        if (!ok) return Optional.empty();
        var etiquetaOpt = etiquetaRepository.buscarPorId(etiquetaId);
        var statusOpt = statusRepository.buscarPorId(statusId);
        return Optional.of(new NotaDTO(nota, etiquetaOpt.orElse(null), statusOpt.orElse(null)));
    }

    public boolean deletar(Long id) throws Exception {
        return notaRepository.deletar(id);
    }
}