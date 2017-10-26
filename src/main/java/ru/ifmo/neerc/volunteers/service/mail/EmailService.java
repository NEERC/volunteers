package ru.ifmo.neerc.volunteers.service.mail;

import org.thymeleaf.context.IContext;
import ru.ifmo.neerc.volunteers.entity.Mail;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.mail.MessagingException;
import java.util.List;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
public interface EmailService {

    void sendSimpleMessage(List<Mail> messages);

    List<Mail> constructEmail(String subject, String templateName, IContext context, User... users) throws MessagingException;

    List<Mail> constructEmail(String subject, String body, User... users) throws MessagingException;
}
