package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumModel;

public final class CurriculumHibernateDao extends AbstractJudgelsHibernateDao<CurriculumModel> implements CurriculumDao {

    public CurriculumHibernateDao() {
        super(CurriculumModel.class);
    }

}
