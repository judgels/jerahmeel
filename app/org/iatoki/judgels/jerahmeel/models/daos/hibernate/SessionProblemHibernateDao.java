package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class SessionProblemHibernateDao extends AbstractHibernateDao<Long, SessionProblemModel> implements SessionProblemDao {

    public SessionProblemHibernateDao() {
        super(SessionProblemModel.class);
    }

    @Override
    public boolean existBySessionJidProblemJidAndAlias(String sessionJid, String problemJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid), cb.equal(root.get(SessionProblemModel_.problemJid), problemJid), cb.equal(root.get(SessionProblemModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
