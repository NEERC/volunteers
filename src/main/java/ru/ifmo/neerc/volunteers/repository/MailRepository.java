package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Mail;

/**
 * Created by Lapenok Akesej on 26.10.2017.
 */
@Repository
public interface MailRepository extends CrudRepository<Mail, Long> {
}
