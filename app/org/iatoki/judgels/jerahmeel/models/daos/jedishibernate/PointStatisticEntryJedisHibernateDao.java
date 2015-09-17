package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("pointStatisticEntryDao")
public final class PointStatisticEntryJedisHibernateDao extends AbstractJedisHibernateDao<Long, PointStatisticEntryModel> implements PointStatisticEntryDao {

    @Inject
    public PointStatisticEntryJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, PointStatisticEntryModel.class);
    }
}
