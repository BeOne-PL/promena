package pl.beone.promena.executable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"pl.beone.promena"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}