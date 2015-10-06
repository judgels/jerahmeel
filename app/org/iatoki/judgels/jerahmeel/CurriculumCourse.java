package org.iatoki.judgels.jerahmeel;

public final class CurriculumCourse {

    private final long id;
    private final String curriculumJid;
    private final String courseJid;
    private final String alias;
    private final String courseName;

    public CurriculumCourse(long id, String curriculumJid, String courseJid, String alias, String courseName) {
        this.id = id;
        this.curriculumJid = curriculumJid;
        this.courseJid = courseJid;
        this.alias = alias;
        this.courseName = courseName;
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

    public String getAlias() {
        return alias;
    }

    public String getCourseName() {
        return courseName;
    }
}
