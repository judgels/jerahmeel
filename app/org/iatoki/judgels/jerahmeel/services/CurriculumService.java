package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;

public interface CurriculumService {

    Page<Curriculum> pageCurriculums(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Curriculum findCurriculumByCurriculumId(long curriculumId) throws CurriculumNotFoundException;

    void createCurriculum(String name, String description);

    void updateCurriculum(long curriculumId, String name, String description) throws CurriculumNotFoundException;
    
}
