package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface SessionLessonService {

    boolean isInSessionByAlias(String sessionJid, String alias);

    SessionLesson findSessionLessonBySessionLessonId(long sessionLessonId) throws SessionLessonNotFoundException;

    Page<SessionLesson> findSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<SessionLessonProgress> findSessionLessons(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionLesson(String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status);

    void removeSessionLesson(long sessionLessonId) throws SessionLessonNotFoundException;

}
