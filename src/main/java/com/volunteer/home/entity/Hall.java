package com.volunteer.home.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
public class Hall {

    @Id
    @GeneratedValue
    long id;

    String name;
}
