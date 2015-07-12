package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.domains.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_curriculum")
@JidPrefix("CURR")
public final class CurriculumModel extends AbstractJudgelsModel {

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
}
