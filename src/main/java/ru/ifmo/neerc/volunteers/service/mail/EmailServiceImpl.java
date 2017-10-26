package ru.ifmo.neerc.volunteers.service.mail;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import ru.ifmo.neerc.volunteers.entity.Mail;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.repository.MailRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;


/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();
    private final TemplateEngine templateEngine;

    private final MailRepository mailRepository;

    @Override
    public void sendSimpleMessage(List<Mail> messages) {
        mailRepository.save(messages);
    }

    @Scheduled(fixedDelay = 1000)
    public void sendMessages() {
        final String emailFrom = messageSource.getMessage("volunteers.email.from", null, "neerc@mail.ifmo.ru", locale);
        Iterable<Mail> mails = mailRepository.findAll();
        Set<Mail> forDelete = new HashSet<>();
        for (Mail mail : mails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
                messageHelper.setTo(mail.getEmail());
                messageHelper.setFrom(emailFrom);
                messageHelper.setSubject(mail.getSubject());
                messageHelper.setText(mail.getBody());
                mailSender.send(message);
                logger.info("Mail for {} sent successfully", mail.getEmail());
                forDelete.add(mail);
            } catch (MailException e) {
                logger.error("Error to send mail", e);
            } catch (MessagingException e) {
                logger.error("Error to create mail", e);
            }
        }
        mailRepository.delete(forDelete);
    }

    @Override
    public List<Mail> constructEmail(String subject, String templateName, IContext context, User... users) throws MessagingException {
        return constructEmail(subject, templateEngine.process(templateName, context), users);
    }

    @Override
    public List<Mail> constructEmail(String subject, String body, User... users) throws MessagingException {
        if (users.length == 0) {
            throw new IllegalArgumentException("users.length == 0");
        }
        List<Mail> mails = new ArrayList<>();
        for (User user : users) {
            Mail mail = new Mail();
            mail.setBody(body);
            mail.setEmail(user.getEmail());
            mail.setSubject(subject);
            mails.add(mail);
        }
        return mails;
    }
}
