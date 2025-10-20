// Utilidades
function $(sel) { return document.querySelector(sel); }
function $all(sel) { return Array.from(document.querySelectorAll(sel)); }
const api = {
  alertas: '/api/notificacoes/alertas',
  etiquetas: '/api/etiquetas',
  notas: '/api/notas'
};

// Mapeamento de cores por nível (fallback quando cor/corTexto não vierem do backend)
const nivelClasses = {
  'CRÍTICO': 'critico',
  'URGENTE': 'urgente',
  'ATENÇÃO': 'atencao',
  'AVISO': 'aviso'
};

// Estado simples
const state = {
  etiquetas: [],
  notas: [],
  alertas: []
};

function getToken() {
  try {
    return localStorage.getItem('auth_token') || '';
  } catch {
    return '';
  }
}

let ordenacaoAtual = { coluna: null, direcao: 'asc' };

// Inicialização
 document.addEventListener('DOMContentLoaded', async () => {
  // Verificar autenticação
  if (!verificarAutenticacao()) {
    window.location.href = '/login.html';
    return;
  }

  // Handlers de UI
  $('#toggleAlertas')?.addEventListener('click', toggleAlertas);
  $('#btnNovaEtiqueta')?.addEventListener('click', modalNovaEtiqueta);
  $('#btnNovaNota')?.addEventListener('click', modalNovaNota);

  // Inicializar datepicker
  try {
    flatpickr('.datepicker', {
      locale: 'pt',
      dateFormat: 'd/m/Y',
      minDate: 'today'
    });
  } catch(e) { /* se CDN não carregar, ignora */ }

  // Event listeners
  inicializarEventListeners();

  // Carregar dados em paralelo
  await Promise.all([
    carregarAlertas(),
    carregarEtiquetas(),
    carregarNotas(),
    carregarStatusNoSelect()
  ]);
 
   // Eventos para checkboxes das colunas
   document.querySelectorAll('#menuColunas input[type="checkbox"]').forEach(checkbox => {
     checkbox.addEventListener('change', function() {
       const coluna = this.dataset.coluna;
       const mostrar = this.checked;
 
       // Toggle header
       const th = document.querySelector(`th.col-${coluna}`);
       if (th) th.style.display = mostrar ? '' : 'none';
 
       // Toggle células
       const idx = getColIndex(coluna);
       if (!idx) return;
       document.querySelectorAll(`#corpoTabela td:nth-child(${idx})`).forEach(td => {
         td.style.display = mostrar ? '' : 'none';
       });
     });
   });
 
   // Eventos de ordenação nos headers
   document.querySelector('th.col-titulo')?.addEventListener('click', () => ordenar('titulo'));
   document.querySelector('th.col-prazo')?.addEventListener('click', () => ordenar('prazo'));
 
   // Form de nota e preview de status
  document.getElementById('formNota')?.addEventListener('submit', salvarNota);
  document.getElementById('notaStatus')?.addEventListener('change', atualizarPreviewCor);
  // carregamentos executados em Promise.all no início do DOMContentLoaded
 });

