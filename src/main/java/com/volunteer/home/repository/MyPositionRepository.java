package com.volunteer.home.repository;

import com.volunteer.home.entity.Position;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Repository
public interface MyPositionRepository extends CrudRepository<Position,Long> {
    Set<Position> findAll();
}
