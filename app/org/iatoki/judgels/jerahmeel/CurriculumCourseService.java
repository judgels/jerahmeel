package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface CurriculumCourseService {

    boolean existByCurriculumJidAndCourseJid(String curriculumJid, String courseJid);

    CurriculumCourse findCurriculumCourseByCurriculumCourseId(long curriculumCourseId) throws CurriculumCourseNotFoundException;

    Page<CurriculumCourse> findCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<CurriculumCourseProgress> findCurriculumCourses(String userJid, String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addCurriculumCourse(String curriculumJid, String courseJid, boolean completeable);

    void removeCurriculumCourse(long curriculumCourseId) throws CurriculumCourseNotFoundException;
}
