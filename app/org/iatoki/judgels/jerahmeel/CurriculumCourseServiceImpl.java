package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.domains.CurriculumCourseModel_;

import java.util.List;
import java.util.stream.Collectors;

public final class CurriculumCourseServiceImpl implements CurriculumCourseService {

    private final CurriculumDao curriculumDao;
    private final CurriculumCourseDao curriculumCourseDao;
    private final CourseDao courseDao;

    public CurriculumCourseServiceImpl(CurriculumDao curriculumDao, CurriculumCourseDao curriculumCourseDao, CourseDao courseDao) {
        this.curriculumDao = curriculumDao;
        this.curriculumCourseDao = curriculumCourseDao;
        this.courseDao = courseDao;
    }

    @Override
    public boolean existByCurriculumJidAndCourseJid(String curriculumJid, String courseJid) {
        return curriculumCourseDao.existByCurriculumJidAndCourseJid(curriculumJid, courseJid);
    }

    @Override
    public CurriculumCourse findByCurriculumCourseId(long curriculumCourseId) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            return new CurriculumCourse(curriculumCourseModel.id, curriculumCourseModel.curriculumJid, curriculumCourseModel.courseJid, null);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    @Override
    public Page<CurriculumCourse> findCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFilters(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of());
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CurriculumCourse> curriculumCourses = curriculumCourseModels.stream().map(s -> new CurriculumCourse(s.id, s.curriculumJid, s.courseJid, courseDao.findByJid(s.courseJid).name)).collect(Collectors.toList());

        return new Page<>(curriculumCourses, totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCurriculumCourse(String curriculumJid, String courseJid) {
        CurriculumCourseModel curriculumCourseModel = new CurriculumCourseModel();
        curriculumCourseModel.curriculumJid = curriculumJid;
        curriculumCourseModel.courseJid = courseJid;

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
}
