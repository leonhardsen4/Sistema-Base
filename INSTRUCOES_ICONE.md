# Instruções para Configurar o Ícone da Aplicação

## Arquivos Criados

Os seguintes arquivos foram criados como placeholders e devem ser substituídos pela imagem real do quebra-cabeça:

1. **src/main/resources/public/img/logo.png** - Logo em PNG (usado no navegador)
2. **src/main/resources/public/favicon.ico** - Favicon no formato ICO

## Passo 1: Preparar as Imagens

### 1.1 Salvar a imagem do quebra-cabeça

Salve a segunda imagem do quebra-cabeça que você enviou como:
- `src/main/resources/public/img/logo.png`

Recomendações:
- Tamanho: 192x192 pixels ou 512x512 pixels
- Formato: PNG com fundo transparente (se possível)

### 1.2 Converter para Favicon ICO

Você precisa converter a imagem PNG para o formato ICO. Existem várias opções:

**Opção A: Online (mais fácil)**
1. Acesse: https://www.favicon-generator.org/
2. Faça upload da imagem logo.png
3. Baixe o arquivo favicon.ico gerado
4. Substitua o arquivo `src/main/resources/public/favicon.ico`

**Opção B: Usando ImageMagick (linha de comando)**
```bash
magick convert src/main/resources/public/img/logo.png -define icon:auto-resize=16,32,48,64,256 src/main/resources/public/favicon.ico
```

**Opção C: Usando GIMP (software grátis)**
1. Abra a imagem no GIMP
2. Vá em File > Export As
3. Salve como favicon.ico
4. Marque as opções para incluir múltiplos tamanhos (16x16, 32x32, 48x48, 64x64)

## Passo 2: Configurar Ícone para jpackage (Instalador Windows)

Para que o instalador Windows (.exe) e a aplicação instalada tenham o ícone:

### 2.1 Criar o arquivo .ico para Windows

Você precisa de um arquivo .ico em alta resolução com múltiplos tamanhos:
- 16x16, 32x32, 48x48, 64x64, 128x128, 256x256

Salve este arquivo como: `src/main/resources/icon.ico`

### 2.2 Atualizar o comando jpackage

O comando jpackage no arquivo CLAUDE.md deve ser atualizado para incluir o ícone:

```bash
jpackage \
  --input target \
  --name "Sistema Base" \
  --main-jar sistema-base.jar \
  --main-class com.sistema.Main \
  --type exe \
  --dest dist \
  --win-console \
  --win-shortcut \
  --icon src/main/resources/icon.ico
```

## Passo 3: Testar

### 3.1 Testar no Navegador

1. Compile e execute a aplicação:
   ```bash
   mvn clean compile exec:java
   ```

2. Abra o navegador em: http://localhost:7070

3. Verifique se o ícone aparece na aba do navegador

4. Se o ícone não aparecer imediatamente, limpe o cache do navegador:
   - Chrome/Edge: Ctrl + Shift + Delete
   - Firefox: Ctrl + Shift + Delete

### 3.2 Testar o Instalador Windows

1. Crie o instalador:
   ```bash
   mvn clean package
   jpackage --input target --name "Sistema Base" --main-jar sistema-base.jar --main-class com.sistema.Main --type exe --dest dist --win-console --win-shortcut --icon src/main/resources/icon.ico
   ```

2. Execute o instalador gerado em `dist/Sistema Base-1.0.exe`

3. Verifique:
   - Ícone do instalador
   - Ícone do atalho criado
   - Ícone da aplicação quando executada

## Resumo dos Arquivos

| Arquivo | Propósito | Formato Recomendado |
|---------|-----------|---------------------|
| `src/main/resources/public/img/logo.png` | Logo no navegador | PNG 192x192 ou 512x512 |
| `src/main/resources/public/favicon.ico` | Favicon do navegador | ICO multi-size (16,32,48,64) |
| `src/main/resources/icon.ico` | Ícone do instalador/app Windows | ICO multi-size (16,32,48,64,128,256) |

## Tags HTML Adicionadas

As seguintes tags foram adicionadas em todos os arquivos HTML:

```html
<link rel="icon" type="image/x-icon" href="/favicon.ico">
<link rel="icon" type="image/png" sizes="32x32" href="/img/logo.png">
<link rel="apple-touch-icon" href="/img/logo.png">
```

Estas tags garantem compatibilidade com:
- Navegadores modernos (Chrome, Firefox, Edge, Safari)
- Dispositivos Apple (iPhone, iPad, Mac)
- PWA (Progressive Web Apps)

## Notas

- **IMPORTANTE**: Substitua os arquivos placeholder criados pela imagem real do quebra-cabeça
- O favicon pode demorar a atualizar devido ao cache do navegador
- Para forçar atualização, use Ctrl + F5 ou limpe o cache
- O ícone do instalador só aparece após gerar o .exe com jpackage