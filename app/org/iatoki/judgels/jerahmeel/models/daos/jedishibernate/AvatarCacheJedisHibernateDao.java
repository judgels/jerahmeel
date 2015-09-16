package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.AvatarCacheModel;
import org.iatoki.judgels.jophiel.models.daos.jedishibernate.AbstractAvatarCacheJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("avatarCacheDao")
public final class AvatarCacheJedisHibernateDao extends AbstractAvatarCacheJedisHibernateDao<AvatarCacheModel> implements AvatarCacheDao {

    @Inject
    public AvatarCacheJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}
