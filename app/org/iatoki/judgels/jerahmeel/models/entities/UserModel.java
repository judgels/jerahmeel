package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.jophiel.models.entities.AbstractUserModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_user")
public final class UserModel extends AbstractUserModel {

    public String roles;
}
