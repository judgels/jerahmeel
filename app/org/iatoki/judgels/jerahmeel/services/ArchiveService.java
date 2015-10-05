package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ArchiveNotFoundException;
import org.iatoki.judgels.jerahmeel.ArchiveWithScore;

import java.util.List;

public interface ArchiveService {

    boolean archiveExistsByJid(String archiveJid);

    List<Archive> getAllArchives();

    List<ArchiveWithScore> getChildArchivesWithScore(String parentJid, String userJid);

    Archive findArchiveById(long archiveId) throws ArchiveNotFoundException;

    void createArchive(String parentJid, String name, String description);

    void updateArchive(String archiveJid, String parentJid, String name, String description);
}
