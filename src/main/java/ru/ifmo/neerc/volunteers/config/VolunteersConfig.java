package ru.ifmo.neerc.volunteers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Configuration
public class VolunteersConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.yandex.ru");
        mailSender.setPort(465);

        mailSender.setUsername("volunteers.reset");
        mailSender.setPassword("147258369a");

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smth.auth", true);
        properties.put("mail.smth.starttls.enable", true);
        properties.put("mail.debug", true);

        return mailSender;
    }


}
