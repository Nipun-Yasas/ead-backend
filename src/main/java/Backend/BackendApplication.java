package Backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackendApplication {

    public static void main(String[] args) {
        // Load .env file if available (optional for Docker environments)
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env file is missing
                    .load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            System.out.println("No .env file found, using environment variables directly");
        }

        SpringApplication.run(BackendApplication.class, args);
    }
}