// ===== Alertas =====
async function carregarAlertas() {
  const container = document.getElementById('alertasContainer');
  const section = document.getElementById('alertasSection');
  const countEl = document.getElementById('alertasCount');

  try {
    const response = await fetch(api.alertas, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });
    const data = await response.json();
    const alertas = Array.isArray(data) ? data : (data.sucesso ? (data.dados || []) : []);

    // Se não houver alertas, colapsar a seção
    if (!alertas.length) {
      container.innerHTML = '<div style="text-align: center; padding: 15px; color: #10b981; font-size: 14px;">✅ Tudo em dia!</div>';
      section.classList.remove('expanded');
      if (countEl) countEl.textContent = '';
      return;
    }

    // Se houver alertas, expandir a seção
    section.classList.add('expanded');
    if (countEl) countEl.textContent = `(${alertas.length})`;

    container.innerHTML = alertas.map(alerta => {
      const cor = alerta.cor || '#FBBF24';
      const corTexto = alerta.corTexto || idealTextColor(cor);
      const notasHtml = (alerta.notas || []).map(nota => `
        <div class="alerta-nota" onclick="abrirNota(${nota.id})">
          <span class="nota-titulo">${nota.titulo}</span>
          <span class="nota-prazo">${nota.prazoFinal || ''}</span>
          ${typeof nota.diasRestantes === 'number' ? `<span class="nota-dias">(${Math.abs(nota.diasRestantes)} dias)</span>` : ''}
        </div>
      `).join('');
      const nivelClasse = `alerta-${(alerta.nivel || '').toLowerCase()}`;
      const mensagem = alerta.mensagem || `${alerta.nivel || 'AVISO'}`;
      return `
        <div class="alerta ${nivelClasse}" style="background: ${cor}; color: ${corTexto};">
          <div class="alerta-header" onclick="toggleAlerta(this); event.stopPropagation();">
            <span>${mensagem}</span>
            <button class="alerta-toggle" style="color: inherit;">▼</button>
          </div>
          <div class="alerta-conteudo" style="display:none;">${notasHtml || '<div class="vazio">Sem notas neste alerta.</div>'}</div>
        </div>
      `;
    }).join('');
  } catch (error) {
    console.error('Erro ao carregar alertas:', error);
    container.innerHTML = '<div class="vazio">Não foi possível carregar alertas.</div>';
    section.classList.remove('expanded');
  }
}

// Função auxiliar para ideal text color (se não existir)
function idealTextColor(bgColor) {
  const hex = bgColor.replace('#', '');
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  return brightness > 155 ? '#000000' : '#FFFFFF';
}

function toggleAlerta(headerEl) {
  const card = headerEl.parentElement;
  const conteudo = card.querySelector('.alerta-conteudo');
  const toggle = headerEl.querySelector('.alerta-toggle');
  const visible = conteudo.style.display !== 'none';
  conteudo.style.display = visible ? 'none' : 'block';
  if (toggle) toggle.textContent = visible ? '▼' : '▲';
}

function renderAlertas() {
  const container = $('#alertasContainer');
  container.innerHTML = '';
  if (!state.alertas.length) {
    container.innerHTML = '<div class="vazio">Nenhum alerta no momento.</div>';
    return;
  }
  state.alertas.forEach((alerta) => {
    const nivel = alerta.nivel || 'AVISO';
    const classe = nivelClasses[nivel] || 'aviso';
    const quantidade = alerta.quantidade ?? (alerta.notas ? alerta.notas.length : 0);
    const texto = alerta.mensagem || `${nivel}: ${quantidade} notas`;

    const card = document.createElement('div');
    card.className = `alerta ${classe}`;
    card.innerHTML = `
      <div class="alerta-header">
        <span class="alerta-icone">⚠️</span>
        <span class="alerta-texto">${texto}</span>
        <button class="alerta-toggle">▼</button>
      </div>
      <div class="alerta-conteudo" style="display:none;"></div>
    `;

    const conteudo = card.querySelector('.alerta-conteudo');
    const notas = alerta.notas || [];
    if (notas.length) {
      notas.forEach((n) => {
        const item = document.createElement('div');
        item.className = 'alerta-nota';
        item.textContent = `📄 ${n.titulo} - ${n.prazoFinal ? `Prazo ${n.prazoFinal}` : ''}`;
        item.addEventListener('click', () => abrirNota(n.id));
        conteudo.appendChild(item);
      });
    } else {
      conteudo.innerHTML = '<div class="vazio">Sem notas neste alerta.</div>';
    }

    card.addEventListener('click', () => expandirAlerta(card));
    container.appendChild(card);
  });
}

function expandirAlerta(el) {
  const content = el.querySelector('.alerta-conteudo');
  const toggle = el.querySelector('.alerta-toggle');
  const visible = content.style.display !== 'none';
  content.style.display = visible ? 'none' : 'block';
  if (toggle) toggle.textContent = visible ? '▼' : '▲';
}

