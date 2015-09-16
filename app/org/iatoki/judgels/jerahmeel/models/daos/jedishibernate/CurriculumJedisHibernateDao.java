package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("curriculumDao")
public final class CurriculumJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<CurriculumModel> implements CurriculumDao {

    @Inject
    public CurriculumJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, CurriculumModel.class);
    }

}
