package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_problem_set")
@JidPrefix("PRSE")
public final class ProblemSetModel extends AbstractJudgelsModel {

    public String archiveJid;

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
}
