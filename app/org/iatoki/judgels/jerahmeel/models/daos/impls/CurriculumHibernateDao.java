package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
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
