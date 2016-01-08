package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.jerahmeel.ChapterNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ChapterService {

    boolean chapterExistsByJid(String chapterJid);

    List<Chapter> getChaptersByTerm(String term);

    Map<String, Chapter> getChaptersMapByJids(List<String> chapterJids);

    Page<Chapter> getPageOfChapters(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Chapter findChapterByJid(String chapterJid);

    Chapter findChapterById(long chapterId) throws ChapterNotFoundException;

    Map<String, String> getChapterJidToNameMapByChapterJids(Collection<String> chapterJids);

    Chapter createChapter(String name, String description, String userJid, String userIpAddress);

    void updateChapter(String chapterJid, String name, String description, String userJid, String userIpAddress);
}
