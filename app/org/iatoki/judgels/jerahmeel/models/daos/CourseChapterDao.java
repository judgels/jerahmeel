package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.CourseChapterModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface CourseChapterDao extends Dao<Long, CourseChapterModel> {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndChapterJid(String courseJid, String chapterJid);
}
