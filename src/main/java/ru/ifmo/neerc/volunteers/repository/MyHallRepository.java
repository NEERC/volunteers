package ru.ifmo.neerc.volunteers.repository;

import ru.ifmo.neerc.volunteers.entity.Hall;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Repository
public interface MyHallRepository extends CrudRepository<Hall,Long> {

    Set<Hall> findAll();
}
