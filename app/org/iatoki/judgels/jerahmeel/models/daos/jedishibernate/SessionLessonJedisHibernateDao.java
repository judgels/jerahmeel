package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("sessionLessonDao")
public final class SessionLessonJedisHibernateDao extends AbstractJedisHibernateDao<Long, SessionLessonModel> implements SessionLessonDao {

    @Inject
    public SessionLessonJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, SessionLessonModel.class);
    }

    @Override
    public boolean existsBySessionJidAndAlias(String sessionJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionLessonModel> root = query.from(SessionLessonModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionLessonModel_.sessionJid), sessionJid), cb.equal(root.get(SessionLessonModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
