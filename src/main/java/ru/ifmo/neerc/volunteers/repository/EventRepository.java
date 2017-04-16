package ru.ifmo.neerc.volunteers.repository;

import ru.ifmo.neerc.volunteers.entity.Event;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
public interface EventRepository extends CrudRepository<Event, Long> {

}
