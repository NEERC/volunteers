package com.volunteer.home.repository;

import com.volunteer.home.entity.Event;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
public interface MyEventRepository extends CrudRepository<Event, Long> {

}
