package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemScoreStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemScoreStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemScoreStatisticDao")
public final class ProblemScoreStatisticJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ProblemScoreStatisticModel> implements ProblemScoreStatisticDao {

    @Inject
    public ProblemScoreStatisticJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProblemScoreStatisticModel.class);
    }
}
