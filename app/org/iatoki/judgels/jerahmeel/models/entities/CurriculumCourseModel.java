package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_curriculum_course")
public final class CurriculumCourseModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String curriculumJid;

    public String courseJid;

    public String alias;

    public boolean completeable;
}
