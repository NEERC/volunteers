package ru.ifmo.neerc.volunteers.dto;

import lombok.Data;

import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserDay;

@Data
public class UserDayDto {
    private String username;
    private String hall;

    public UserDayDto(UserDay userDay) {
        final User user = userDay.getUserYear().getUser();

        this.username = user.getEmail();

        if (user.getChatAlias() != null && !user.getChatAlias().isEmpty()) {
            this.hall = user.getChatAlias();
        } else {
            this.hall = userDay.getHall().getName();
        }
    }
}
