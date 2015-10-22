package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel_;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public final class ArchiveServiceUtils {

    private ArchiveServiceUtils() {
        //prevent instantiation
    }

    static Archive createArchiveFromModel(ArchiveModel archiveModel, Archive parentArchive) {
        return new Archive(archiveModel.id, archiveModel.jid, parentArchive, Lists.newArrayList(), archiveModel.name, archiveModel.description);
    }

    static List<Archive> getChildArchives(ArchiveDao archiveDao, String parentJid) {
        List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("name", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, parentJid), 0, -1);

        return archiveModels.stream().map(m -> createArchiveWithParentArchivesFromModel(archiveDao, m)).collect(Collectors.toList());
    }

    static Archive createArchiveWithParentArchivesFromModel(ArchiveDao archiveDao, ArchiveModel intendedArchiveModel) {
        Stack<ArchiveModel> archiveModelStack = new Stack<>();
        archiveModelStack.push(intendedArchiveModel);
        while (!archiveModelStack.peek().parentJid.isEmpty()) {
            archiveModelStack.push(archiveDao.findByJid(archiveModelStack.peek().parentJid));
        }

        Archive parentArchive = null;
        Archive intendedArchive = null;
        while (!archiveModelStack.isEmpty()) {
            ArchiveModel currentArchiveModel = archiveModelStack.pop();

            if (currentArchiveModel.jid.equals(intendedArchiveModel.jid) && (intendedArchive == null)) {
                intendedArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);
            } else {
                Archive currentArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);

                if (parentArchive != null) {
                    parentArchive.getSubArchives().add(currentArchive);
                }
                parentArchive = currentArchive;
            }
        }

        return intendedArchive;
    }
}
