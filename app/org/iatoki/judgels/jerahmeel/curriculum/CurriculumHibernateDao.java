package org.iatoki.judgels.jerahmeel.curriculum;

import org.iatoki.judgels.jerahmeel.curriculum.CurriculumDao;
import org.iatoki.judgels.jerahmeel.curriculum.CurriculumModel;
import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;

import javax.inject.Singleton;

@Singleton
public final class CurriculumHibernateDao extends AbstractJudgelsHibernateDao<CurriculumModel> implements CurriculumDao {

    public CurriculumHibernateDao() {
        super(CurriculumModel.class);
    }

}
