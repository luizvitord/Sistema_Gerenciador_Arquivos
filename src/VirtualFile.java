public class VirtualFile extends FileSystemNode{
    private String content;

    public VirtualFile(String name, VirtualDirectory parent, String content) {
        super(name, parent);
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
