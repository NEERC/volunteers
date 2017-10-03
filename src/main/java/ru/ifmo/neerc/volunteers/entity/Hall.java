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
public class Hall {

    @Id
    @GeneratedValue
    long id;

    @NonNull
    private String name;

    @NonNull
    private boolean def;

    @NonNull
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    private Year year;

    public Hall(HallForm form, Year year) {
        this(form.getName(), false, form.getDescription(), year);
    }

    public Hall() {

    }
}
