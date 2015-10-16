package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;

final class SessionProblemServiceUtils {

    private SessionProblemServiceUtils() {
        // prevent instantiation
    }

    static SessionProblem createFromModel(SessionProblemModel model) {
        return new SessionProblem(model.id, model.sessionJid, model.problemJid, model.problemSecret, model.alias, SessionProblemType.valueOf(model.type), SessionProblemStatus.valueOf(model.status));
    }
}
