package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("archiveDao")
public final class ArchiveHibernateDao extends AbstractJudgelsJedisHibernateDao<ArchiveModel> implements ArchiveDao {

    @Inject
    public ArchiveHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ArchiveModel.class);
    }
}
