package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface CourseService {

    boolean courseExistsByJid(String courseJid);

    List<Course> getCoursesByTerm(String term);

    Page<Course> getPageOfCourses(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Course findCourseById(long courseId) throws CourseNotFoundException;

    Course findCourseByJid(String courseJid);

    Course createCourse(String name, String description, String userJid, String userIpAddress);

    void updateCourse(String courseJid, String name, String description, String userJid, String userIpAddress);
}
