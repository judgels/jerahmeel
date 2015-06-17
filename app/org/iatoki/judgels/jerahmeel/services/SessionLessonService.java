package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;

public interface SessionLessonService {

    boolean isInSessionByAlias(String sessionJid, String alias);

    SessionLesson findSessionLessonBySessionLessonId(long sessionLessonId) throws SessionLessonNotFoundException;

    Page<SessionLesson> findSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<SessionLessonProgress> findSessionLessons(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionLesson(String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status);

    void removeSessionLesson(long sessionLessonId) throws SessionLessonNotFoundException;

}
