package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.AvatarCacheModel;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AbstractAvatarCacheHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("avatarCacheDao")
public final class AvatarCacheHibernateDao extends AbstractAvatarCacheHibernateDao<AvatarCacheModel> implements AvatarCacheDao {

    public AvatarCacheHibernateDao() {
        super(AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}
