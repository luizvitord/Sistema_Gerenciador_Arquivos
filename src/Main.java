import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileSystemSimulator fs = FileSystemSimulator.loadSystem();
        Scanner scanner = new Scanner(System.in);
        String commandLine;

        System.out.println("=== Java File System Simulator (Type 'exit' to quit) ===");

        while (true) {
            // Exibe o prompt estilo shell: /home/user>
            System.out.print(fs.getCurrentPath() + "> ");
            commandLine = scanner.nextLine().trim();

            if (commandLine.equals("exit")) {
                fs.saveSystem(); // Salva antes de sair
                break;
            }

            String[] parts = commandLine.split(" ", 3);
            String command = parts[0];
            String arg1 = parts.length > 1 ? parts[1] : null;
            String arg2 = parts.length > 2 ? parts[2] : null;

            switch (command) {
                case "mkdir":
                    if (arg1 != null) fs.mkdir(arg1);
                    else System.out.println("Uso: mkdir <nome>");
                    break;
                case "mkfile":
                    if (arg1 != null) fs.mkfile(arg1, arg2 != null ? arg2 : "");
                    else System.out.println("Uso: mkfile <nome> [conteudo]");
                    break;
                case "ls":
                    fs.ls();
                    break;
                case "cd":
                    if (arg1 != null) fs.cd(arg1);
                    else System.out.println("Uso: cd <nome> ou cd ..");
                    break;
                case "rm":
                    if (arg1 != null) fs.rm(arg1);
                    else System.out.println("Uso: rm <nome>");
                    break;
                case "mv": // Renomear
                    if (arg1 != null && arg2 != null) fs.rename(arg1, arg2);
                    else System.out.println("Uso: mv <nome_antigo> <nome_novo>");
                    break;
                case "cp":
                    if (arg1 != null && arg2 != null) fs.cp(arg1, arg2);
                    else System.out.println("Uso: cp <origem> <destino>");
                    break;
                case "save":
                    fs.saveSystem();
                    break;
                default:
                    System.out.println("Comando desconhecido.");
            }
        }
        scanner.close();
    }
}