package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.sandalphon.models.daos.impls.AbstractGradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.GradingDao;
import org.iatoki.judgels.jerahmeel.models.entities.GradingModel;

public final class GradingHibernateDao extends AbstractGradingHibernateDao<GradingModel> implements GradingDao {
    public GradingHibernateDao() {
        super(GradingModel.class);
    }

    @Override
    public GradingModel createGradingModel() {
        return new GradingModel();
    }
}