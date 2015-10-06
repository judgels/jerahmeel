package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonWithProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.play.Page;

public interface SessionLessonService {

    boolean aliasExistsInSession(String sessionJid, String alias);

    SessionLesson findSessionLessonById(long sessionLessonId) throws SessionLessonNotFoundException;

    Page<SessionLesson> getPageOfSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<SessionLessonWithProgress> getPageOfSessionLessonsWithProgress(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionLesson(String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status, String userJid, String userIpAddress);

    void updateSessionLesson(long sessionLessonId, String alias, SessionLessonStatus status, String userJid, String userIpAddress);

    void removeSessionLesson(long sessionLessonId);
}
