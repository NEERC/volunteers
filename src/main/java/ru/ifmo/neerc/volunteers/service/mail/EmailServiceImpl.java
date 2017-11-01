package ru.ifmo.neerc.volunteers.service.mail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import ru.ifmo.neerc.volunteers.entity.Mail;
import ru.ifmo.neerc.volunteers.entity.MailStatus;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.repository.MailRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();
    private final TemplateEngine templateEngine;
    private final EmailSendStatusService emailSendStatusService;

    private final MailRepository mailRepository;

    @Override
    public void sendSimpleMessage(List<Mail> messages) {
        mailRepository.save(messages);
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void sendMessages() {
        if (!emailSendStatusService.doWeSendEmails()) {
            return;
        }
        final String emailFrom = messageSource.getMessage("volunteers.email.from", null,
                "neerc.volunteers@gmail.com", locale);
        List<Mail> mails = mailRepository.findByStatus(MailStatus.QUEUED);
        Map<MimeMessage, Mail> msg2mail = new HashMap<>();
        try {
            mailSender.send(mails.stream().map(m -> {
                MimeMessage message = mailSender.createMimeMessage();
                if (m.getEmail().endsWith("@nowhere.com")) {
                    m.error(new Exception("Not valid email"));
                    return null;
                }
                try {
                    MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
                    messageHelper.setTo(m.getEmail());
                    messageHelper.setFrom(emailFrom);
                    messageHelper.setSubject(m.getSubject());
                    messageHelper.setText(m.getBody(), true);
                } catch (MessagingException e) {
                    m.error(e);
                    log.error("Error to create mail", e);
                    return null;
                }
                msg2mail.put(message, m);
                return message;
            }).filter(Objects::nonNull).collect(Collectors.toList()).toArray(new MimeMessage[0]));


        } catch (MailAuthenticationException e) {
            log.error("FATAL: Invalid credentials - can not authenticate. Stop sending all mails", e);
            this.emailSendStatusService.stopSendingMails();
            return;
        } catch (MailSendException e) {
            e.getFailedMessages().forEach((message, ex) -> {
                // we know for sure that MimeMessage will be returned
                Mail mail = msg2mail.remove(message);
                if (mail != null) {
                    log.error("Error to send mail to " + mail.getEmail(), ex);
                    mail.error(ex);
                } else {
                    log.error("FATAL: Wrong message returned from MailSender");
                }
            });

        }
        msg2mail.values().forEach(m -> {
            log.info("Mail for {} sent successfully", m.getEmail());
            m.success();
        });
        mailRepository.save(mails);
    }

    @Override
    public List<Mail> constructEmail(String subject, String templateName, IContext context, User... users) {
        return constructEmail(subject, templateEngine.process(templateName, context), users);
    }

    @Override
    public List<Mail> constructEmail(String subject, String body, User... users) {
        if (users.length == 0) {
            throw new IllegalArgumentException("users.length == 0");
        }
        List<Mail> mails = new ArrayList<>();
        for (User user : users) {
            Mail mail = new Mail();
            mail.setBody(body);
            mail.setStatus(MailStatus.QUEUED);
            mail.setEmail(user.getEmail());
            mail.setSubject(subject);
            mails.add(mail);
        }
        return mails;
    }
}
