package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ChapterLessonDao;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterLessonModel;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterLessonModel_;
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
@Named("chapterLessonDao")
public final class ChapterLessonJedisHibernateDao extends AbstractJedisHibernateDao<Long, ChapterLessonModel> implements ChapterLessonDao {

    @Inject
    public ChapterLessonJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ChapterLessonModel.class);
    }

    @Override
    public boolean existsByChapterJidAndAlias(String chapterJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ChapterLessonModel> root = query.from(ChapterLessonModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ChapterLessonModel_.chapterJid), chapterJid), cb.equal(root.get(ChapterLessonModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
