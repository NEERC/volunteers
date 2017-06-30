package ru.ifmo.neerc.volunteers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class VolunteerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(VolunteerApplication.class, args);
    }
}
