package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

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

    public boolean completeable;
}
