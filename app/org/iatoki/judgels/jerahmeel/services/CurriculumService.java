package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface CurriculumService {

    List<Curriculum> getAllCurriculums();

    Page<Curriculum> getPageOfCurriculums(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Curriculum findCurriculumById(long curriculumId) throws CurriculumNotFoundException;

    Curriculum createCurriculum(String name, String description, String userJid, String userIpAddress);

    void updateCurriculum(String curriculumJid, String name, String description, String userJid, String userIpAddress);
}
