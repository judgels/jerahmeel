package org.iatoki.judgels.jerahmeel.activity;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.models.daos.BaseActivityLogDao;

@ImplementedBy(ActivityLogHibernateDao.class)
public interface ActivityLogDao extends BaseActivityLogDao<ActivityLogModel> {

}
