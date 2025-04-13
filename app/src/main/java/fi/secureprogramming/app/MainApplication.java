package fi.secureprogramming.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"fi.secureprogramming.app.model", "fi.secureprogramming.model"})
@EnableJpaRepositories(basePackages = {"fi.secureprogramming.repository", "fi.secureprogramming.app.repository"})
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
