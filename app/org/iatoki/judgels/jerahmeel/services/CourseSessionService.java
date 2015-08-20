package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionProgress;

public interface CourseSessionService {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid);

    CourseSession findCourseSessionById(long courseSessionId) throws CourseSessionNotFoundException;

    Page<CourseSession> getPageOfCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<CourseSessionProgress> getPageOfCourseSessionsProgress(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addCourseSession(String courseJid, String sessionJid, String alias, boolean completeable);

    void updateCourseSession(long courseSessionId, String alias, boolean completeable) throws CourseSessionNotFoundException;

    void removeCourseSession(long courseSessionId) throws CourseSessionNotFoundException;
}
