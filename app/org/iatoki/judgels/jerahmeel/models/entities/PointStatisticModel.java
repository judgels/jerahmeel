package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_point_statistic")
@JidPrefix("POST")
public class PointStatisticModel extends AbstractJudgelsModel {

    public long time;
}
