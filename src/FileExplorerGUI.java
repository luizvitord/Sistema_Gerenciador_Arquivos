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
    private JTextArea txtInfo; // Área para logs ou conteúdo de arquivo

    public FileExplorerGUI() {
        // 1. Carregar o Sistema (Persistência)
        fs = FileSystemSimulator.loadSystem();

        // 2. Configurações da Janela
        setTitle("Simulador de Sistema de Arquivos (Java Swing)");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- PAINEL SUPERIOR (Caminho e Voltar) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton btnUp = new JButton("⬆ Voltar");
        lblPath = new JLabel("Caminho: " + fs.getCurrentPath());
        lblPath.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        topPanel.add(btnUp, BorderLayout.WEST);
        topPanel.add(lblPath, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (Lista de Arquivos) ---
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(fileList);
        add(scrollPane, BorderLayout.CENTER);

        // --- PAINEL DIREITO (Info e Conteúdo) ---
        txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setBorder(BorderFactory.createTitledBorder("Visualizador / Logs"));
        JScrollPane infoScroll = new JScrollPane(txtInfo);
        infoScroll.setPreferredSize(new Dimension(250, 0));
        add(infoScroll, BorderLayout.EAST);

        // --- PAINEL INFERIOR (Botões de Ação) ---
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton btnMkdir = new JButton("Nova Pasta");
        JButton btnMkfile = new JButton("Novo Arquivo");
        JButton btnDelete = new JButton("Excluir");
        JButton btnRename = new JButton("Renomear");
        JButton btnCopy = new JButton("Copiar");
        JButton btnSave = new JButton("Salvar Disco");

        bottomPanel.add(btnMkdir);
        bottomPanel.add(btnMkfile);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRename);
        bottomPanel.add(btnCopy);
        bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
        bottomPanel.add(btnSave);

        add(bottomPanel, BorderLayout.SOUTH);

        // =================AÇÕES (LISTENERS)=================

        // 1. Atualizar a lista inicial
        refreshView();

        // 2. Botão Voltar (CD ..)
        btnUp.addActionListener(e -> {
            fs.cd("..");
            refreshView();
        });

        // 3. Navegação (Duplo Clique na lista)
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = fileList.getSelectedValue();
                    if (selected != null) {
                        // Removemos o prefixo [DIR] ou [FILE] para pegar o nome real
                        String name = selected.substring(7); // "[DIR]  " tem 7 chars

                        if (selected.startsWith("[DIR]")) {
                            fs.cd(name);
                            refreshView();
                        } else {
                            // Se for arquivo, mostra o conteúdo na lateral
                            txtInfo.setText("Conteúdo de " + name + ":\n\n" + fs.getFileContent(name));
                        }
                    }
                }
            }
        });

        // 4. Nova Pasta (MKDIR)
        btnMkdir.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome da nova pasta:");
            if (name != null && !name.trim().isEmpty()) {
                fs.mkdir(name);
                refreshView();
            }
        });

        // 5. Novo Arquivo (MKFILE)
        btnMkfile.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome do arquivo:");
            if (name != null && !name.trim().isEmpty()) {
                String content = JOptionPane.showInputDialog(this, "Conteúdo do arquivo:");
                fs.mkfile(name, content != null ? content : "");
                refreshView();
            }
        });

        // 6. Excluir (RM)
        btnDelete.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja apagar " + selected + "?");
                if (confirm == JOptionPane.YES_OPTION) {
                    fs.rm(selected);
                    refreshView();
                    txtInfo.setText("");
                }
            }
        });

        // 7. Renomear (RENAME)
        btnRename.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(this, "Renomear " + selected + " para:");
                if (newName != null && !newName.trim().isEmpty()) {
                    fs.rename(selected, newName);
                    refreshView();
                }
            }
        });

        // 8. Copiar (CP)
        btnCopy.addActionListener(e -> {
            String selected = getSelectedName();
            if (selected != null) {
                String destName = JOptionPane.showInputDialog(this, "Copiar " + selected + " como (novo nome):");
                if (destName != null && !destName.trim().isEmpty()) {
                    fs.cp(selected, destName);
                    refreshView();
                }
            }
        });

        // 9. Salvar
        btnSave.addActionListener(e -> {
            fs.saveSystem();
            JOptionPane.showMessageDialog(this, "Sistema salvo com sucesso!");
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

    // Método para redesenhar a lista baseada no estado atual do simulador
    private void refreshView() {
        listModel.clear();
        lblPath.setText("Caminho: " + fs.getCurrentPath());

        List<FileSystemNode> nodes = fs.getCurrentContent();
        for (FileSystemNode node : nodes) {
            String prefix = (node instanceof VirtualDirectory) ? "[DIR]  " : "[FILE] ";
            listModel.addElement(prefix + node.getName());
        }
    }

    // Método para pegar o nome limpo do item selecionado na lista
    private String getSelectedName() {
        String selected = fileList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um item na lista primeiro.");
            return null;
        }
        return selected.substring(7); // Remove o prefixo
    }

    public static void main(String[] args) {
        // Roda a interface na Thread correta do Swing
        SwingUtilities.invokeLater(() -> {
            new FileExplorerGUI().setVisible(true);
        });
    }
}