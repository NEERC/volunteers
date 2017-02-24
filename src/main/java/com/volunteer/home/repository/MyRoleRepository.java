package com.volunteer.home.repository;

import com.volunteer.home.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Lapenok Akesej on 24.02.2017.
 */
@Repository
public interface MyRoleRepository extends CrudRepository<Role,Long> {

}
