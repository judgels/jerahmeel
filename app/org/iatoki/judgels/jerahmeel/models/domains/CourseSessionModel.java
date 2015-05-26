package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;
import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_course_session")
public final class CourseSessionModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String courseJid;

    public String sessionJid;
}
