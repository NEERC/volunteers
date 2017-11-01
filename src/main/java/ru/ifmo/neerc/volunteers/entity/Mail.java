package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lapenok Akesej on 26.10.2017.
 */
@Entity
@Data
@Table(indexes = {@Index(columnList = "status")})
public class Mail {

    @Id
    @GeneratedValue
    private long id;

    private String email;

    @Lob
    private String body;

    private String subject;

    private Date created = new Date();
    private Date dateSent;
    @Lob
    private String log;

    @Enumerated(EnumType.STRING)
    private MailStatus status;

    public void error(Exception e) {
        setStatus(MailStatus.ERROR);
        appendLog(e.getMessage());
    }

    public void appendLog(String message) {
        this.log = (this.log == null ? "" : this.log + "\n")
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date()) + message;
    }

    public void success() {
        setStatus(MailStatus.SENT);
        dateSent = new Date();
        appendLog("SUCCESS");
    }
}