function toggleAlertas() {
  const container = $('#alertasContainer');
  const btn = $('#toggleAlertas');
  const hidden = container.style.display === 'none';
  container.style.display = hidden ? 'grid' : 'none';
  btn.textContent = hidden ? 'Colapsar ▲' : 'Expandir ▼';
  btn.setAttribute('aria-expanded', hidden ? 'true' : 'false');
}

// ===== Etiquetas =====
async function carregarEtiquetas() {
  try {
    const resp = await fetch(api.etiquetas);
    const json = await resp.json();
    if (!json.sucesso) throw new Error(json.mensagem || 'Falha ao obter etiquetas');
    state.etiquetas = json.dados || [];
    renderEtiquetas(state.etiquetas);
    preencherEtiquetasNoSelect();
  } catch (e) {
    console.error('Erro etiquetas:', e);
    $('#listaEtiquetas').innerHTML = '<div class="vazio">Não foi possível carregar etiquetas.</div>';
  }
}

function renderEtiquetas(items) {
  const lista = $('#listaEtiquetas');
  lista.innerHTML = '';
  if (!items.length) {
    lista.innerHTML = '<div style="text-align: center; padding: 20px; color: #6b7280; font-size: 13px;">Nenhuma etiqueta encontrada.</div>';
    return;
  }
  items.forEach((et) => {
    const el = document.createElement('div');
    el.className = 'etiqueta-item';
    el.dataset.id = et.id;
    el.innerHTML = `
      <span class="etiqueta-checkbox">☐</span>
      <span class="etiqueta-nome">${et.nome || 'Sem nome'}</span>
      <span class="etiqueta-contador">(${et.contador ?? 0})</span>
      <button class="btn-icone-small" onclick="editarEtiqueta(${et.id}, '${(et.nome || '').replace(/'/g, "\\'")}'); event.stopPropagation();" title="Editar">✏️</button>
      <button class="btn-icone-small" onclick="excluirEtiqueta(${et.id}); event.stopPropagation();" title="Excluir">🗑️</button>
    `;
    el.addEventListener('click', () => selecionarEtiqueta(et.id));
    lista.appendChild(el);
  });
}

function filtrarEtiquetas(term) {
  const etiquetas = document.querySelectorAll('.etiqueta-item');
  const busca = (term || '').toLowerCase();
  etiquetas.forEach(etiq => {
    const nomeEl = etiq.querySelector('.etiqueta-nome');
    const nome = (nomeEl?.textContent || '').toLowerCase();
    etiq.style.display = nome.includes(busca) ? 'grid' : 'none';
  });
}

function selecionarEtiqueta(id) {
  // Carregar notas por etiqueta (quando backend estiver pronto)
  alert(`Etiqueta ${id} selecionada – implementar carregamento de notas por etiqueta.`);
}

function excluirEtiqueta(id) {
  const ok = confirm('Excluir etiqueta?');
  if (!ok) return;
  fetch(`${api.etiquetas}/${id}`, { method: 'DELETE' })
    .then(r => r.json())
    .then(j => {
      if (!j.sucesso) throw new Error(j.mensagem || 'Falha ao excluir');
      state.etiquetas = state.etiquetas.filter(e => e.id !== id);
      renderEtiquetas(state.etiquetas);
    })
    .catch(e => alert('Erro ao excluir: ' + e.message));
}

function modalNovaEtiqueta() {
  const nome = prompt('Nome da etiqueta:');
  if (!nome) return;
  fetch(api.etiquetas, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome })
  })
    .then(r => r.json())
    .then(j => {
      if (!j.sucesso) throw new Error(j.mensagem || 'Falha ao criar');
      carregarEtiquetas();
    })
    .catch(e => alert('Erro ao criar: ' + e.message));
}

