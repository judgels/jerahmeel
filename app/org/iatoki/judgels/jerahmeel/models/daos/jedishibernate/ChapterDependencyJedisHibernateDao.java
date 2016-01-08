package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ChapterDependencyDao;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterDependencyModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("chapterDependencyDao")
public final class ChapterDependencyJedisHibernateDao extends AbstractJedisHibernateDao<Long, ChapterDependencyModel> implements ChapterDependencyDao {

    @Inject
    public ChapterDependencyJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ChapterDependencyModel.class);
    }

    @Override
    public boolean existsByChapterJidAndDependencyJid(String chapterJid, String dependencyJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ChapterDependencyModel> root = query.from(ChapterDependencyModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ChapterDependencyModel_.chapterJid), chapterJid), cb.equal(root.get(ChapterDependencyModel_.dependedChapterJid), dependencyJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ChapterDependencyModel> getByChapterJid(String chapterJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ChapterDependencyModel> query = cb.createQuery(ChapterDependencyModel.class);
        Root<ChapterDependencyModel> root = query.from(ChapterDependencyModel.class);

        query.where(cb.equal(root.get(ChapterDependencyModel_.chapterJid), chapterJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
