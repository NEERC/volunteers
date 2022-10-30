package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.neerc.volunteers.entity.Year;

import java.util.List;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Repository
public interface YearRepository extends CrudRepository<Year, Long> {

    List<Year> findAll();
    List<Year> findAllByOrderByIdDesc();

    Year findByName(String name);

}
