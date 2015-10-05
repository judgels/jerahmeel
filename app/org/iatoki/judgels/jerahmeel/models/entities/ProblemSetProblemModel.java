package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_problem_set_problem")
public final class ProblemSetProblemModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String problemSetJid;

    public String problemJid;

    public String problemSecret;

    public String alias;

    public String type;

    public String status;
}
