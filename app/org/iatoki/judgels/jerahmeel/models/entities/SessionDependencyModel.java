package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_session_dependency")
public final class SessionDependencyModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String sessionJid;

    public String dependedSessionJid;
}
