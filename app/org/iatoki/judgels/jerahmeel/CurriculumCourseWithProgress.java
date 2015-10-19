package org.iatoki.judgels.jerahmeel;

public final class CurriculumCourseWithProgress {

    private final CurriculumCourse curriculumCourse;
    private final CourseProgress courseProgress;
    private final long completedSessions;
    private final long totalSessions;
    private final double totalScores;

    public CurriculumCourseWithProgress(CurriculumCourse curriculumCourse, CourseProgress courseProgress, long completedSessions, long totalSessions, double totalScores) {
        this.curriculumCourse = curriculumCourse;
        this.courseProgress = courseProgress;
        this.completedSessions = completedSessions;
        this.totalSessions = totalSessions;
        this.totalScores = totalScores;
    }

    public CurriculumCourse getCurriculumCourse() {
        return curriculumCourse;
    }

    public CourseProgress getCourseProgress() {
        return courseProgress;
    }

    public long getCompletedSessions() {
        return completedSessions;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public double getTotalScores() {
        return totalScores;
    }
}
