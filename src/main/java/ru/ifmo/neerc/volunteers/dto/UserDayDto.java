package ru.ifmo.neerc.volunteers.dto;

import lombok.Data;

import ru.ifmo.neerc.volunteers.entity.UserDay;

@Data
public class UserDayDto {
    private String username;
    private String hall;

    public UserDayDto (UserDay userDay) {
        this.username = userDay.getUserYear().getUser().getEmail();
        this.hall = userDay.getHall().getName();
    }
}
