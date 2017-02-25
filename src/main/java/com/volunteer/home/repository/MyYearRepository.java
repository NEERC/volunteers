package com.volunteer.home.repository;

import com.volunteer.home.entity.Year;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Repository
public interface MyYearRepository extends CrudRepository<Year,Long> {

    Set<Year> findByCurrentOrderByIdAsc(boolean current);

    @Modifying(clearAutomatically = true)
    @Query("update Year y set y.current = :current where y.id = :id")
    void finishYear(@Param("current") boolean current,@Param("id") long id);

    @Modifying(clearAutomatically = true)
    @Query("update Year y set y.open = :open where y.id = :id")
    void closeRegistaration(@Param("open") boolean open, @Param("id") long id);
}
