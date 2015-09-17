package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemStatisticEntryDao")
public final class ProblemStatisticEntryHibernateDao extends AbstractJedisHibernateDao<Long, ProblemStatisticEntryModel> implements ProblemStatisticEntryDao {

    @Inject
    public ProblemStatisticEntryHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProblemStatisticEntryModel.class);
    }
}
