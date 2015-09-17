package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("pointStatisticDao")
public final class PointStatisticJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<PointStatisticModel> implements PointStatisticDao {

    @Inject
    public PointStatisticJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, PointStatisticModel.class);
    }
}
