package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Event;

import java.util.Collection;

/**
 * Created by Lapenok Aleksej on 01.08.2017.
 */
@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    Collection<Event> findAll();
}
