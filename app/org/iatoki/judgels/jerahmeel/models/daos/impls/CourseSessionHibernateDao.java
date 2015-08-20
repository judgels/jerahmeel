package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("courseSessionDao")
public final class CourseSessionHibernateDao extends AbstractHibernateDao<Long, CourseSessionModel> implements CourseSessionDao {

    public CourseSessionHibernateDao() {
        super(CourseSessionModel.class);
    }

    @Override
    public boolean existsByCourseJidAndAlias(String courseJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CourseSessionModel> root = query.from(CourseSessionModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CourseSessionModel_.courseJid), courseJid), cb.equal(root.get(CourseSessionModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CourseSessionModel> root = query.from(CourseSessionModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CourseSessionModel_.courseJid), courseJid), cb.equal(root.get(CourseSessionModel_.sessionJid), sessionJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
