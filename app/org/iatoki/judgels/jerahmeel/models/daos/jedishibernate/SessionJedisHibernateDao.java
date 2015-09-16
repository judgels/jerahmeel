package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("sessionDao")
public final class SessionJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<SessionModel> implements SessionDao {

    @Inject
    public SessionJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, SessionModel.class);
    }

    @Override
    protected List<SingularAttribute<SessionModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(SessionModel_.name, SessionModel_.description);
    }
}
