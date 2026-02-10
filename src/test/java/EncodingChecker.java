import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class EncodingChecker {
    public static void main(String[] args) throws IOException {
        Path resourcesDir = Paths.get("src/main/resources");

        Files.walk(resourcesDir)
             .filter(Files::isRegularFile)
             .forEach(file -> {
                 try {
                     // tenta ler o arquivo em UTF-8
                     byte[] bytes = Files.readAllBytes(file);
                     String content = new String(bytes, StandardCharsets.UTF_8);

                     // reencode para ver se bate
                     byte[] reencoded = content.getBytes(StandardCharsets.UTF_8);
                     if (!java.util.Arrays.equals(bytes, reencoded)) {
                         System.out.println("⚠ Arquivo com problemas de encoding: " + file);
                     }
                 } catch (Exception e) {
                     System.out.println("Erro ao verificar " + file + ": " + e.getMessage());
                 }
             });
    }
}
