package ru.ifmo.neerc.volunteers.entity;

import javax.persistence.Entity;

/**
 * Created by artem on 12.03.17.
 * Represents attendance of a particular user on a particular event.
 */
public enum Attendance {
    YES, NO, LATE, SICK
}

