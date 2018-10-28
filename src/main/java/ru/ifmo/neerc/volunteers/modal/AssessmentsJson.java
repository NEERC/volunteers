package ru.ifmo.neerc.volunteers.modal;

import lombok.Data;
import ru.ifmo.neerc.volunteers.entity.Assessment;

@Data
public class AssessmentsJson {

    String authorName;
    String comment;
    int id;
    double value;

    public AssessmentsJson(Assessment assessment) {
        this.authorName = assessment.getAuthor().getBadgeName();
        this.comment = assessment.getComment();
        this.id = assessment.getId();
        this.value = assessment.getValue();
    }

}
