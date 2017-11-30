package ru.ifmo.neerc.volunteers.dto;

import lombok.Data;

import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserDay;

@Data
public class UserDayDto {
    private String username;
    private String hall;

    public UserDayDto(UserDay userDay) {
        this.username = userDay.getUserYear().getUser().getEmail();
        final String chatAlias = userDay.getHall().getChatAlias();
        if (chatAlias != null && !chatAlias.isEmpty())
            this.hall = chatAlias;
        else
            this.hall = userDay.getHall().getName();
    }

    public UserDayDto(User user) {
        this.username = user.getEmail();
        this.hall = user.getChatAlias();
    }
}
