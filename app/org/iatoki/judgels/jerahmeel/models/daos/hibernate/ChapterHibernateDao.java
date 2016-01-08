package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.models.daos.ChapterDao;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterModel;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("chapterDao")
public final class ChapterHibernateDao extends AbstractJudgelsHibernateDao<ChapterModel> implements ChapterDao {

    public ChapterHibernateDao() {
        super(ChapterModel.class);
    }

    @Override
    protected List<SingularAttribute<ChapterModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ChapterModel_.name, ChapterModel_.description);
    }
}