// ===== Notas =====
async function carregarNotas(etiquetaId = null) {
  const tbody = document.getElementById('corpoTabela');
  if (!tbody) return;

  try {
    let url = api.notas;
    if (etiquetaId) {
      url = `${api.notas}/etiqueta/${etiquetaId}`;
    }

    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });
    const data = await response.json();
    const notas = Array.isArray(data) ? data : (data.dados || []);

    if (!notas.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="texto-centralizado">Nenhuma nota encontrada</td></tr>';
      atualizarBarraAcoes();
      return;
    }

    tbody.innerHTML = notas.map(nota => {
      const classePrazo = getClassePrazo(nota.diasRestantes);
      const textoPrazo = formatarPrazo(nota.diasRestantes, nota.prazoFinal);
      const corTexto = getCorTexto(nota.statusCor || '#000000');

      return `
        <tr data-id="${nota.id}">
          <td class="nota-titulo col-titulo" onclick="abrirNota(${nota.id})">${nota.titulo || 'Sem título'}</td>
          <td class="col-etiqueta"><span class="badge badge-etiqueta">${nota.etiquetaNome || '-'}</span></td>
          <td class="col-status">
            <span class="status-badge" style="background-color: ${nota.statusCor || '#CCC'}; color: ${corTexto};">
              ${nota.statusNome || '-'}
            </span>
          </td>
          <td class="col-prazo ${classePrazo}">${textoPrazo}</td>
          <td class="col-criacao data-pequena">${nota.dataCriacao || '-'}</td>
          <td class="col-atualizacao data-pequena">${nota.dataAtualizacao || '-'}</td>
          <td class="col-acoes">
            <button onclick="editarNota(${nota.id})" class="btn-icone" title="Editar">✏️</button>
            <button onclick="confirmarExclusaoNota(${nota.id})" class="btn-icone" title="Excluir">🗑️</button>
          </td>
        </tr>
      `;
    }).join('');
  } catch (error) {
    console.error('Erro ao carregar notas:', error);
    if (tbody) tbody.innerHTML = '<tr><td colspan="7" class="texto-centralizado">Erro ao carregar notas</td></tr>';
  }
}

