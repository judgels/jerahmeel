package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel;

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

    public String alias;

    public boolean completeable;
}
