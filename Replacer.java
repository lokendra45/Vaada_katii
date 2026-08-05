import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class Replacer {
    public static void main(String[] args) throws Exception {
        Files.walk(Paths.get("shared/src/commonMain/kotlin/com/gaatho/rent/features"))
            .filter(p -> p.toString().endsWith(".kt"))
            .forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                    if (content.contains("FontWeight.Bold")) {
                        content = content.replace("FontWeight.Bold", "FontWeight.SemiBold");
                        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
                        System.out.println("Updated: " + p);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
