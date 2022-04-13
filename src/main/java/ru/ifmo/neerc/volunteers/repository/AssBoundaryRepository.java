package ru.ifmo.neerc.volunteers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ifmo.neerc.volunteers.entity.AssBoundary;
import ru.ifmo.neerc.volunteers.entity.Year;

import java.util.List;

/**
 * Created by Matvey on 12/10/17.
 */
public interface AssBoundaryRepository extends JpaRepository<AssBoundary, Long> {

    List<AssBoundary> findByYear(Year year);


}
