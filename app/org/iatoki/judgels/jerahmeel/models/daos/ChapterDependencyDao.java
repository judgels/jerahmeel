package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ChapterDependencyModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface ChapterDependencyDao extends Dao<Long, ChapterDependencyModel> {

    boolean existsByChapterJidAndDependencyJid(String chapterJid, String dependencyJid);

    List<ChapterDependencyModel> getByChapterJid(String chapterJid);
}
