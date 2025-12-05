import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FileExplorerGUI extends JFrame {
    private FileSystemSimulator fs;
    private DefaultListModel<String> listModel;
    private JList<String> fileList;
    private JLabel lblPath;

    public FileExplorerGUI() {
        // Carregar o sistema (Persistência)
        fs = FileSystemSimulator.loadSystem();

        setTitle("Simulador SO - Gerenciador de Arquivos");
        setSize(600, 500); // Tamanho reduzido pois não tem mais editor
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- PAINEL SUPERIOR (Caminho e Voltar) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton btnUp = new JButton("⬆ Voltar / Nível Acima");
        lblPath = new JLabel(" Caminho: " + fs.getCurrentPath());
        lblPath.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPath.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        topPanel.add(btnUp, BorderLayout.WEST);
        topPanel.add(lblPath, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTRO (Lista de Arquivos e Pastas) ---
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Adiciona barra de rolagem
        JScrollPane scrollPane = new JScrollPane(fileList);
        add(scrollPane, BorderLayout.CENTER);

        // --- PAINEL INFERIOR (Botões de Ação) ---
        JPanel bottomPanel = new JPanel(new GridLayout(2, 3, 5, 5)); // Grid para organizar botões
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton btnMkdir = new JButton("Criar Diretório");
        JButton btnMkfile = new JButton("Criar Arquivo");
        JButton btnCopy = new JButton("Copiar");
        JButton btnRename = new JButton("Renomear");
        JButton btnDelete = new JButton("Apagar");
        JButton btnSave = new JButton("Salvar Disco");

        bottomPanel.add(btnMkdir);
        bottomPanel.add(btnMkfile);
        bottomPanel.add(btnCopy);
        bottomPanel.add(btnRename);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnSave);

        add(bottomPanel, BorderLayout.SOUTH);

        // =================AÇÕES (LISTENERS)=================

        // 1. Inicializa a lista
        refreshView();

        // 2. Botão Voltar
        btnUp.addActionListener(e -> {
            fs.cd("..");
            refreshView();
        });

        // 3. Navegação (Duplo Clique) - CORRIGIDO
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedName = getSelectedName();
                    if (selectedName != null) {
                        // Verifica no backend se é realmente um diretório antes de tentar entrar
                        if (fs.isDirectory(selectedName)) {
                            fs.cd(selectedName);
                            refreshView();
                        } else {
                            // É um arquivo. Como removemos a edição, não faz nada ou avisa.
                            JOptionPane.showMessageDialog(FileExplorerGUI.this,
                                    "Este é um arquivo. O sistema apenas gerencia a estrutura, não edita conteúdo.");
                        }
                    }
                }
            }
        });

        // 4. Criar Diretório (MKDIR)
        btnMkdir.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome do novo diretório:");
            if (isValidInput(name)) {
                // Tenta criar e captura o resultado
                boolean success = fs.mkdir(name);

                if (success) {
                    refreshView();
                } else {
                    // Pop-up de Erro
                    JOptionPane.showMessageDialog(this,
                            "Erro: Já existe um item com o nome '" + name + "' nesta pasta.",
                            "Erro ao criar pasta",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 5. Criar Arquivo (MKFILE) - Com verificação de erro
        btnMkfile.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome do novo arquivo (ex: doc.txt):");
            if (isValidInput(name)) {
                // Tenta criar e captura o resultado
                boolean success = fs.mkfile(name);

                if (success) {
                    refreshView();
                } else {
                    // Pop-up de Erro
                    JOptionPane.showMessageDialog(this,
                            "Erro: Já existe um item com o nome '" + name + "' nesta pasta.",
                            "Erro ao criar arquivo",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 6. Copiar (CP)
        btnCopy.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                String destPath = JOptionPane.showInputDialog(this,
                        "Copiando '" + selected + "'.\n" +
                                "Digite o caminho de destino (ex: '..', '/home', 'backup')\n" +
                                "Deixe em BRANCO para duplicar na pasta atual:",
                        "");

                if (destPath != null) {
                    // AGORA O FRONT RECEBE A RESPOSTA DO BACK
                    boolean sucesso = fs.cp(selected, destPath);

                    if (sucesso) {
                        refreshView();
                        JOptionPane.showMessageDialog(this, "Operação de cópia finalizada.");
                    } else {
                        // AQUI MOSTRAMOS O ERRO
                        JOptionPane.showMessageDialog(this,
                                "Erro ao copiar!\nVerifique se o diretório de destino existe.",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                showSelectWarning();
            }
        });

        // 7. Renomear (RENAME)
        btnRename.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(this, "Renomear '" + selected + "' para:");

                if (isValidInput(newName)) {
                    // Chama o backend e guarda o resultado (true ou false)
                    boolean success = fs.rename(selected, newName);

                    if (success) {
                        // Se deu certo, atualiza a tela
                        refreshView();
                    } else {
                        // Se deu errado (já existe), mostra o Pop-up de Erro
                        JOptionPane.showMessageDialog(this,
                                "Erro: Já existe um arquivo ou diretório com esse nome!",
                                "Erro ao Renomear",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                showSelectWarning();
            }
        });

        // 8. Apagar (RM)
        btnDelete.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Tem certeza que deseja apagar '" + selected + "'?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    fs.rm(selected);
                    refreshView();
                }
            } else {
                showSelectWarning();
            }
        });

        // 9. Salvar
        btnSave.addActionListener(e -> {
            fs.saveSystem();
            JOptionPane.showMessageDialog(this, "Estado do sistema salvo em disco!");
        });

        // Salvar ao fechar a janela
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                fs.saveSystem();
            }
        });
    }

    // --- Métodos Auxiliares ---

    private void refreshView() {
        listModel.clear();
        lblPath.setText(" Caminho: " + fs.getCurrentPath());

        List<FileSystemNode> nodes = fs.getCurrentContent();
        for (FileSystemNode node : nodes) {
            // Usa ícones de texto para diferenciar visualmente
            String prefix = (node instanceof VirtualDirectory) ? "[DIR]  " : "[FILE] ";
            listModel.addElement(prefix + node.getName());
        }
    }

    // Pega o nome do item selecionado removendo o prefixo visual
    private String getSelectedName() {
        String selected = fileList.getSelectedValue();
        if (selected == null) return null;
        // O prefixo "[DIR]  " ou "[FILE] " tem 7 caracteres.
        return selected.substring(7);
    }

    private boolean isValidInput(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private void showSelectWarning() {
        JOptionPane.showMessageDialog(this, "Por favor, selecione um item da lista primeiro.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FileExplorerGUI().setVisible(true);
        });
    }
}