package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticDao;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("pointStatisticDao")
public final class PointStatisticHibernateDao extends AbstractJudgelsHibernateDao<PointStatisticModel> implements PointStatisticDao {

    public PointStatisticHibernateDao() {
        super(PointStatisticModel.class);
    }
}
