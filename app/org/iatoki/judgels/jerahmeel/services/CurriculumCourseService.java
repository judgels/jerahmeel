package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseWithProgress;
import org.iatoki.judgels.play.Page;

public interface CurriculumCourseService {

    boolean existsByCurriculumJidAndAlias(String curriculumJid, String alias);

    boolean existsByCurriculumJidAndCourseJid(String curriculumJid, String courseJid);

    CurriculumCourse findCurriculumCourseByCurriculumCourseId(long curriculumCourseId) throws CurriculumCourseNotFoundException;

    Page<CurriculumCourse> getPageOfCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<CurriculumCourseWithProgress> getPageOfCurriculumCoursesWithProgress(String userJid, String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    CurriculumCourse addCurriculumCourse(String curriculumJid, String courseJid, String alias, String userJid, String userIpAddress);

    void updateCurriculumCourse(long curriculumCourseId, String alias, String userJid, String userIpAddress);

    void removeCurriculumCourse(long curriculumCourseId);
}
