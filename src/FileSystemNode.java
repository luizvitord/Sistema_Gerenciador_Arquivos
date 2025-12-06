import java.io.Serializable;

abstract class FileSystemNode implements Serializable{

    protected String name;
    protected VirtualDirectory parent;

    public FileSystemNode(String name, VirtualDirectory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public VirtualDirectory getParent() { return parent; }
    public void setParent(VirtualDirectory parent) { this.parent = parent; }

    public String getPath() {
        if (parent == null) return "/" + name;
        if (parent.getParent() == null) return "/" + name;
        return parent.getPath() + "/" + name;
    }

}
