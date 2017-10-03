package ru.ifmo.neerc.volunteers.service.mail;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.User;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    final JavaMailSender mailSender;
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final MessageSource messageSource;
    final Locale locale = LocaleContextHolder.getLocale();
    final ExecutorService toSend = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void sendSimpleMessage(SimpleMailMessage message) {
        toSend.execute(() -> {
            try {
                logger.info("I will send email", message);
                mailSender.send(message);
                logger.info("I sent email", message);
            } catch (MailException e) {
                logger.error("Error to send email", e);
            }
        });
    }

    @Override
    public SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(body);
        message.setTo(user.getEmail());
        String email = messageSource.getMessage("volunteers.email.from", null, "neerc@mail.ifmo.ru", locale);
        message.setFrom(email);
        return message;
    }
}
