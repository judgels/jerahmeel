package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class SessionProblemHibernateDao extends AbstractHibernateDao<Long, SessionProblemModel> implements SessionProblemDao {

    public SessionProblemHibernateDao() {
        super(SessionProblemModel.class);
    }

    @Override
    public boolean existBySessionJidAndAlias(String sessionJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid), cb.equal(root.get(SessionProblemModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<SessionProblemModel> findBySessionJid(String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionProblemModel> query = cb.createQuery(SessionProblemModel.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.where(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public SessionProblemModel findBySesssionJidAndProblemJid(String sessionJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionProblemModel> query = cb.createQuery(SessionProblemModel.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.where(cb.and(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid), cb.equal(root.get(SessionProblemModel_.problemJid), problemJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
