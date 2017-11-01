package ru.ifmo.neerc.volunteers.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Matvey on 11/1/17.
 */
@AllArgsConstructor
public enum MailStatus {
    QUEUED("Queued"),
    SENT("Sent"),
    ERROR("Error");

    @Getter
    private String name;
}
