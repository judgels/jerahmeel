package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterModel;

final class ChapterServiceUtils {

    private ChapterServiceUtils() {
        // prevent instantiation
    }

    static Chapter createChapterFromModel(ChapterModel chapterModel) {
        return new Chapter(chapterModel.id, chapterModel.jid, chapterModel.name, chapterModel.description);
    }
}
