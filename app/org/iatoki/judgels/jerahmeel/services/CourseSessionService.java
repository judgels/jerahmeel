package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionProgress;
import org.iatoki.judgels.play.Page;

public interface CourseSessionService {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid);

    CourseSession findCourseSessionById(long courseSessionId) throws CourseSessionNotFoundException;

    Page<CourseSession> getPageOfCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<CourseSessionProgress> getPageOfCourseSessionsProgress(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    CourseSession addCourseSession(String courseJid, String sessionJid, String alias, String userJid, String userIpAddress);

    void updateCourseSession(long courseSessionId, String alias, String userJid, String userIpAddress);

    void removeCourseSession(long courseSessionId);
}
