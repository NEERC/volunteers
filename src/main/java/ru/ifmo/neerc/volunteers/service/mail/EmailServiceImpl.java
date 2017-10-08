package ru.ifmo.neerc.volunteers.service.mail;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
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
    final TemplateEngine templateEngine;
    final ExecutorService toSend = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void sendSimpleMessage(MimeMessage message) {
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
    public MimeMessage constructEmail(String subject, String body, IContext context, User... users) throws MessagingException {
        if (users.length == 0) {
            throw new IllegalArgumentException("users.length == 0");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setTo(Arrays.stream(users).map(User::getEmail).toArray(String[]::new));
        messageHelper.setSubject(subject);
        messageHelper.setText(templateEngine.process(body, context), true);
        String email = messageSource.getMessage("volunteers.email.from", null, "neerc@mail.ifmo.ru", locale);
        messageHelper.setFrom(email);
        return message;
    }
}
