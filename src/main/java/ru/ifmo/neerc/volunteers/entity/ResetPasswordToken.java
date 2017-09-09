package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Entity
@Data
@Table(indexes = {@Index(columnList = "token", unique = true)})
public class ResetPasswordToken {

    public static final int EXPIRATION = 1000 * 60 * 60 * 24;

    @Id
    @GeneratedValue
    private long id;

    private String token;

    private boolean used = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    private Date expiryDay = new Date();

    public ResetPasswordToken() {

    }

    public ResetPasswordToken(User user) {
        this.user = user;
    }
}
