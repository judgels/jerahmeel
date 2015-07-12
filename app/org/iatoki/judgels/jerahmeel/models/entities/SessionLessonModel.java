package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_session_lesson")
public final class SessionLessonModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String sessionJid;

    public String lessonJid;

    public String lessonSecret;

    public String alias;

    public String status;
}
