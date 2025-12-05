import java.io.*;
import java.util.List;

public class FileSystemSimulator implements Serializable {
    private VirtualDirectory root;
    private VirtualDirectory currentDirectory;
    private transient Journal journal;

    public FileSystemSimulator() {
        this.root = new VirtualDirectory("root", null);
        this.currentDirectory = root;
        this.journal = new Journal();
    }

    public void initJournal() {
        if (this.journal == null) this.journal = new Journal();
    }

    public String getCurrentPath() {
        if (currentDirectory == root) return "/";
        return currentDirectory.getPath();
    }

    // --- OPERAÇÕES ---

    // 1. Criar Diretório (mkdir) - Já estava correto
    public boolean mkdir(String name) {
        journal.log("MKDIR", name, "START");

        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Já existe um arquivo ou diretório com este nome.");
            journal.log("MKDIR", name, "FAIL - EXISTS");
            return false;
        }

        VirtualDirectory newDir = new VirtualDirectory(name, currentDirectory);
        currentDirectory.addChild(newDir);
        journal.log("MKDIR", name, "SUCCESS");
        return true;
    }

    // 2. Criar Arquivo (mkfile) - Já estava correto
    public boolean mkfile(String name) {
        journal.log("MKFILE", name, "START");

        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Nome já utilizado.");
            journal.log("MKFILE", name, "FAIL - EXISTS");
            return false;
        }

        VirtualFile newFile = new VirtualFile(name, currentDirectory, "");
        currentDirectory.addChild(newFile);
        journal.log("MKFILE", name, "SUCCESS - EMPTY");
        return true;
    }

    // 3. Listar (ls) - Mantido void (apenas visualização)
    public void ls() {
        System.out.println("Conteúdo de " + getCurrentPath() + ":");
        List<FileSystemNode> children = currentDirectory.getChildren();

        if (children.isEmpty()) {
            System.out.println("(Diretório vazio)");
            return;
        }

        for (FileSystemNode node : children) {
            String type = (node instanceof VirtualDirectory) ? "[DIR] " : "[FILE]";
            System.out.println(type + " " + node.getName());
        }
    }

    // 4. Mudar Diretório (cd) - ALTERADO PARA BOOLEAN
    public boolean cd(String name) {
        // Caso especial: Voltar diretório
        if (name.equals("..")) {
            if (currentDirectory.getParent() != null) {
                currentDirectory = currentDirectory.getParent();
                return true; // Sucesso: mudou de diretório
            } else {
                System.out.println("Aviso: Já está no diretório raiz.");
                return false; // Falha: não mudou nada
            }
        }

        // Navegar para frente
        FileSystemNode node = currentDirectory.getChild(name);

        if (node == null) {
            System.out.println("Erro: Diretório '" + name + "' não encontrado.");
            return false;
        }

        if (node instanceof VirtualDirectory) {
            currentDirectory = (VirtualDirectory) node;
            return true;
        } else {
            System.out.println("Erro: '" + name + "' é um arquivo, não um diretório.");
            return false;
        }
    }

    // 5. Remover (rm) - ALTERADO PARA BOOLEAN
    public boolean rm(String name) {
        journal.log("DELETE", name, "START");
        FileSystemNode node = currentDirectory.getChild(name);

        if (node != null) {
            currentDirectory.removeChild(node);
            journal.log("DELETE", name, "SUCCESS");
            System.out.println("Item '" + name + "' removido.");
            return true;
        } else {
            System.out.println("Erro: Arquivo ou diretório não encontrado.");
            journal.log("DELETE", name, "FAIL - NOT FOUND");
            return false;
        }
    }

    // 6. Renomear (rename) - Já estava correto
    public boolean rename(String oldName, String newName) {
        journal.log("RENAME", oldName + " -> " + newName, "START");

        if (currentDirectory.getChild(newName) != null) {
            System.out.println("Erro: Já existe um item com o nome " + newName);
            journal.log("RENAME", newName, "FAIL - ALREADY EXISTS");
            return false;
        }

        FileSystemNode node = currentDirectory.getChild(oldName);

        if (node != null) {
            node.setName(newName);
            journal.log("RENAME", newName, "SUCCESS");
            System.out.println("Renomeado com sucesso.");
            return true;
        } else {
            System.out.println("Erro: Arquivo original não encontrado.");
            journal.log("RENAME", oldName, "FAIL - NOT FOUND");
            return false;
        }
    }

    // 7. Copiar (cp) - Já estava correto
    public boolean cp(String sourceName, String destPath) {
        journal.log("COPY", sourceName + " -> " + destPath, "START");

        FileSystemNode sourceNode = currentDirectory.getChild(sourceName);
        if (sourceNode == null) {
            System.out.println("Erro: Arquivo de origem não encontrado.");
            return false;
        }

        VirtualDirectory targetDir = resolveDirectory(destPath);
        if (targetDir == null) {
            System.out.println("Erro: Diretório de destino inválido.");
            journal.log("COPY", sourceName, "FAIL - INVALID TARGET");
            return false;
        }

        // Lógica de renomeação automática em caso de conflito (file.txt -> file1.txt)
        String originalName = sourceNode.getName();
        String finalName = originalName;
        int counter = 1;
        String namePart = originalName;
        String extPart = "";

        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            namePart = originalName.substring(0, dotIndex);
            extPart = originalName.substring(dotIndex);
        }

        while (targetDir.getChild(finalName) != null) {
            finalName = namePart + counter + extPart;
            counter++;
        }

        if (sourceNode instanceof VirtualFile) {
            VirtualFile originalFile = (VirtualFile) sourceNode;
            VirtualFile copy = new VirtualFile(finalName, targetDir, originalFile.getContent());
            targetDir.addChild(copy);

            journal.log("COPY", finalName, "SUCCESS");
            System.out.println("Copiado com sucesso para: " + targetDir.getPath() + "/" + finalName);
            return true;
        } else {
            System.out.println("Erro: Cópia de diretórios (recursiva) ainda não implementada.");
            return false;
        }
    }

    // --- PERSISTÊNCIA ---
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
            return new FileSystemSimulator();
        }
    }

    public List<FileSystemNode> getCurrentContent() {
        return currentDirectory.getChildren();
    }

    public VirtualDirectory resolveDirectory(String path) {
        if (path == null || path.isEmpty() || path.equals(".")) return currentDirectory;

        VirtualDirectory target = path.startsWith("/") ? root : currentDirectory;
        String[] parts = path.split("/");

        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;

            if (part.equals("..")) {
                if (target.getParent() != null) target = target.getParent();
            } else {
                FileSystemNode child = target.getChild(part);
                if (child instanceof VirtualDirectory) {
                    target = (VirtualDirectory) child;
                } else {
                    return null;
                }
            }
        }
        return target;
    }
}