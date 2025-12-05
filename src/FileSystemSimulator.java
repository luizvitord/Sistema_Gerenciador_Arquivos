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
    public boolean mkdir(String name) {
        journal.log("MKDIR", name, "START");

        // Verificação: Já existe?
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Já existe um arquivo ou diretório com este nome.");
            journal.log("MKDIR", name, "FAIL - EXISTS");
            return false; // <--- Retorna Erro
        }

        VirtualDirectory newDir = new VirtualDirectory(name, currentDirectory);
        currentDirectory.addChild(newDir);
        journal.log("MKDIR", name, "SUCCESS");
        return true; // <--- Retorna Sucesso
    }

    // 2. Criar Arquivo Vazio (Atualizado com boolean)
    public boolean mkfile(String name) {
        journal.log("MKFILE", name, "START");

        // Verificação: Já existe?
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Nome já utilizado.");
            journal.log("MKFILE", name, "FAIL - EXISTS");
            return false; // <--- Retorna Erro
        }

        VirtualFile newFile = new VirtualFile(name, currentDirectory, "");
        currentDirectory.addChild(newFile);
        journal.log("MKFILE", name, "SUCCESS - EMPTY");
        return true; // <--- Retorna Sucesso
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
    public boolean rename(String oldName, String newName) {
        journal.log("RENAME", oldName + " -> " + newName, "START");

        // 1. Verificação de colisão: Já existe algo com o novo nome?
        if (currentDirectory.getChild(newName) != null) {
            System.out.println("Erro: Já existe um arquivo/diretório com o nome " + newName);
            journal.log("RENAME", newName, "FAIL - ALREADY EXISTS");
            return false; // Retorna falso para indicar erro
        }

        // 2. Busca o arquivo original
        FileSystemNode node = currentDirectory.getChild(oldName);

        if (node != null) {
            node.setName(newName);
            journal.log("RENAME", newName, "SUCCESS");
            return true; // Retorna verdadeiro (Sucesso)
        } else {
            System.out.println("Erro: Arquivo original não encontrado.");
            journal.log("RENAME", oldName, "FAIL - NOT FOUND");
            return false; // Retorna falso (Erro)
        }
    }

    // 7. Copiar (cp) - Implementação simples (Cópia rasa)
    public boolean cp(String sourceName, String destPath) {
        journal.log("COPY", sourceName + " -> " + destPath, "START");

        FileSystemNode sourceNode = currentDirectory.getChild(sourceName);
        if (sourceNode == null) {
            System.out.println("Erro: Arquivo de origem não encontrado.");
            return false; // <--- MUDANÇA: Retorna erro
        }

        // 1. Identificar o diretório de destino
        VirtualDirectory targetDir = resolveDirectory(destPath);

        // AQUI É O PULO DO GATO
        if (targetDir == null) {
            System.out.println("Erro: Diretório de destino inválido.");
            journal.log("COPY", sourceName, "FAIL - INVALID TARGET");
            return false; // <--- MUDANÇA: Avisa ao front que falhou
        }

        // ... (resto do código de renomeação igual) ...
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

        // 4. Executar a cópia
        if (sourceNode instanceof VirtualFile) {
            VirtualFile originalFile = (VirtualFile) sourceNode;
            VirtualFile copy = new VirtualFile(finalName, targetDir, originalFile.getContent());
            targetDir.addChild(copy);

            journal.log("COPY", finalName, "SUCCESS (Renamed from " + originalName + ")");
            System.out.println("Copiado com sucesso para: " + targetDir.getPath() + "/" + finalName);
            return true; // <--- MUDANÇA: Retorna sucesso
        } else {
            System.out.println("Erro na cópia de diretórios .");
            return false; // <--- MUDANÇA: Retorna erro
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
                    return null; // Caminho inválido (não é diretório ou não existe)
                }
            }
        }
        return target;
    }
}
