package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("userItemDao")
public final class UserItemHibernateDao extends AbstractHibernateDao<Long, UserItemModel> implements UserItemDao {

    public UserItemHibernateDao() {
        super(UserItemModel.class);
    }

    @Override
    public boolean existByUserJidAndItemJid(String userJid, String itemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserItemModel_.userJid), userJid), cb.equal(root.get(UserItemModel_.itemJid), itemJid)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public boolean existByUserJidItemJidAndStatus(String userJid, String itemJid, String status) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserItemModel_.userJid), userJid), cb.equal(root.get(UserItemModel_.itemJid), itemJid), cb.equal(root.get(UserItemModel_.status), status)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public UserItemModel findByUserJidAndItemJid(String userJid, String itemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserItemModel> query = cb.createQuery(UserItemModel.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.where(cb.and(cb.equal(root.get(UserItemModel_.userJid), userJid), cb.equal(root.get(UserItemModel_.itemJid), itemJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<UserItemModel> findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserItemModel> query = cb.createQuery(UserItemModel.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.where(cb.equal(root.get(UserItemModel_.userJid), userJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserItemModel> findByItemJid(String itemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserItemModel> query = cb.createQuery(UserItemModel.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.where(cb.equal(root.get(UserItemModel_.itemJid), itemJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserItemModel> findByUserJidAndStatus(String userJid, String status) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserItemModel> query = cb.createQuery(UserItemModel.class);
        Root<UserItemModel> root = query.from(UserItemModel.class);

        query.where(cb.and(cb.equal(root.get(UserItemModel_.userJid), userJid), cb.equal(root.get(UserItemModel_.status), status)));

        return JPA.em().createQuery(query).getResultList();
    }
}
