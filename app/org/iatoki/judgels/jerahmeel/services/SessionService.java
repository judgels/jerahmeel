package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;

import java.util.List;

public interface SessionService {

    boolean existBySessionJid(String sessionJid);

    List<Session> findAllSessionByTerm(String term);

    Page<Session> pageSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Session findSessionBySessionJid(String sessionJid);

    Session findSessionBySessionId(long sessionId) throws SessionNotFoundException;

    void createSession(String name, String description);

    void updateSession(long sessionId, String name, String description) throws SessionNotFoundException;
}
