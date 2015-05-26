package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_user_item_status")
public final class UserItemStatusModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String itemJid;

}
