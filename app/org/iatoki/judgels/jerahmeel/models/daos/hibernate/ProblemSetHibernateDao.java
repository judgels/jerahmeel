package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemSetDao")
public final class ProblemSetHibernateDao extends AbstractJudgelsHibernateDao<ProblemSetModel> implements ProblemSetDao {

    public ProblemSetHibernateDao() {
        super(ProblemSetModel.class);
    }
}
