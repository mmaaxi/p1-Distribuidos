import java.io.*;

public class Main {
    public static void main(String[] args) {
        String inputFilePath = "input.txt";
        String part1FilePath = "part1.txt";
        String part2FilePath = "part2.txt";

        try {
            splitFile(inputFilePath, part1FilePath, part2FilePath);

            WordCounterProcessThread thread1 = new WordCounterProcessThread(part1FilePath);
            WordCounterProcessThread thread2 = new WordCounterProcessThread(part2FilePath);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            System.out.println("Palabras en " + part1FilePath + ": " + thread1.getWordCount());
            System.out.println("Palabras en " + part2FilePath + ": " + thread2.getWordCount());
            System.out.println("Procesamiento completado. Total de palabras: " +
                    (thread1.getWordCount() + thread2.getWordCount()));

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para dividir el archivo en dos partes
    private static void splitFile(String inputFilePath, String part1FilePath, String part2FilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || inputFile.length() == 0) {
            throw new IOException("El archivo de entrada no existe o está vacío.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer1 = new FileWriter(part1FilePath);
             FileWriter writer2 = new FileWriter(part2FilePath)) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    if (lineCount % 2 == 0) {
                        writer1.write(line + "\n");
                    } else {
                        writer2.write(line + "\n");
                    }
                    lineCount++;
                }
            }
        }
    }
}

// Clase que representa un hilo que ejecuta un proceso para contar palabras en un archivo
class WordCounterProcessThread extends Thread {
    private final String filePath;
    private int wordCount;

    public WordCounterProcessThread(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            wordCount = countWordsInFileUsingProcess(filePath);
        } catch (IOException | InterruptedException | NumberFormatException e) {
            System.err.println("Error en el proceso para " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getWordCount() {
        return wordCount;
    }

    // Método que usa ProcessBuilder para contar palabras en un archivo
    private int countWordsInFileUsingProcess(String filePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // para windows con powershell
            processBuilder.command("powershell", "-command",
                    "(Get-Content '" + filePath + "' | Out-String | Measure-Object -Word).Words");
        } else {
            processBuilder.command("sh", "-c", "wc -w < '" + filePath + "'");
        }

        Process process = processBuilder.start();
        process.waitFor();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            if (output == null || output.trim().isEmpty()) {
                throw new IOException("No se pudo leer el conteo de palabras para " + filePath);
            }
            return Integer.parseInt(output.trim());
        }
    }
}