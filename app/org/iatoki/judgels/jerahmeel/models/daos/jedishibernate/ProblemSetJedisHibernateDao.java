package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemSetDao")
public final class ProblemSetJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ProblemSetModel> implements ProblemSetDao {

    @Inject
    public ProblemSetJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProblemSetModel.class);
    }
}
