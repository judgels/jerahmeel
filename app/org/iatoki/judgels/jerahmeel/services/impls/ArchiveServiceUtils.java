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
                intendedArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);
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
                currentArchive.getSubArchives().add(archive);
                archiveStack.push(archive);
            }
        }

        return intendedArchive;
    }

    static List<Archive> getChildArchives(ArchiveDao archiveDao, String parentJid) {
        List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("name", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, parentJid), 0, -1);

        return archiveModels.stream().map(m -> createArchiveWithParentAndSubArchivesFromModel(archiveDao, m)).collect(Collectors.toList());
    }

    static Archive createArchiveWithParentAndSubArchivesFromModel(ArchiveDao archiveDao, ArchiveModel intendedArchiveModel) {
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
                intendedArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);
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
                currentArchive.getSubArchives().add(archive);
                archiveStack.push(archive);
            }
        }

        return intendedArchive;
    }
}
