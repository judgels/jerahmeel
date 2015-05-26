package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface CourseSessionService {

    boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid);
    
    CourseSession findByCourseSessionId(long courseSessionId) throws CourseSessionNotFoundException;

    Page<CourseSession> findCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addCourseSession(String courseJid, String sessionJid);

    void removeCourseSession(long courseSessionId) throws CourseSessionNotFoundException;
}
