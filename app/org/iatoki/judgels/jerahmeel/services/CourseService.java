package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;

import java.util.List;

public interface CourseService {

    boolean courseExistsByJid(String courseJid);

    List<Course> getCoursesByTerm(String term);

    Page<Course> getPageOfCourses(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Course findCourseByJid(String courseJid);

    Course findCourseById(long courseId) throws CourseNotFoundException;

    void createCourse(String name, String description);

    void updateCourse(long courseId, String name, String description) throws CourseNotFoundException;
}
