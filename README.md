# UNIVERSIDADE DE FORTALEZA
### CENTRO DE CIÊNCIAS TECNOLÓGICAS
### CURSO: CIÊNCIA DA COMPUTAÇÃO

---

# SISTEMA GERENCIADOR DE ARQUIVOS COM JOURNALING

**Autores:** Luiz Vitor e Gabriel Levi

**Palavras-chave:** Sistemas de Arquivos. Journaling. Java. Estrutura de Dados. Persistência.

## Resumo

Este trabalho apresenta o desenvolvimento de um simulador de sistema de arquivos em memória com suporte à técnica de Journaling. O software implementa uma estrutura hierárquica de diretórios e arquivos, garantindo a integridade e consistência das operações através de registros de log (Write-Ahead Logging) e permitindo a persistência do estado do disco virtual entre execuções.

## Introdução

Um sistema de arquivos é responsável por controlar como os dados são armazenados e recuperados. Para garantir a confiabilidade, sistemas modernos utilizam journaling, um mecanismo que registra as intenções de mudança em um diário (log) antes de aplicá-las. Este projeto simula um ambiente de shell interativo onde operações de manipulação de arquivos (criar, remover, renomear) são auditadas e persistidas, demonstrando na prática os conceitos de organização hierárquica e tolerância a falhas.

## Metodologia

O simulador foi desenvolvido em Java, utilizando o Padrão Composite para estruturar a hierarquia de pastas e arquivos. As classes principais são VirtualDirectory (nós compostos) e VirtualFile (nós folhas), ambas estendendo FileSystemNode.

A persistência dos dados é realizada através da serialização de objetos Java (virtual_disk.dat), salvando toda a árvore de diretórios no disco físico. O diferencial do projeto é a implementação da classe Journal, que intercepta todas as operações do usuário e escreve um log detalhado (filesystem_journal.log) com timestamp, tipo de operação e status (sucesso ou falha), simulando o comportamento de sistemas reais como ext4 ou NTFS.

## Resultados e Discussão

O sistema fornece uma interface de linha de comando (CLI) robusta. Ao executar comandos como mkdir, mkfile ou rm, o sistema valida as permissões e a existência dos arquivos, atualiza a estrutura em memória e registra a transação.

Abaixo, um exemplo de interação com o shell do simulador:

```bash
/> mkdir documentos
/> cd documentos
/documentos> mkfile relatorio.txt
/documentos> ls
Conteúdo de /documentos:
[FILE] relatorio.txt
```

Simultaneamente, o sistema gera o seguinte registro no arquivo de Journal para auditoria:

```
[2025-12-05T14:30:15] [MKDIR] documentos - SUCCESS
[2025-12-05T14:30:20] [MKFILE] relatorio.txt - SUCCESS - EMPTY
```

A persistência foi validada ao executar o comando save ou exit, onde o estado da árvore de diretórios é serializado. Ao reiniciar o programa, a estrutura é restaurada perfeitamente, garantindo a continuidade dos dados. A utilização do journaling permitiu rastrear todas as modificações, facilitando a depuração e garantindo um histórico confiável de alterações.

## Conclusão

O Sistema Gerenciador de Arquivos com Journaling cumpriu o objetivo de simular as operações fundamentais de um sistema operacional. A aplicação prática de estruturas de dados em árvore, combinada com a técnica de logging, demonstrou ser eficaz para manter a organização e a integridade dos dados. O projeto serviu para consolidar conhecimentos sobre I/O, serialização e arquitetura de sistemas de arquivos.

## Referências

TANENBAUM, Andrew S.; BOS, Herbert. **Sistemas Operacionais Modernos**. 4. ed. São Paulo: Pearson Prentice Hall, 2016.

---

# 🚀 Como Executar o Projeto

Para compilar e executar o simulador, é necessário ter o **JDK (Java Development Kit)** instalado em sua máquina.

### 1. Estrutura de Pastas

```
src/
├── Main.java                 # Ponto de entrada
├── FileSystemSimulator.java  # Lógica do sistema
├── Journal.java              # Sistema de Log
├── ShellInterface.java       # Interface CLI
├── FileSystemNode.java       # Classe Abstrata
├── VirtualDirectory.java     # Implementação de Diretório
└── VirtualFile.java          # Implementação de Arquivo
```

### 2. Compilação (via Terminal)

Abra um terminal na pasta raiz do projeto (a pasta que contém a pasta src). Execute o seguinte comando:

**Windows / Linux / macOS:**
```bash
javac -d bin src/*.java
```

### 3. Execução

Após a compilação bem-sucedida (nenhum erro deve aparecer), execute a classe principal (Main):

```bash
java -cp bin Main
```

O shell interativo será iniciado. Digite `help` para ver a lista de comandos disponíveis.