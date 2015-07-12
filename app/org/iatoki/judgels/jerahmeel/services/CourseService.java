package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;

import java.util.List;

public interface CourseService {

    boolean existByCourseJid(String courseJid);

    List<Course> findAllCourseByTerm(String term);

    Page<Course> pageCourses(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Course findCourseByCourseJid(String courseJid);

    Course findCourseByCourseId(long courseId) throws CourseNotFoundException;

    void createCourse(String name, String description);

    void updateCourse(long courseId, String name, String description) throws CourseNotFoundException;
}
