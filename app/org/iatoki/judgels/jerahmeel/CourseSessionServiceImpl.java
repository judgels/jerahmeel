package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseSessionDao courseSessionDao;
    private final SessionDao sessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final UserItemDao userItemDao;

    public CourseSessionServiceImpl(CourseSessionDao courseSessionDao, SessionDao sessionDao, SessionDependencyDao sessionDependencyDao, UserItemDao userItemDao) {
        this.courseSessionDao = courseSessionDao;
        this.sessionDao = sessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid) {
        return courseSessionDao.existByCourseJidAndSessionJid(courseJid, sessionJid);
    }

    @Override
    public CourseSession findByCourseSessionId(long courseSessionId) throws CourseSessionNotFoundException {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        if (courseSessionModel != null) {
            return createFromModel(courseSessionModel);
        } else {
            throw new CourseSessionNotFoundException("Course Session Not Found.");
        }
    }

    @Override
    public Page<CourseSession> findCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFilters(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CourseSession> courseSessions = courseSessionModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(courseSessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CourseSessionProgress> findCourseSessions(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFilters(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<CourseSessionProgress> courseSessionProgressBuilder = ImmutableList.builder();
        List<UserItemModel> completedUserItemModel = userItemDao.findByStatus(UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.findByStatus(UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            SessionProgress progress = SessionProgress.LOCKED;
            if ((completedJids.contains(courseSessionModel.sessionJid)) && (courseSessionModel.completeable)) {
                progress = SessionProgress.COMPLETED;
            } else if ((onProgressJids.contains(courseSessionModel.sessionJid)) && (courseSessionModel.completeable)) {
                progress = SessionProgress.ON_PROGRESS;
            } else {
                List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.findBySessionJid(courseSessionModel.sessionJid);
                Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                dependencyJids.removeAll(completedJids);
                if (dependencyJids.isEmpty()) {
                    progress = SessionProgress.AVAILABLE;
                }
            }
            courseSessionProgressBuilder.add(new CourseSessionProgress(createFromModel(courseSessionModel), progress));
        }

        return new Page<>(courseSessionProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCourseSession(String courseJid, String sessionJid, boolean completeable) {
        CourseSessionModel courseSessionModel = new CourseSessionModel();
        courseSessionModel.courseJid = courseJid;
        courseSessionModel.sessionJid = sessionJid;
        courseSessionModel.completeable = completeable;

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

    private CourseSession createFromModel(CourseSessionModel model) {
        return new CourseSession(model.id, model.courseJid, model.sessionJid, sessionDao.findByJid(model.sessionJid).name, model.completeable);
    }
}
