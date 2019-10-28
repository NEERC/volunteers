package ru.ifmo.neerc.volunteers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class VolunteerApplication {

    public static void main(final String[] args) {
        final ConfigurableApplicationContext run = SpringApplication.run(VolunteerApplication.class, args);
    }
}
