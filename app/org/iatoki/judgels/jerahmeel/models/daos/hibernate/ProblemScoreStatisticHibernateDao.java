package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemScoreStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemScoreStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemScoreStatisticDao")
public final class ProblemScoreStatisticHibernateDao extends AbstractJudgelsHibernateDao<ProblemScoreStatisticModel> implements ProblemScoreStatisticDao {

    public ProblemScoreStatisticHibernateDao() {
        super(ProblemScoreStatisticModel.class);
    }
}
