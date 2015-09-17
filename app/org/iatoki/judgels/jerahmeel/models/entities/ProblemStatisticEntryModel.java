package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_problem_statistic_entry")
public class ProblemStatisticEntryModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String problemStatisticJid;

    public String problemJid;

    public long totalSubmissions;
}
