package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.ChapterDependency;
import org.iatoki.judgels.jerahmeel.ChapterDependencyNotFoundException;
import org.iatoki.judgels.play.Page;

public interface ChapterDependencyService {

    boolean isDependenciesFulfilled(String userJid, String chapterJid);

    boolean existsByChapterJidAndDependencyJid(String chapterJid, String dependencyJid);

    ChapterDependency findChapterDependencyById(long chapterDependencyId) throws ChapterDependencyNotFoundException;

    Page<ChapterDependency> getPageOfChapterDependencies(String chapterJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ChapterDependency addChapterDependency(String chapterJid, String dependedChapterJid, String userJid, String userIpAddress);

    void removeChapterDependency(long chapterDependencyId);
}
