package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.ifmo.neerc.volunteers.form.UserYearForm;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id", "year", "userDays"}, includeFieldNames = false)
@EqualsAndHashCode(exclude = {"id", "year", "userDays", "experience"})
@NoArgsConstructor
public class ApplicationForm {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year")
    private Year year;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<PositionValue> positions;

    private String suggestions;

    @Column(name = "`group`")
    private String group;

    private double experience;

    private String covidStatus;

    @OneToMany(mappedBy = "userYear")
    @OrderBy("day ASC")
    private List<UserDay> userDays;

    private Date registrationDate = new Date();

    private double extraExperience = 0.0;

    public ApplicationForm(User user, Year year) {
        setUser(user);
        setYear(year);
    }

    public ApplicationForm(UserYearForm form, User user, Year year) {
        this(user, year);
        setValues(form);
    }

    public ApplicationForm(User user, Year year, Set<PositionValue> positions, String suggestions, String group, double experience, String covidStatus, List<UserDay> userDays, double extraExperience) {
        this.user = user;
        this.year = year;
        this.positions = new HashSet<>(positions);
        this.suggestions = suggestions;
        this.group = group;
        this.experience = experience;
        this.covidStatus = covidStatus;
        this.userDays = new ArrayList<>(userDays);
        this.extraExperience = extraExperience;
    }

    public static ApplicationForm createNewUsers(final ApplicationForm form, Year year) {
        return new ApplicationForm(form.getUser(), year, form.getPositions(), form.getSuggestions(), form.getGroup(), 0.0, form.getCovidStatus(), form.getUserDays(), form.getExtraExperience());
    }

    public void setValues(UserYearForm form) {
        this.positions = form.getPositions();
        this.group = form.getGroup();
        this.suggestions = form.getSuggestions();
        this.covidStatus = form.getCovidStatus();
    }

    public String positionsStr() {
        return String.join(", ", positions.stream().map(PositionValue::getName).collect(Collectors.toList()));
    }
}
