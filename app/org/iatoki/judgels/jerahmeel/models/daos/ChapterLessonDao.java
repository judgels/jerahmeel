package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ChapterLessonModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface ChapterLessonDao extends Dao<Long, ChapterLessonModel> {

    boolean existsByChapterJidAndAlias(String chapterJid, String alias);
}
