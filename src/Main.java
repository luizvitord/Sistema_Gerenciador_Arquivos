public class Main {
    public static void main(String[] args) {
        // 1. Carrega o Back-end (Lógica e Dados)
        FileSystemSimulator fs = FileSystemSimulator.loadSystem();

        // 2. Inicia o Front-end (Interface Shell)
        ShellInterface shell = new ShellInterface(fs);
        shell.start();
    }
}