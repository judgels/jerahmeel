package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionSessionDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionSessionModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class SessionSessionHibernateDao extends AbstractHibernateDao<Long, SessionSessionModel> implements SessionSessionDao {

    public SessionSessionHibernateDao() {
        super(SessionSessionModel.class);
    }

    @Override
    public boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionSessionModel> root = query.from(SessionSessionModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionSessionModel_.sessionJid), sessionJid), cb.equal(root.get(SessionSessionModel_.dependedSessionJid), dependencyJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
