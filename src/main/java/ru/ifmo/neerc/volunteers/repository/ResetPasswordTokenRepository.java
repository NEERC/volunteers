package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.ResetPasswordToken;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Repository
public interface ResetPasswordTokenRepository extends CrudRepository<ResetPasswordToken, Long> {

    ResetPasswordToken findByToken(String token);

    Set<ResetPasswordToken> findAll();
}
