package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("archiveDao")
public final class ArchiveHibernateDao extends AbstractJudgelsHibernateDao<ArchiveModel> implements ArchiveDao {

    public ArchiveHibernateDao() {
        super(ArchiveModel.class);
    }
}
