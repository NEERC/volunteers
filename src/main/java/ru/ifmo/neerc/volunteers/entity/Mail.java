package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Created by Lapenok Akesej on 26.10.2017.
 */
@Entity
@Data
public class Mail {

    @Id
    @GeneratedValue
    private long id;

    private String email;

    @Lob
    private String body;

    private String subject;
}
