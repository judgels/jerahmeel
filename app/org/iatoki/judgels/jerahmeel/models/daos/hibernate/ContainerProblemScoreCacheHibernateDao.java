package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("containerProblemScoreCacheDao")
public final class ContainerProblemScoreCacheHibernateDao extends AbstractHibernateDao<Long, ContainerProblemScoreCacheModel> implements ContainerProblemScoreCacheDao {

    public ContainerProblemScoreCacheHibernateDao() {
        super(ContainerProblemScoreCacheModel.class);
    }

    @Override
    public boolean existsByUserJidContainerJidAndProblemJid(String userJid, String containerJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContainerProblemScoreCacheModel> root = query.from(ContainerProblemScoreCacheModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContainerProblemScoreCacheModel_.userJid), userJid), cb.equal(root.get(ContainerProblemScoreCacheModel_.containerJid), containerJid), cb.equal(root.get(ContainerProblemScoreCacheModel_.problemJid), problemJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContainerProblemScoreCacheModel getByUserJidContainerJidAndProblemJid(String userJid, String containerJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContainerProblemScoreCacheModel> query = cb.createQuery(ContainerProblemScoreCacheModel.class);
        Root<ContainerProblemScoreCacheModel> root = query.from(ContainerProblemScoreCacheModel.class);

        query.where(cb.and(cb.equal(root.get(ContainerProblemScoreCacheModel_.userJid), userJid), cb.equal(root.get(ContainerProblemScoreCacheModel_.containerJid), containerJid), cb.equal(root.get(ContainerProblemScoreCacheModel_.problemJid), problemJid)));

        return getFirstResultAndDeleteTheRest(query);
    }
}
