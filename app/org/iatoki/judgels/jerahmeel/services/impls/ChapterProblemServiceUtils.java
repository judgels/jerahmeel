package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.ChapterProblem;
import org.iatoki.judgels.jerahmeel.ChapterProblemStatus;
import org.iatoki.judgels.jerahmeel.ChapterProblemType;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterProblemModel;

final class ChapterProblemServiceUtils {

    private ChapterProblemServiceUtils() {
        // prevent instantiation
    }

    static ChapterProblem createFromModel(ChapterProblemModel model) {
        return new ChapterProblem(model.id, model.chapterJid, model.problemJid, model.problemSecret, model.alias, ChapterProblemType.valueOf(model.type), ChapterProblemStatus.valueOf(model.status));
    }
}
