package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.PositionValue;

/**
 * Created by Lapenok Akesej on 19.03.2017.
 */
@Repository
public interface PositionValueRepository extends CrudRepository<PositionValue,Long> {
}
