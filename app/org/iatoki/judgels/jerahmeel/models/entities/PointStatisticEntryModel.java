package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_point_statistic_entry")
public class PointStatisticEntryModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String pointStatisticJid;

    public String userJid;

    public double totalPoints;

    public long totalProblems;
}
