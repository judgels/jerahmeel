package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ActivityLogDao;
import org.iatoki.judgels.jerahmeel.models.entities.ActivityLogModel;
import org.iatoki.judgels.jophiel.models.daos.jedishibernate.AbstractActivityLogJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("activityLogDao")
public final class ActivityLogJedisHibernateDao extends AbstractActivityLogJedisHibernateDao<ActivityLogModel> implements ActivityLogDao {

    @Inject
    public ActivityLogJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ActivityLogModel.class);
    }

    @Override
    public ActivityLogModel createActivityLogModel() {
        return new ActivityLogModel();
    }
}
