package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.ifmo.neerc.volunteers.form.UserYearForm;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id", "year", "userDays"}, includeFieldNames = false)
@EqualsAndHashCode(exclude = {"id", "year", "userDays"})
public class ApplicationForm {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year")
    private Year year;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<PositionValue> positions;

    private String suggestions;

    @Column(name = "`group`")
    private String group;

    private double experience;

    @OneToMany(mappedBy = "userYear")
    @OrderBy("day ASC")
    private List<UserDay> userDays;

    public ApplicationForm() {

    }

    public ApplicationForm(User user, Year year) {
        setUser(user);
        setYear(year);
    }

    public ApplicationForm(UserYearForm form, User user, Year year) {
        this(user, year);
        setValues(form);
    }

    public void setValues(UserYearForm form) {
        this.positions = form.getPositions();
        this.group = form.getGroup();
        this.suggestions = form.getSuggestions();
    }
}
