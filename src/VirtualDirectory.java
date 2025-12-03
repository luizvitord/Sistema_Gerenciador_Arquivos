import java.util.ArrayList;
import java.util.List;

public class VirtualDirectory extends FileSystemNode {
    private List<FileSystemNode> children;

    public VirtualDirectory(String name, VirtualDirectory parent) {
        super(name, parent);
        this.children = new ArrayList<>();
    }

    public List<FileSystemNode> getChildren() { return children; }

    public void addChild(FileSystemNode node) {
        children.add(node);
    }

    public void removeChild(FileSystemNode node) {
        children.remove(node);
    }

    // Busca filho pelo nome
    public FileSystemNode getChild(String name) {
        for (FileSystemNode node : children) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }
}
