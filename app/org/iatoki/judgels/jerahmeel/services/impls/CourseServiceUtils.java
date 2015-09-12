package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.models.entities.CourseModel;

final class CourseServiceUtils {

    private CourseServiceUtils() {
        // prevent instantiation
    }

    static Course createCourseFromModel(CourseModel courseModel) {
        return new Course(courseModel.id, courseModel.jid, courseModel.name, courseModel.description);
    }
}
