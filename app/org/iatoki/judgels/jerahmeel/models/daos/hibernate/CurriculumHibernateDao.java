package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("curriculumDao")
public final class CurriculumHibernateDao extends AbstractJudgelsHibernateDao<CurriculumModel> implements CurriculumDao {

    public CurriculumHibernateDao() {
        super(CurriculumModel.class);
    }

}
