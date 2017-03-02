package com.volunteer.home.repository;

import com.volunteer.home.entity.UserEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Lapenok Akesej on 02.03.2017.
 */
@Repository
public interface UserEventRepository extends CrudRepository<UserEvent,Long> {
}
