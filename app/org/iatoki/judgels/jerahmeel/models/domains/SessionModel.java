package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.JidPrefix;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;
import org.iatoki.judgels.jophiel.commons.models.domains.AbstractUserModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_session")
@JidPrefix("SESS")
public final class SessionModel extends AbstractJudgelsModel {

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
}
