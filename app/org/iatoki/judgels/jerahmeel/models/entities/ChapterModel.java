package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_chapter")
@JidPrefix("SESS")
public final class ChapterModel extends AbstractJudgelsModel {

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
}
