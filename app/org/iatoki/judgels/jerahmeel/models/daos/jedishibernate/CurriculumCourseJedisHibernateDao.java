package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("curriculumCourseDao")
public final class CurriculumCourseJedisHibernateDao extends AbstractJedisHibernateDao<Long, CurriculumCourseModel> implements CurriculumCourseDao {

    @Inject
    public CurriculumCourseJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, CurriculumCourseModel.class);
    }

    @Override
    public boolean existsByCurriculumJidAndAlias(String curriculumJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CurriculumCourseModel> root = query.from(CurriculumCourseModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CurriculumCourseModel_.curriculumJid), curriculumJid), cb.equal(root.get(CurriculumCourseModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsByCurriculumJidAndCourseJid(String curriculumJid, String courseJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CurriculumCourseModel> root = query.from(CurriculumCourseModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(CurriculumCourseModel_.curriculumJid), curriculumJid), cb.equal(root.get(CurriculumCourseModel_.courseJid), courseJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
