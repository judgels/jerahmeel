package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("sessionLessonDao")
public final class SessionLessonHibernateDao extends AbstractHibernateDao<Long, SessionLessonModel> implements SessionLessonDao {

    public SessionLessonHibernateDao() {
        super(SessionLessonModel.class);
    }

    @Override
    public boolean existBySessionJidAndAlias(String sessionJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionLessonModel> root = query.from(SessionLessonModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionLessonModel_.sessionJid), sessionJid), cb.equal(root.get(SessionLessonModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
