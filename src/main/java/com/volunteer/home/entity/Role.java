package com.volunteer.home.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

/**
 * Created by Алексей on 23.02.2017.
 */
@Entity
@Data
public class Role implements Serializable, GrantedAuthority {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @Override
    public String getAuthority() {
        return getName();
    }
}
