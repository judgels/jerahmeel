package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.sandalphon.models.daos.jedishibernate.AbstractBundleSubmissionJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("bundleSubmissionDao")
public final class BundleSubmissionJedisHibernateDao extends AbstractBundleSubmissionJedisHibernateDao<BundleSubmissionModel> implements BundleSubmissionDao {

    @Inject
    public BundleSubmissionJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, BundleSubmissionModel.class);
    }

    @Override
    public BundleSubmissionModel createSubmissionModel() {
        return new BundleSubmissionModel();
    }
}
