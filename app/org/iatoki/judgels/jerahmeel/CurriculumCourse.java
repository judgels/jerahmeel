package org.iatoki.judgels.jerahmeel;

public final class CurriculumCourse {

    private final long id;
    private final String curriculumJid;
    private final String courseJid;
    private final String courseName;
    private final boolean completeable;

    public CurriculumCourse(long id, String curriculumJid, String courseJid, String courseName, boolean completeable) {
        this.id = id;
        this.curriculumJid = curriculumJid;
        this.courseJid = courseJid;
        this.courseName = courseName;
        this.completeable = completeable;
    }

    public long getId() {
        return id;
    }

    public String getCurriculumJid() {
        return curriculumJid;
    }

    public String getCourseJid() {
        return courseJid;
    }

    public String getCourseName() {
        return courseName;
    }

    public boolean isCompleteable() {
        return completeable;
    }
}
