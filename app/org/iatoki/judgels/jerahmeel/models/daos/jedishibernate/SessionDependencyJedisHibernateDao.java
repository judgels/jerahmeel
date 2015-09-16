package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("sessionDependencyDao")
public final class SessionDependencyJedisHibernateDao extends AbstractJedisHibernateDao<Long, SessionDependencyModel> implements SessionDependencyDao {

    @Inject
    public SessionDependencyJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, SessionDependencyModel.class);
    }

    @Override
    public boolean existsBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionDependencyModel> root = query.from(SessionDependencyModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionDependencyModel_.sessionJid), sessionJid), cb.equal(root.get(SessionDependencyModel_.dependedSessionJid), dependencyJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<SessionDependencyModel> getBySessionJid(String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionDependencyModel> query = cb.createQuery(SessionDependencyModel.class);
        Root<SessionDependencyModel> root = query.from(SessionDependencyModel.class);

        query.where(cb.equal(root.get(SessionDependencyModel_.sessionJid), sessionJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
