package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemStatusDao;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemStatusModel;

public final class UserItemStatusHibernateDao extends AbstractHibernateDao<Long, UserItemStatusModel> implements UserItemStatusDao {

    public UserItemStatusHibernateDao() {
        super(UserItemStatusModel.class);
    }

}
