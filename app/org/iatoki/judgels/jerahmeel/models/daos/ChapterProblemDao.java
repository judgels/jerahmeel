package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ChapterProblemModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface ChapterProblemDao extends Dao<Long, ChapterProblemModel> {

    boolean existsByChapterJidAndAlias(String chapterJid, String alias);

    List<ChapterProblemModel> getByChapterJid(String chapterJid);

    ChapterProblemModel findByChapterJidAndProblemJid(String chapterJid, String problemJid);
}
