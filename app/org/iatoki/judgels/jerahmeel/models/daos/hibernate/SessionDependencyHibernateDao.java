package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class SessionDependencyHibernateDao extends AbstractHibernateDao<Long, SessionDependencyModel> implements SessionDependencyDao {

    public SessionDependencyHibernateDao() {
        super(SessionDependencyModel.class);
    }

    @Override
    public boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionDependencyModel> root = query.from(SessionDependencyModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionDependencyModel_.sessionJid), sessionJid), cb.equal(root.get(SessionDependencyModel_.dependedSessionJid), dependencyJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<SessionDependencyModel> findBySessionJid(String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionDependencyModel> query = cb.createQuery(SessionDependencyModel.class);
        Root<SessionDependencyModel> root = query.from(SessionDependencyModel.class);

        query.where(cb.equal(root.get(SessionDependencyModel_.sessionJid), sessionJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
