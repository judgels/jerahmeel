package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class CourseSessionHibernateDao extends AbstractHibernateDao<Long, CourseSessionModel> implements CourseSessionDao {

    public CourseSessionHibernateDao() {
        super(CourseSessionModel.class);
    }

    @Override
    public boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CourseSessionModel> root = query.from(CourseSessionModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CourseSessionModel_.courseJid), courseJid), cb.equal(root.get(CourseSessionModel_.sessionJid), sessionJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
