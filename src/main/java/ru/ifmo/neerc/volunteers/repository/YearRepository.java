package ru.ifmo.neerc.volunteers.repository;

import ru.ifmo.neerc.volunteers.entity.Year;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Repository
public interface YearRepository extends CrudRepository<Year, Long> {

    List<Year> findAll();
    List<Year> findAllByOrderByIdDesc();
}
