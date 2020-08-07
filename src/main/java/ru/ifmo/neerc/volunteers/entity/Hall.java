package ru.ifmo.neerc.volunteers.entity;

import lombok.*;
import ru.ifmo.neerc.volunteers.form.HallForm;

import javax.persistence.*;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id","year"})
@EqualsAndHashCode(exclude = {"year"})
@RequiredArgsConstructor
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hall {

    @Id
    @GeneratedValue
    long id;

    @NonNull
    private String name;

    @NonNull
    private String curName;

    @NonNull
    private boolean def;

    @NonNull
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    private Year year;

    private String chatAlias;

    public Hall(HallForm form, Year year) {
        this(form.getName(), form.getCurName(), false, form.getDescription(), year);
    }

    public void setFields(HallForm form) {
        setName(form.getName());
        setCurName(form.getCurName());
        setDescription(form.getDescription());
        setChatAlias(form.getChatAlias());
    }

    public static Hall createNewHall(final Hall hall, Year year) {
        return new Hall(hall.getName(), hall.getCurName(), hall.isDef(), hall.getDescription(), year);
    }
}
