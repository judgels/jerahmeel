package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("sessionProblemDao")
public final class SessionProblemHibernateDao extends AbstractHibernateDao<Long, SessionProblemModel> implements SessionProblemDao {

    public SessionProblemHibernateDao() {
        super(SessionProblemModel.class);
    }

    @Override
    public boolean existsBySessionJidAndAlias(String sessionJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid), cb.equal(root.get(SessionProblemModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<SessionProblemModel> getBySessionJid(String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionProblemModel> query = cb.createQuery(SessionProblemModel.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.where(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public SessionProblemModel findBySessionJidAndProblemJid(String sessionJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<SessionProblemModel> query = cb.createQuery(SessionProblemModel.class);
        Root<SessionProblemModel> root = query.from(SessionProblemModel.class);

        query.where(cb.and(cb.equal(root.get(SessionProblemModel_.sessionJid), sessionJid), cb.equal(root.get(SessionProblemModel_.problemJid), problemJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
