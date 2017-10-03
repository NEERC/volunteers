package ru.ifmo.neerc.volunteers.repository;

import ru.ifmo.neerc.volunteers.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Алексей on 21.02.2017.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmailIgnoreCase(String name);
}
