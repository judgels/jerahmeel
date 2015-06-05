package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface CurriculumService {

    Page<Curriculum> pageCurriculums(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Curriculum findCurriculumByCurriculumId(long curriculumId) throws CurriculumNotFoundException;

    void createCurriculum(String name, String description);

    void updateCurriculum(long curriculumId, String name, String description) throws CurriculumNotFoundException;
    
}
