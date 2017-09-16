package ru.ifmo.neerc.volunteers.service.mail;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.User;


/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    final JavaMailSender mailSender;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sendSimpleMessage(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Error to send email", e);
        }
    }

    @Override
    public SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(body);
        message.setTo(user.getEmail());
        message.setFrom("neerc@mail.ifmo.ru");
        return message;
    }
}
