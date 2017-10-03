package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Assessment;

/**
 * Created by Lapenok Akesej on 09.04.2017.
 */
@Repository
public interface UserEventAssessmentRepository extends CrudRepository<Assessment, Long> {
}
