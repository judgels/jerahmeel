package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.JidCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.JidCacheModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJidCacheJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("jidCacheDao")
public final class JidCacheJedisHibernateDao extends AbstractJidCacheJedisHibernateDao<JidCacheModel> implements JidCacheDao {

    @Inject
    public JidCacheJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, JidCacheModel.class);
    }

    @Override
    public JidCacheModel createJidCacheModel() {
        return new JidCacheModel();
    }
}
