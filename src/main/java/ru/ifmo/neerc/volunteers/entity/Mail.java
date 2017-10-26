package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Lapenok Akesej on 26.10.2017.
 */
@Entity
@Data
@Table(indexes = {@Index(columnList = "sent")})
public class Mail {

    @Id
    @GeneratedValue
    private long id;

    private String email;

    @Lob
    private String body;

    private String subject;

    private boolean sent = false;
}
