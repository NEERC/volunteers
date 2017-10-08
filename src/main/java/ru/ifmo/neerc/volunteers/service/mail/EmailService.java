package ru.ifmo.neerc.volunteers.service.mail;

import org.thymeleaf.context.IContext;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
public interface EmailService {

    void sendSimpleMessage(MimeMessage message);

    MimeMessage constructEmail(String subject, String body, IContext context, User... users) throws MessagingException;
}
