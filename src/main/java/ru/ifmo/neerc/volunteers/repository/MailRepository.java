package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Mail;
import ru.ifmo.neerc.volunteers.entity.MailStatus;

import java.util.List;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.10.2017.
 */
@Repository
public interface MailRepository extends CrudRepository<Mail, Long> {
    List<Mail> findByStatus(MailStatus status);
}
