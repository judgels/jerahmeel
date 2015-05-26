package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface CourseService {

    boolean existByCourseJid(String courseJid);

    List<Course> findAllCourseByTerm(String term);

    Page<Course> pageCourses(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Course findByCourseId(long courseId) throws CourseNotFoundException;

    void createCourse(String name, String description);

    void updateCourse(long courseId, String name, String description) throws CourseNotFoundException;
}
