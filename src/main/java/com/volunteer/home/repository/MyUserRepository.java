package com.volunteer.home.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.home.entity.User;

/**
 * Created by Алексей on 21.02.2017.
 */
@Repository
public interface MyUserRepository extends CrudRepository<User, Long> {

    List<User> findByName(String name);
    User findByEmailIgnoreCase(String name);
}
