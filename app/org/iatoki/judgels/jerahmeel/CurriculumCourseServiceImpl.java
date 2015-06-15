package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel_;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CurriculumCourseServiceImpl implements CurriculumCourseService {

    private final CurriculumCourseDao curriculumCourseDao;
    private final CourseDao courseDao;
    private final CourseSessionDao courseSessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final UserItemDao userItemDao;

    public CurriculumCourseServiceImpl(CurriculumCourseDao curriculumCourseDao, CourseDao courseDao, CourseSessionDao courseSessionDao, SessionDependencyDao sessionDependencyDao, UserItemDao userItemDao) {
        this.curriculumCourseDao = curriculumCourseDao;
        this.courseDao = courseDao;
        this.courseSessionDao = courseSessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean existByCurriculumJidAndCourseJid(String curriculumJid, String courseJid) {
        return curriculumCourseDao.existByCurriculumJidAndCourseJid(curriculumJid, courseJid);
    }

    @Override
    public CurriculumCourse findCurriculumCourseByCurriculumCourseId(long curriculumCourseId) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            return createFromModel(curriculumCourseModel);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    @Override
    public Page<CurriculumCourse> findCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFilters(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of());
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CurriculumCourse> curriculumCourses = curriculumCourseModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(curriculumCourses, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CurriculumCourseProgress> findCurriculumCourses(String userJid, String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFilters(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of());
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<CurriculumCourseProgress> curriculumCourseProgressBuilder = ImmutableList.builder();
        List<UserItemModel> completedUserItemModel = userItemDao.findByStatus(UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.findByStatus(UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        for (CurriculumCourseModel curriculumCourseModel : curriculumCourseModels) {
            List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, curriculumCourseModel.courseJid), ImmutableMap.of(), 0, -1);
            int completed = 0;
            CourseProgress progress = CourseProgress.LOCKED;
            for (CourseSessionModel courseSessionModel : courseSessionModels) {
                if ((completedJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    completed++;
                } else if ((onProgressJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    progress = CourseProgress.ON_PROGRESS;
                    break;
                } else {
                    List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.findBySessionJid(courseSessionModel.sessionJid);
                    Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                    dependencyJids.removeAll(completedJids);
                    if (dependencyJids.isEmpty()) {
                        progress = CourseProgress.AVAILABLE;
                        break;
                    }
                }
            }
            if (completed == courseSessionModels.size()) {
                progress = CourseProgress.COMPLETED;
            }
            curriculumCourseProgressBuilder.add(new CurriculumCourseProgress(createFromModel(curriculumCourseModel), progress));
        }

        return new Page<>(curriculumCourseProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCurriculumCourse(String curriculumJid, String courseJid, boolean completeable) {
        CurriculumCourseModel curriculumCourseModel = new CurriculumCourseModel();
        curriculumCourseModel.curriculumJid = curriculumJid;
        curriculumCourseModel.courseJid = courseJid;
        curriculumCourseModel.completeable = completeable;

        curriculumCourseDao.persist(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeCurriculumCourse(long curriculumCourseId) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            curriculumCourseDao.remove(curriculumCourseModel);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    private CurriculumCourse createFromModel(CurriculumCourseModel model) {
        return new CurriculumCourse(model.id, model.curriculumJid, model.courseJid, courseDao.findByJid(model.courseJid).name, model.completeable);
    }
}
