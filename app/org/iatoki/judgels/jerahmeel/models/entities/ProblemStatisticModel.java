package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_problem_statistic")
@JidPrefix("PRST")
public class ProblemStatisticModel extends AbstractJudgelsModel {

    public long time;
}
