package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemStatisticDao")
public final class ProblemStatisticHibernateDao extends AbstractJudgelsHibernateDao<ProblemStatisticModel> implements ProblemStatisticDao {

    public ProblemStatisticHibernateDao() {
        super(ProblemStatisticModel.class);
    }
}
