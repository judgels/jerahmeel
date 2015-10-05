package org.iatoki.judgels.jerahmeel.services.impls;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel_;

import java.util.List;
import java.util.Stack;

public final class ArchiveServiceUtils {

    private ArchiveServiceUtils() {
        //prevent instantiation
    }

    static Archive createArchiveFromModel(ArchiveModel archiveModel, Archive parentArchive) {
        return new Archive(archiveModel.id, archiveModel.jid, parentArchive, Lists.newArrayList(), archiveModel.name, archiveModel.description);
    }

    static Archive createArchiveWithParentsFromModel(ArchiveDao archiveDao, ArchiveModel intendedArchiveModel) {
        Stack<ArchiveModel> archiveModelStack = new Stack<>();
        archiveModelStack.push(intendedArchiveModel);
        while (!archiveModelStack.peek().parentJid.equals("")) {
            archiveModelStack.push(archiveDao.findByJid(archiveModelStack.peek().parentJid));
        }

        Archive parentArchive = null;
        Archive intendedArchive = null;
        while (!archiveModelStack.isEmpty()) {
            ArchiveModel currentArchiveModel = archiveModelStack.pop();

            if (currentArchiveModel.jid.equals(intendedArchiveModel.jid) && (intendedArchive == null)) {
                Archive currentArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);
                intendedArchive = currentArchive;
            } else {
                Archive currentArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);

                if (parentArchive != null) {
                    parentArchive.getSubArchives().add(currentArchive);
                }
                parentArchive = currentArchive;
            }
        }

        Stack<Archive> archiveStack = new Stack<>();
        archiveStack.add(intendedArchive);

        while (!archiveStack.isEmpty()) {
            Archive currentArchive = archiveStack.pop();

            List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, currentArchive.getJid()), 0, -1);
            for (ArchiveModel archiveModel : archiveModels) {
                Archive archive = ArchiveServiceUtils.createArchiveFromModel(archiveModel, currentArchive);
                if (currentArchive != null) {
                    currentArchive.getSubArchives().add(archive);
                }
                archiveStack.push(archive);
            }
        }

        return intendedArchive;
    }
}
