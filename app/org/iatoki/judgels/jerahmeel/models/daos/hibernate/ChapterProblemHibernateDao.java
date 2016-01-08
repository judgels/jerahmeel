package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ChapterProblemDao;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterProblemModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("chapterProblemDao")
public final class ChapterProblemHibernateDao extends AbstractHibernateDao<Long, ChapterProblemModel> implements ChapterProblemDao {

    public ChapterProblemHibernateDao() {
        super(ChapterProblemModel.class);
    }

    @Override
    public boolean existsByChapterJidAndAlias(String chapterJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ChapterProblemModel> root = query.from(ChapterProblemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ChapterProblemModel_.chapterJid), chapterJid), cb.equal(root.get(ChapterProblemModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ChapterProblemModel> getByChapterJid(String chapterJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ChapterProblemModel> query = cb.createQuery(ChapterProblemModel.class);
        Root<ChapterProblemModel> root = query.from(ChapterProblemModel.class);

        query.where(cb.equal(root.get(ChapterProblemModel_.chapterJid), chapterJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ChapterProblemModel findByChapterJidAndProblemJid(String chapterJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ChapterProblemModel> query = cb.createQuery(ChapterProblemModel.class);
        Root<ChapterProblemModel> root = query.from(ChapterProblemModel.class);

        query.where(cb.and(cb.equal(root.get(ChapterProblemModel_.chapterJid), chapterJid), cb.equal(root.get(ChapterProblemModel_.problemJid), problemJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