function renderNotas() {
  const body = document.getElementById('corpoTabela');
  if (!body) return;
  body.innerHTML = '';
  if (!state.notas.length) {
    const emptyEl = document.getElementById('emptyNotas');
    if (emptyEl) emptyEl.hidden = false;
    return;
  }
  const emptyEl = document.getElementById('emptyNotas');
  if (emptyEl) emptyEl.hidden = true;

  state.notas.forEach(n => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><input type="checkbox" /></td>
      <td class="col-titulo">${n.titulo || 'Sem título'}</td>
      <td class="col-etiqueta"><span class="badge">${n.etiquetaNome || n.etiquetaId || '-'}</span></td>
      <td class="col-status"><span class="status-badge">${n.statusNome || n.statusId || '-'}</span></td>
      <td class="col-prazo">${n.prazoFinal || '-'}</td>
      <td class="col-criacao">${n.dataCriacao || '-'}</td>
      <td class="col-atualizacao">${n.dataAtualizacao || '-'}</td>
      <td class="col-acoes">
        <button class="btn pequeno" onclick="editarNota(${n.id})">✏️</button>
        <button class="btn pequeno" onclick="excluirNota(${n.id})">🗑️</button>
      </td>
    `;
    body.appendChild(tr);
  });
}

function filtrarNotas(texto) {
  const linhas = document.querySelectorAll('#corpoTabela tr');
  const busca = (texto || '').toLowerCase();

  let encontrados = 0;
  linhas.forEach(linha => {
    if (linha.cells.length <= 1) return; // Linha de carregamento/vazia

    const titulo = (linha.cells[0]?.textContent || '').toLowerCase(); // Coluna 0: Título
    const etiqueta = (linha.cells[1]?.textContent || '').toLowerCase(); // Coluna 1: Etiqueta
    const status = (linha.cells[2]?.textContent || '').toLowerCase(); // Coluna 2: Status

    const encontrado = titulo.includes(busca) || etiqueta.includes(busca) || status.includes(busca);
    linha.style.display = encontrado ? '' : 'none';
    if (encontrado) encontrados++;
  });

  const emptyEl = document.getElementById('emptyNotas');
  if (emptyEl) emptyEl.hidden = encontrados > 0;
}

// ===== Ações de nota =====
async function abrirNota(notaId) {
  try {
    const response = await fetch(`/api/notas/${notaId}`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });
    const data = await response.json();
    if (data.sucesso) {
      const nota = data.dados;
      document.getElementById('notaId').value = nota.id;
      document.getElementById('notaTitulo').value = nota.titulo || '';
      document.getElementById('notaEtiqueta').value = nota.etiquetaId ?? '';
      document.getElementById('notaStatus').value = nota.statusId ?? '';
      document.getElementById('notaPrazo').value = nota.prazoFinal || '';
      document.getElementById('notaConteudo').value = nota.conteudo || '';
      atualizarPreviewCor();
      document.getElementById('modalTitulo').textContent = 'Editar Nota';
      document.getElementById('modalNota').style.display = 'flex';
      const linha = document.querySelector(`tr[data-id="${notaId}"]`);
      if (linha) {
        linha.scrollIntoView({ behavior: 'smooth', block: 'center' });
        linha.classList.add('linha-destacada');
        setTimeout(() => linha.classList.remove('linha-destacada'), 2000);
      }
    }
  } catch (error) {
    console.error('Erro ao abrir nota:', error);
  }
}

function editarNota(id) {
  window.location.href = `/nota_cadastro.html?id=${id}`;
}

function confirmarExclusaoNota(id) {
  if (!confirm('Deseja realmente excluir esta nota?')) return;
  excluirNota(id);
}

async function excluirNota(id) {
  try {
    const token = getToken();
    const resp = await fetch(`/api/notas/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token }
    });
    const json = await resp.json();

    if (json.sucesso) {
      mostrarSucesso('Nota excluída com sucesso!');
      carregarNotas();
      carregarAlertas();
    } else {
      mostrarErro(json.mensagem || 'Erro ao excluir nota');
    }
  } catch (e) {
    console.error('Erro ao excluir nota:', e);
    mostrarErro('Erro ao excluir nota');
  }
}

async function salvarNota(event) {
  event.preventDefault();
  const id = document.getElementById('notaId').value;
  const dados = {
    titulo: document.getElementById('notaTitulo').value,
    etiquetaId: parseInt(document.getElementById('notaEtiqueta').value),
    statusId: parseInt(document.getElementById('notaStatus').value),
    prazoFinal: converterDataBRparaISO(document.getElementById('notaPrazo').value),
    conteudo: document.getElementById('notaConteudo').value
  };
  try {
    const metodo = id ? 'PUT' : 'POST';
    const url = id ? `/api/notas/${id}` : '/api/notas';
    const response = await fetch(url, {
      method: metodo,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
      },
      body: JSON.stringify(dados)
    });
    const resultado = await response.json();
    if (resultado.sucesso) {
      mostrarSucesso(id ? 'Nota atualizada!' : 'Nota criada!');
      fecharModal();
      carregarNotas();
      carregarAlertas();
    } else {
      mostrarErro(resultado.mensagem);
    }
  } catch (error) {
    console.error('Erro ao salvar nota:', error);
    mostrarErro('Erro ao salvar nota');
  }
}

function converterDataBRparaISO(dataBR) {
  const m = (dataBR || '').match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return '';
  return `${m[3]}-${m[2]}-${m[1]}`;
}

function atualizarPreviewCor() {
  const select = document.getElementById('notaStatus');
  const preview = document.getElementById('statusPreview');
  if (!select || !preview) return;
  const option = select.options[select.selectedIndex];
  if (option && option.value) {
    const cor = option.dataset.cor;
    const nome = option.textContent;
    const corTexto = getCorTexto(cor);
    preview.style.backgroundColor = cor;
    preview.style.color = corTexto;
    preview.textContent = nome;
    preview.style.display = 'block';
  } else {
    preview.style.display = 'none';
  }
}

