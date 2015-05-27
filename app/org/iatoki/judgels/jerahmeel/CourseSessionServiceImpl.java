package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.domains.SessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionModel_;

import java.util.List;
import java.util.stream.Collectors;

public final class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseDao courseDao;
    private final CourseSessionDao courseSessionDao;
    private final SessionDao sessionDao;

    public CourseSessionServiceImpl(CourseDao courseDao, CourseSessionDao courseSessionDao, SessionDao sessionDao) {
        this.courseDao = courseDao;
        this.courseSessionDao = courseSessionDao;
        this.sessionDao = sessionDao;
    }

    @Override
    public boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid) {
        return courseSessionDao.existByCourseJidAndSessionJid(courseJid, sessionJid);
    }

    @Override
    public CourseSession findByCourseSessionId(long courseSessionId) throws CourseSessionNotFoundException {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        if (courseSessionModel != null) {
            return new CourseSession(courseSessionModel.id, courseSessionModel.courseJid, courseSessionModel.sessionJid, null);
        } else {
            throw new CourseSessionNotFoundException("Course Session Not Found.");
        }
    }

    @Override
    public Page<CourseSession> findCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFilters(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CourseSession> courseSessions = courseSessionModels.stream().map(s -> new CourseSession(s.id, s.courseJid, s.sessionJid, sessionDao.findByJid(s.sessionJid).name)).collect(Collectors.toList());

        return new Page<>(courseSessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCourseSession(String courseJid, String sessionJid) {
        CourseSessionModel courseSessionModel = new CourseSessionModel();
        courseSessionModel.courseJid = courseJid;
        courseSessionModel.sessionJid = sessionJid;

        courseSessionDao.persist(courseSessionModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeCourseSession(long courseSessionId) throws CourseSessionNotFoundException {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        if (courseSessionModel != null) {
            courseSessionDao.remove(courseSessionModel);
        } else {
            throw new CourseSessionNotFoundException("Course Session Not Found.");
        }
    }
}
