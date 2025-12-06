public class Main {
    public static void main(String[] args) {

        FileSystemSimulator fs = FileSystemSimulator.loadSystem();

        ShellInterface shell = new ShellInterface(fs);
        shell.start();
    }
}