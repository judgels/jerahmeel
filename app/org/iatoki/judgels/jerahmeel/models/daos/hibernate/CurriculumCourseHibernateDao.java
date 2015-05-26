package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class CurriculumCourseHibernateDao extends AbstractHibernateDao<Long, CurriculumCourseModel> implements CurriculumCourseDao {

    public CurriculumCourseHibernateDao() {
        super(CurriculumCourseModel.class);
    }

    @Override
    public boolean existByCurriculumJidAndCourseJid(String curriculumJid, String courseJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CurriculumCourseModel> root = query.from(CurriculumCourseModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CurriculumCourseModel_.curriculumJid), curriculumJid), cb.equal(root.get(CurriculumCourseModel_.courseJid), courseJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
