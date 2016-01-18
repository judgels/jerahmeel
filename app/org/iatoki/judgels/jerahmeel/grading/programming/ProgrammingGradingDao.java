package org.iatoki.judgels.jerahmeel.grading.programming;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.sandalphon.models.daos.BaseProgrammingGradingDao;

@ImplementedBy(ProgrammingGradingHibernateDao.class)
public interface ProgrammingGradingDao extends BaseProgrammingGradingDao<ProgrammingGradingModel> {

}
