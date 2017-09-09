package ru.ifmo.neerc.volunteers.service.mail;

import org.springframework.mail.SimpleMailMessage;
import ru.ifmo.neerc.volunteers.entity.User;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
public interface EmailService {

    void sendSimpleMessage(SimpleMailMessage message);

    SimpleMailMessage constructEmail(String subject, String body, User user);
}
