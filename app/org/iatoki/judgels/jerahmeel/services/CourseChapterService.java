package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.CourseChapter;
import org.iatoki.judgels.jerahmeel.CourseChapterNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseChapterWithProgress;
import org.iatoki.judgels.play.Page;

public interface CourseChapterService {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndChapterJid(String courseJid, String chapterJid);

    CourseChapter findCourseChapterById(long courseChapterId) throws CourseChapterNotFoundException;

    Page<CourseChapter> getPageOfCourseChapters(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<CourseChapterWithProgress> getPageOfCourseChaptersWithProgress(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    CourseChapter addCourseChapter(String courseJid, String chapterJid, String alias, String userJid, String userIpAddress);

    void updateCourseChapter(long courseChapterId, String alias, String userJid, String userIpAddress);

    void removeCourseChapter(long courseChapterId);
}
