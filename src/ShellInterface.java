import java.util.Scanner;

public class ShellInterface {

    private final FileSystemSimulator fs;
    private final Scanner scanner;

    public ShellInterface(FileSystemSimulator fs) {
        this.fs = fs;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();

        while (true) {
            System.out.print(fs.getCurrentPath() + "> ");

            String commandLine = scanner.nextLine().trim();
            if (commandLine.isEmpty()) continue;

            String[] parts = commandLine.split("\\s+", 3);
            String command = parts[0].toLowerCase();
            String arg1 = parts.length > 1 ? parts[1] : null;
            String arg2 = parts.length > 2 ? parts[2] : null;

            if (command.equals("exit")) {
                fs.saveSystem();
                System.out.println("Sistema salvo. Até logo!");
                break;
            }

            processCommand(command, arg1, arg2);
        }
        scanner.close();
    }

    private void processCommand(String command, String arg1, String arg2) {
        switch (command) {
            case "help":
            case "?":
                showHelp();
                break;

            case "ls":
            case "dir":
                fs.ls();
                break;

            case "mkdir":
                if (arg1 != null) fs.mkdir(arg1);
                else printError("Uso: mkdir <nome_pasta>");
                break;

            case "mkfile":
            case "touch":
                if (arg1 != null) fs.mkfile(arg1);
                else printError("Uso: mkfile <nome_arquivo>");
                break;

            case "cd":
                if (arg1 != null) fs.cd(arg1);
                else printError("Uso: cd <caminho> ou cd ..");
                break;

            case "rm":
                if (arg1 != null) fs.rm(arg1);
                else printError("Uso: rm <nome_arquivo_ou_pasta>");
                break;

            case "mv":
                if (arg1 != null && arg2 != null) fs.rename(arg1, arg2);
                else printError("Uso: mv <nome_antigo> <nome_novo>");
                break;

            case "cp":
                if (arg1 != null && arg2 != null) fs.cp(arg1, arg2);
                else printError("Uso: cp <origem> <destino>");
                break;

            case "save":
                fs.saveSystem();
                break;

            case "cls":
            case "clear":
                for (int i = 0; i < 50; i++) System.out.println();
                break;

            default:
                System.out.println("Comando '" + command + "' não reconhecido. Digite 'help' para ver a lista.");
        }
    }

    private void showHelp() {
        System.out.println("\n--- COMANDOS DISPONÍVEIS ---");
        System.out.printf("%-15s %-35s %s\n", "COMANDO", "USO", "DESCRIÇÃO");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("%-15s %-35s %s\n", "ls / dir", "ls", "Lista o conteúdo do diretório atual");
        System.out.printf("%-15s %-35s %s\n", "cd", "cd <caminho>", "Navega entre pastas (use .. para voltar)");
        System.out.printf("%-15s %-35s %s\n", "mkdir", "mkdir <nome>", "Cria um novo diretório");
        System.out.printf("%-15s %-35s %s\n", "mkfile / touch", "mkfile <nome>", "Cria um novo arquivo vazio");
        System.out.printf("%-15s %-35s %s\n", "rm", "rm <nome>", "Remove um arquivo ou diretório");
        System.out.printf("%-15s %-35s %s\n", "mv", "mv <atual> <novo>", "Renomeia um arquivo ou diretório");
        System.out.printf("%-15s %-35s %s\n", "cp", "cp <arquivoorigem> <destino (/nomediretorio)>", "Copia um arquivo para outra pasta");
        System.out.printf("%-15s %-35s %s\n", "save", "save", "Salva o estado atual no disco");
        System.out.printf("%-15s %-35s %s\n", "cls / clear", "cls", "Limpa a tela do terminal");
        System.out.printf("%-15s %-35s %s\n", "exit", "exit", "Salva e sai do programa");
        System.out.println("-----------------------------------------------------------------------------\n");
    }

    private void printHeader() {
        System.out.println("==========================================");
        System.out.println("   JAVA VIRTUAL FILE SYSTEM (SHELL MODE)  ");
        System.out.println("==========================================");
        System.out.println("Dica: Digite 'help' para ver os comandos.");
        System.out.println();
    }

    private void printError(String msg) {
        System.out.println(">> " + msg);
    }
}