package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_problem_score_statistic_entry")
public class ProblemScoreStatisticEntryModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String problemScoreStatisticJid;

    public String userJid;

    public double score;

    public long time;
}
