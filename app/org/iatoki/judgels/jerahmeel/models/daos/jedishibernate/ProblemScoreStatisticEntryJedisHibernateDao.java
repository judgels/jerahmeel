package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemScoreStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemScoreStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemScoreStatisticEntryDao")
public final class ProblemScoreStatisticEntryJedisHibernateDao extends AbstractJedisHibernateDao<Long, ProblemScoreStatisticEntryModel> implements ProblemScoreStatisticEntryDao {

    @Inject
    public ProblemScoreStatisticEntryJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProblemScoreStatisticEntryModel.class);
    }
}
