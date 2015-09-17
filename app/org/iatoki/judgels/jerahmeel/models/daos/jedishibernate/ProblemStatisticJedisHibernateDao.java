package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemStatisticDao")
public final class ProblemStatisticJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ProblemStatisticModel> implements ProblemStatisticDao {

    @Inject
    public ProblemStatisticJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProblemStatisticModel.class);
    }
}
