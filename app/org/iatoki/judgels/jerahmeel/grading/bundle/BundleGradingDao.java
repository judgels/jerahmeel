package org.iatoki.judgels.jerahmeel.grading.bundle;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.sandalphon.models.daos.BaseBundleGradingDao;

@ImplementedBy(BundleGradingHibernateDao.class)
public interface BundleGradingDao extends BaseBundleGradingDao<BundleGradingModel> {

}
