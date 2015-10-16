package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;

public final class ProblemSetServiceUtils {

    private ProblemSetServiceUtils() {
        // prevent instantiation
    }

    static ProblemSet createProblemSetFromModelAndArchive(ProblemSetModel problemSetModel, Archive archive) {
        return new ProblemSet(problemSetModel.id, problemSetModel.jid, archive, problemSetModel.name, problemSetModel.description);
    }
}
