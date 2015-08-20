package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;

public interface CurriculumService {

    Page<Curriculum> getPageOfCurriculums(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Curriculum findCurriculumById(long curriculumId) throws CurriculumNotFoundException;

    void createCurriculum(String name, String description);

    void updateCurriculum(long curriculumId, String name, String description) throws CurriculumNotFoundException;
}
