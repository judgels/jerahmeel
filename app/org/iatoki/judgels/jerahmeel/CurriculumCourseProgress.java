package org.iatoki.judgels.jerahmeel;

public class CurriculumCourseProgress {

    private final CurriculumCourse curriculumCourse;
    private final CourseProgress courseProgress;

    public CurriculumCourseProgress(CurriculumCourse curriculumCourse, CourseProgress courseProgress) {
        this.curriculumCourse = curriculumCourse;
        this.courseProgress = courseProgress;
    }

    public CurriculumCourse getCurriculumCourse() {
        return curriculumCourse;
    }

    public CourseProgress getCourseProgress() {
        return courseProgress;
    }
}