async function carregarStatusNoSelect() {
  try {
    const select = document.getElementById('notaStatus');
    if (!select) return;
    const response = await fetch('/api/status', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });
    const data = await response.json();
    const status = data.dados || [];
    select.innerHTML = '<option value="">Selecione...</option>' +
      status.map(s => `
        <option value="${s.id}" data-cor="${s.corHex}">
          ${s.nome}
        </option>
      `).join('');
    atualizarPreviewCor();
  } catch (error) {
    console.error('Erro ao carregar status:', error);
  }
}

// ===== Ordenação =====
function ordenar(coluna) {
  const tbody = document.getElementById('corpoTabela');
  if (!tbody) return;
  const linhas = Array.from(tbody.querySelectorAll('tr'));

  if (ordenacaoAtual.coluna === coluna) {
    ordenacaoAtual.direcao = ordenacaoAtual.direcao === 'asc' ? 'desc' : 'asc';
  } else {
    ordenacaoAtual.coluna = coluna;
    ordenacaoAtual.direcao = 'asc';
  }

  const colIndex = { titulo: 1, prazo: 4 }[coluna];
  if (colIndex === undefined) return;

  linhas.sort((a, b) => {
    let valorA = a.cells[colIndex].textContent.trim();
    let valorB = b.cells[colIndex].textContent.trim();

    if (coluna === 'prazo') {
      valorA = converterDataBR(valorA);
      valorB = converterDataBR(valorB);
    }

    if (ordenacaoAtual.direcao === 'asc') {
      return valorA > valorB ? 1 : -1;
    } else {
      return valorA < valorB ? 1 : -1;
    }
  });

  linhas.forEach(linha => tbody.appendChild(linha));

  document.querySelectorAll('.sort-icon').forEach(icon => { icon.textContent = '↕️'; });
  const icone = document.querySelector(`.col-${coluna} .sort-icon`);
  if (icone) icone.textContent = ordenacaoAtual.direcao === 'asc' ? '↑' : '↓';
}

function converterDataBR(dataStr) {
  const m = (dataStr || '').match(/(\d{2})\/(\d{2})\/(\d{4})/);
  if (!m) return '';
  return `${m[3]}-${m[2]}-${m[1]}`;
}

// ===== Prazo helpers =====
function getClassePrazo(diasRestantes) {
  if (typeof diasRestantes !== 'number') return 'prazo-normal';
  if (diasRestantes < 0) return 'prazo-atrasado';
  if (diasRestantes <= 1) return 'prazo-urgente';
  if (diasRestantes <= 3) return 'prazo-atencao';
  if (diasRestantes <= 5) return 'prazo-aviso';
  return 'prazo-normal';
}

function formatarPrazo(diasRestantes, prazoFinal) {
  if (typeof diasRestantes === 'number' && diasRestantes < 0) {
    return `${prazoFinal || ''} (${Math.abs(diasRestantes)} dias atrasado)`;
  }
  return prazoFinal || '';
}

function getCorTexto(corBackground) {
  const hex = (corBackground || '').replace('#', '');
  if (hex.length !== 6) return '#000000';
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  const luminosidade = (r * 0.299 + g * 0.587 + b * 0.114);
  return luminosidade > 150 ? '#000000' : '#FFFFFF';
}

// ===== Toggle de Colunas =====
function toggleColunas() {
  const menu = document.getElementById('menuColunas');
  if (!menu) return;
  const vis = menu.style.display;
  menu.style.display = (vis === 'none' || vis === '') ? 'block' : 'none';
}

function getColIndex(coluna) {
  const map = {
    titulo: 2,
    etiqueta: 3,
    status: 4,
    prazo: 5,
    criacao: 6,
    atualizacao: 7
  };
  return map[coluna];
}

