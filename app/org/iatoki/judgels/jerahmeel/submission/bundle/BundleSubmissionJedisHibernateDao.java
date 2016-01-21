package org.iatoki.judgels.jerahmeel.submission.bundle;

import org.iatoki.judgels.sandalphon.problem.bundle.submission.AbstractBundleSubmissionJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
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
