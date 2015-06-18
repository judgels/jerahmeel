package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.CourseProgress;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseProgress;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel_;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;

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
    public boolean existByCurriculumJidAndAlias(String curriculumJid, String alias) {
        return curriculumCourseDao.existByCurriculumJidAndAlias(curriculumJid, alias);
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
        List<UserItemModel> completedUserItemModel = userItemDao.findByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.findByUserJidAndStatus(userJid, UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        for (CurriculumCourseModel curriculumCourseModel : curriculumCourseModels) {
            List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, curriculumCourseModel.courseJid), ImmutableMap.of(), 0, -1);
            int completed = 0;
            CourseProgress progress = CourseProgress.LOCKED;
            for (CourseSessionModel courseSessionModel : courseSessionModels) {
                if ((completedJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    completed++;
                } else if ((onProgressJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    progress = CourseProgress.IN_PROGRESS;
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
    public void addCurriculumCourse(String curriculumJid, String courseJid, String alias, boolean completeable) {
        CurriculumCourseModel curriculumCourseModel = new CurriculumCourseModel();
        curriculumCourseModel.curriculumJid = curriculumJid;
        curriculumCourseModel.courseJid = courseJid;
        curriculumCourseModel.alias = alias;
        curriculumCourseModel.completeable = completeable;

        curriculumCourseDao.persist(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateCurriculumCourse(long curriculumCourseId, String alias, boolean completeable) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            curriculumCourseModel.alias = alias;
            curriculumCourseModel.completeable = completeable;

            curriculumCourseDao.edit(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
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
        return new CurriculumCourse(model.id, model.curriculumJid, model.courseJid, model.alias, courseDao.findByJid(model.courseJid).name, model.completeable);
    }
}