function mostrarSucesso(msg) { try { console.log(msg); } catch(_) {} }
function mostrarErro(msg) { try { console.error(msg); alert(msg); } catch(_) {} }
function fecharModal() {
  const modal = document.getElementById('modalNota');
  if (modal) modal.style.display = 'none';
}

function modalNovaNota() {
  const form = document.getElementById('formNota');
  if (form) form.reset();
  const idEl = document.getElementById('notaId');
  if (idEl) idEl.value = '';
  const tituloEl = document.getElementById('modalTitulo');
  if (tituloEl) tituloEl.textContent = 'Nova Nota';
  const modal = document.getElementById('modalNota');
  if (modal) modal.style.display = 'flex';
  atualizarPreviewCor();
  preencherEtiquetasNoSelect();
  const prazoEl = document.getElementById('notaPrazo');
  if (prazoEl) {
    try {
      if (prazoEl._flatpickr) {
        prazoEl._flatpickr.setDate(new Date(), true);
      } else {
        prazoEl.value = new Date().toLocaleDateString('pt-BR');
      }
    } catch(_) {}
  }
}

// Eventos de massa (se existirem no DOM)
document.getElementById('btnExcluirSelecionadas')?.addEventListener('click', excluirSelecionadas);
document.getElementById('selecionarTodos')?.addEventListener('change', (e) => selecionarTodas(e.target));

function selecionarTodas(checkbox) {
  const checkboxes = document.querySelectorAll('.nota-checkbox');
  checkboxes.forEach(cb => cb.checked = checkbox.checked);
  atualizarBarraAcoes();
}

function atualizarBarraAcoes() {
  const selecionadas = document.querySelectorAll('.nota-checkbox:checked').length;
  const barra = document.getElementById('barraAcoesMassa');
  if (!barra) return;
  if (selecionadas > 0) {
    barra.style.display = 'flex';
    const contadorEl = barra.querySelector('.contador');
    if (contadorEl) contadorEl.textContent = `${selecionadas} selecionada(s)`;
  } else {
    barra.style.display = 'none';
  }
}

async function excluirSelecionadas() {
  const checkboxes = document.querySelectorAll('.nota-checkbox:checked');
  const ids = Array.from(checkboxes).map(cb => cb.value);
  if (ids.length === 0) return;
  if (!confirm(`Deseja realmente excluir ${ids.length} nota(s)?`)) return;
  try {
    const promises = ids.map(id =>
      fetch(`/api/notas/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${getToken()}` }
      })
    );
    await Promise.all(promises);
    mostrarSucesso(`${ids.length} nota(s) excluída(s) com sucesso!`);
    carregarNotas();
    carregarAlertas();
  } catch (error) {
    console.error('Erro ao excluir notas:', error);
    mostrarErro('Erro ao excluir notas');
  }
}

document.addEventListener('change', function(e) {
  if (e.target.classList && e.target.classList.contains('nota-checkbox')) {
    atualizarBarraAcoes();
  }
});

function verificarAutenticacao() {
  const token = getToken();
  return !!token;
}

function inicializarEventListeners() {
  const buscaEtiquetas = document.getElementById('buscaEtiquetas');
  if (buscaEtiquetas) {
    buscaEtiquetas.addEventListener('input', (e) => {
      filtrarEtiquetas(e.target.value);
    });
  }
  const buscaNotas = document.getElementById('buscaNotas');
  if (buscaNotas) {
    buscaNotas.addEventListener('input', (e) => {
      filtrarNotas(e.target.value);
    });
  }
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.filtros')) {
      const mf = document.getElementById('menuFiltros');
      const mc = document.getElementById('menuColunas');
      if (mf) mf.style.display = 'none';
      if (mc) mc.style.display = 'none';
    }
  });
}

function preencherEtiquetasNoSelect() {
  const select = document.getElementById('notaEtiqueta');
  if (!select) return;
  const options = ['<option value="">Selecione...</option>']
    .concat((state.etiquetas || []).map(et => `<option value="${et.id}">${et.nome}</option>`));
  select.innerHTML = options.join('');
}