package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseModel;

import java.util.List;

public final class CourseServiceImpl implements CourseService {

    private final CourseDao courseDao;

    public CourseServiceImpl(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @Override
    public boolean existByCourseJid(String courseJid) {
        return courseDao.existsByJid(courseJid);
    }

    @Override
    public List<Course> findAllCourseByTerm(String term) {
        List<CourseModel> courses = courseDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<Course> courseBuilder = ImmutableList.builder();

        for (CourseModel course : courses) {
            courseBuilder.add(createCourseFromModel(course));
        }

        return courseBuilder.build();
    }

    @Override
    public Page<Course> pageCourses(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<CourseModel> courseModels = courseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Course> courses = Lists.transform(courseModels, m -> createCourseFromModel(m));

        return new Page<>(courses, totalPages, pageIndex, pageSize);
    }

    @Override
    public Course findCourseByCourseJid(String courseJid) {
        CourseModel courseModel = courseDao.findByJid(courseJid);
        return createCourseFromModel(courseModel);
    }

    @Override
    public Course findCourseByCourseId(long courseId) throws CourseNotFoundException {
        CourseModel courseModel = courseDao.findById(courseId);
        if (courseModel != null) {
            return createCourseFromModel(courseModel);
        } else {
            throw new CourseNotFoundException("Course not found.");
        }
    }

    @Override
    public void createCourse(String name, String description) {
        CourseModel courseModel = new CourseModel();
        courseModel.name = name;
        courseModel.description = description;

        courseDao.persist(courseModel, "michael", IdentityUtils.getIpAddress());
    }

    @Override
    public void updateCourse(long courseId, String name, String description) throws CourseNotFoundException {
        CourseModel courseModel = courseDao.findById(courseId);
        if (courseModel != null) {
            courseModel.name = name;
            courseModel.description = description;

            courseDao.edit(courseModel, "michael", IdentityUtils.getIpAddress());
        } else {
            throw new CourseNotFoundException("Course not found.");
        }
    }

    private Course createCourseFromModel(CourseModel courseModel) {
        return new Course(courseModel.id, courseModel.jid, courseModel.name, courseModel.description);
    }
}
