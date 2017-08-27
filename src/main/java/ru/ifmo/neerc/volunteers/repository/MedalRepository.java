package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Medal;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 28.04.2017.
 */
@Repository
public interface MedalRepository extends CrudRepository<Medal, Long> {
    Set<Medal> findAll();
}
