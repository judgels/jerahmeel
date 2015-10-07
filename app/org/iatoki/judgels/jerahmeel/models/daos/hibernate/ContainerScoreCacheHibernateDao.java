package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("containerScoreCacheDao")
public final class ContainerScoreCacheHibernateDao extends AbstractHibernateDao<Long, ContainerScoreCacheModel> implements ContainerScoreCacheDao {

    public ContainerScoreCacheHibernateDao() {
        super(ContainerScoreCacheModel.class);
    }

    @Override
    public boolean existsByUserJidAndContainerJid(String userJid, String containerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContainerScoreCacheModel> root = query.from(ContainerScoreCacheModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContainerScoreCacheModel_.userJid), userJid), cb.equal(root.get(ContainerScoreCacheModel_.containerJid), containerJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContainerScoreCacheModel getByUserJidAndContainerJid(String userJid, String containerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContainerScoreCacheModel> query = cb.createQuery(ContainerScoreCacheModel.class);
        Root<ContainerScoreCacheModel> root = query.from(ContainerScoreCacheModel.class);

        query.where(cb.and(cb.equal(root.get(ContainerScoreCacheModel_.userJid), userJid), cb.equal(root.get(ContainerScoreCacheModel_.containerJid), containerJid)));

        return getFirstResultAndDeleteTheRest(query);
    }
}