package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class SessionLessonHibernateDao extends AbstractHibernateDao<Long, SessionLessonModel> implements SessionLessonDao {

    public SessionLessonHibernateDao() {
        super(SessionLessonModel.class);
    }

    @Override
    public boolean existBySessionJidLessonJidAndAlias(String sessionJid, String lessonJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SessionLessonModel> root = query.from(SessionLessonModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(SessionLessonModel_.sessionJid), sessionJid), cb.equal(root.get(SessionLessonModel_.lessonJid), lessonJid), cb.equal(root.get(SessionLessonModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
