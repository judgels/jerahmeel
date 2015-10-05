package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("problemSetProblemDao")
public final class ProblemSetProblemHibernateDao extends AbstractHibernateDao<Long, ProblemSetProblemModel> implements ProblemSetProblemDao {

    public ProblemSetProblemHibernateDao() {
        super(ProblemSetProblemModel.class);
    }

    @Override
    public boolean existsByProblemSetJidAndAlias(String problemSetJid, String alias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProblemSetProblemModel> root = query.from(ProblemSetProblemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ProblemSetProblemModel_.problemSetJid), problemSetJid), cb.equal(root.get(ProblemSetProblemModel_.alias), alias)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ProblemSetProblemModel> getByProblemSetJid(String problemSetJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ProblemSetProblemModel> query = cb.createQuery(ProblemSetProblemModel.class);
        Root<ProblemSetProblemModel> root = query.from(ProblemSetProblemModel.class);

        query.where(cb.equal(root.get(ProblemSetProblemModel_.problemSetJid), problemSetJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ProblemSetProblemModel findByProblemSetJidAndProblemJid(String problemSetJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ProblemSetProblemModel> query = cb.createQuery(ProblemSetProblemModel.class);
        Root<ProblemSetProblemModel> root = query.from(ProblemSetProblemModel.class);

        query.where(cb.and(cb.equal(root.get(ProblemSetProblemModel_.problemSetJid), problemSetJid), cb.equal(root.get(ProblemSetProblemModel_.problemJid), problemJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
