import java.io.*;
import java.util.List;

public class FileSystemSimulator implements Serializable {
    private VirtualDirectory root;
    private VirtualDirectory currentDirectory;
    private transient Journal journal; // Transient para não serializar o logger

    public FileSystemSimulator() {
        // Inicializa o sistema com o diretório Root
        this.root = new VirtualDirectory("root", null);
        this.currentDirectory = root;
        this.journal = new Journal();
    }

    // Necessário reinicializar o Journal após carregar de um save
    public void initJournal() {
        if (this.journal == null) this.journal = new Journal();
    }

    public String getCurrentPath() {
        if (currentDirectory == root) return "/";
        return currentDirectory.getPath();
    }

    // --- OPERAÇÕES ---

    // 1. Criar Diretório (mkdir)
    public void mkdir(String name) {
        journal.log("MKDIR", name, "START");
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Já existe um arquivo ou diretório com este nome.");
            journal.log("MKDIR", name, "FAIL - EXISTS");
            return;
        }
        VirtualDirectory newDir = new VirtualDirectory(name, currentDirectory);
        currentDirectory.addChild(newDir);
        journal.log("MKDIR", name, "SUCCESS");
    }

    // 2. Criar Arquivo (mkfile)
    public void mkfile(String name, String content) {
        journal.log("MKFILE", name, "START");
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Nome já utilizado.");
            journal.log("MKFILE", name, "FAIL - EXISTS");
            return;
        }
        VirtualFile newFile = new VirtualFile(name, currentDirectory, content);
        currentDirectory.addChild(newFile);
        journal.log("MKFILE", name, "SUCCESS");
    }

    // 3. Listar (ls)
    public void ls() {
        System.out.println("Conteúdo de " + getCurrentPath() + ":");
        for (FileSystemNode node : currentDirectory.getChildren()) {
            String type = (node instanceof VirtualDirectory) ? "[DIR] " : "[FILE]";
            System.out.println(type + " " + node.getName());
        }
    }

    // 4. Mudar Diretório (cd)
    public void cd(String name) {
        if (name.equals("..")) {
            if (currentDirectory.getParent() != null) {
                currentDirectory = currentDirectory.getParent();
            }
            return;
        }

        FileSystemNode node = currentDirectory.getChild(name);
        if (node instanceof VirtualDirectory) {
            currentDirectory = (VirtualDirectory) node;
        } else {
            System.out.println("Erro: Diretório não encontrado.");
        }
    }

    // 5. Remover (rm) - Funciona para arquivo e diretório
    public void rm(String name) {
        journal.log("DELETE", name, "START");
        FileSystemNode node = currentDirectory.getChild(name);
        if (node != null) {
            currentDirectory.removeChild(node);
            journal.log("DELETE", name, "SUCCESS");
        } else {
            System.out.println("Erro: Arquivo não encontrado.");
            journal.log("DELETE", name, "FAIL - NOT FOUND");
        }
    }

    // 6. Renomear
    public void rename(String oldName, String newName) {
        journal.log("RENAME", oldName + " -> " + newName, "START");
        FileSystemNode node = currentDirectory.getChild(oldName);
        if (node != null) {
            node.setName(newName);
            journal.log("RENAME", newName, "SUCCESS");
        } else {
            System.out.println("Erro: Arquivo não encontrado.");
        }
    }

    // 7. Copiar (cp) - Implementação simples (Cópia rasa)
    public void cp(String sourceName, String destName) {
        journal.log("COPY", sourceName, "START");
        FileSystemNode node = currentDirectory.getChild(sourceName);

        if (node instanceof VirtualFile) {
            VirtualFile original = (VirtualFile) node;
            // Cria novo arquivo com mesmo conteúdo
            mkfile(destName, original.getContent());
            journal.log("COPY", destName, "SUCCESS");
        } else {
            System.out.println("Erro: Cópia de diretórios não implementada nesta versão simplificada.");
            journal.log("COPY", sourceName, "FAIL - IS DIR");
        }
    }

    // --- PERSISTÊNCIA (Salvar o "Disco") ---
    public void saveSystem() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("virtual_disk.dat"))) {
            oos.writeObject(this);
            System.out.println("Sistema de arquivos salvo em 'virtual_disk.dat'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileSystemSimulator loadSystem() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("virtual_disk.dat"))) {
            FileSystemSimulator fs = (FileSystemSimulator) ois.readObject();
            fs.initJournal();
            return fs;
        } catch (IOException | ClassNotFoundException e) {
            return new FileSystemSimulator(); // Retorna um novo se não existir
        }
    }
    public List<FileSystemNode> getCurrentContent() {
        return currentDirectory.getChildren();
    }

    public boolean isDirectory(String name) {
        FileSystemNode node = currentDirectory.getChild(name);
        return node instanceof VirtualDirectory;
    }

    public String getFileContent(String name) {
        FileSystemNode node = currentDirectory.getChild(name);
        if (node instanceof VirtualFile) {
            return ((VirtualFile) node).getContent();
        }
        return "";
    }
}
