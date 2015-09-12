package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel;

final class SessionServiceUtils {

    private SessionServiceUtils() {
        // prevent instantiation
    }

    static Session createSessionFromModel(SessionModel sessionModel) {
        return new Session(sessionModel.id, sessionModel.jid, sessionModel.name, sessionModel.description);
    }
}
