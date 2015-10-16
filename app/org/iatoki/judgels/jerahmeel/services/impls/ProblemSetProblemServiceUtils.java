package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemType;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;

final class ProblemSetProblemServiceUtils {

    private ProblemSetProblemServiceUtils() {
        // prevent instantiation
    }

    static ProblemSetProblem createFromModel(ProblemSetProblemModel model) {
        return new ProblemSetProblem(model.id, model.problemSetJid, model.problemJid, model.problemSecret, model.alias, ProblemSetProblemType.valueOf(model.type), ProblemSetProblemStatus.valueOf(model.status));
    }
}
