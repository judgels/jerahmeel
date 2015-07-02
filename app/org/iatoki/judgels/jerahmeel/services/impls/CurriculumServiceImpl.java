package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumModel;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;

import java.util.List;

public final class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumDao curriculumDao;

    public CurriculumServiceImpl(CurriculumDao curriculumDao) {
        this.curriculumDao = curriculumDao;
    }

    @Override
    public Page<Curriculum> pageCurriculums(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<CurriculumModel> curriculumModels = curriculumDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Curriculum> curriculums = Lists.transform(curriculumModels, m -> createCurriculumFromModel(m));

        return new Page<>(curriculums, totalPages, pageIndex, pageSize);
    }

    @Override
    public Curriculum findCurriculumByCurriculumId(long curriculumId) throws CurriculumNotFoundException {
        CurriculumModel curriculumModel = curriculumDao.findById(curriculumId);
        if (curriculumModel != null) {
            return createCurriculumFromModel(curriculumModel);
        } else {
            throw new CurriculumNotFoundException("Curriculum not found.");
        }
    }

    @Override
    public void createCurriculum(String name, String description) {
        CurriculumModel curriculumModel = new CurriculumModel();
        curriculumModel.name = name;
        curriculumModel.description = description;

        curriculumDao.persist(curriculumModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateCurriculum(long curriculumId, String name, String description) throws CurriculumNotFoundException {
        CurriculumModel curriculumModel = curriculumDao.findById(curriculumId);
        if (curriculumModel != null) {
            curriculumModel.name = name;
            curriculumModel.description = description;

            curriculumDao.edit(curriculumModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            throw new CurriculumNotFoundException("Curriculum not found.");
        }
    }

    private Curriculum createCurriculumFromModel(CurriculumModel curriculumModel) {
        return new Curriculum(curriculumModel.id, curriculumModel.jid, curriculumModel.name, curriculumModel.description);
    }
}
