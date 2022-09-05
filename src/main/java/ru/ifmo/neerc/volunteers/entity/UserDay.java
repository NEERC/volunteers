package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"day", "id", "assessments"})
@EqualsAndHashCode(exclude = {"assessments"})
@JsonIgnoreProperties(value = {"day", "userYear"})
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"userYear", "day"})})
public class UserDay {
    @Id
    @GeneratedValue
    long id;

    @JoinColumn(name = "userYear")
    @ManyToOne(fetch = FetchType.LAZY)
    ApplicationForm userYear;

    @JoinColumn(name = "day")
    @ManyToOne
    Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position")
    PositionValue position;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<Assessment> assessments;

    @ManyToOne(fetch = FetchType.LAZY)
    Hall hall;

    @Enumerated(EnumType.STRING)
    Attendance attendance = Attendance.UNKNOWN;

    public double totalAssessment() {
        return Optional.ofNullable(assessments).map(a -> a.stream().mapToDouble(Assessment::getValue).sum()).orElse(0d)
                + calcAttendanceScore();
    }

    public String totalAssessmentComment() {
        return String.format("Total Score in %s", hall.getName());
    }

    public Assessment createFakeAssessmentByAttendace(Map<Attendance, String> attendanceComments) {
        Assessment back = new Assessment();
        back.setComment(String.format("%s (%s)",day.getName(), attendanceComments.get(attendance)));
        back.setValue(calcAttendanceScore());
        return back;
    }

    public double calcAttendanceScore() {
        double score;
        switch (attendance) {
            case YES:
                score = day.getAttendanceValue();
                break;
            case LATE:
                score = day.getAttendanceValue() / 2;
                break;
            case NO:
                score = -day.getAttendanceValue();
                break;
            case SICK:
            default:
                score = 0;
                break;
        }
        return score;
    }

}
