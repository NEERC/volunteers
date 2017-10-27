package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.UserDay;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 02.03.2017.
 */
@Repository
public interface UserEventRepository extends CrudRepository<UserDay, Long> {

    Set<UserDay> findByDay(Day day);
}
