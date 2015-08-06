package org.iatoki.judgels.jerahmeel;

public class CourseSessionProgress {

    private final CourseSession courseSession;
    private final SessionProgress sessionProgress;
    private final long solvedProblems;
    private final long totalProblems;
    private final double totalScores;

    public CourseSessionProgress(CourseSession courseSession, SessionProgress sessionProgress, long solvedProblems, long totalProblems, double totalScores) {
        this.courseSession = courseSession;
        this.sessionProgress = sessionProgress;
        this.solvedProblems = solvedProblems;
        this.totalProblems = totalProblems;
        this.totalScores = totalScores;
    }

    public CourseSession getCourseSession() {
        return courseSession;
    }

    public SessionProgress getSessionProgress() {
        return sessionProgress;
    }

    public long getSolvedProblems() {
        return solvedProblems;
    }

    public long getTotalProblems() {
        return totalProblems;
    }

    public double getTotalScores() {
        return totalScores;
    }
}
