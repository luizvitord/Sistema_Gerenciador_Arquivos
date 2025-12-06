import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Journal {
    private static final String LOG_FILE = "filesystem_journal.log";

    public void log(String operation, String target, String status) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().toString();
            out.printf("[%s] [%s] %s - %s%n", timestamp, operation, target, status);
        } catch (IOException e) {
            System.err.println("Erro crítico: Falha ao escrever no Journal.");
        }
    }
}
