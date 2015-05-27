package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface SessionService {

    boolean existBySessionJid(String sessionJid);

    List<Session> findAllSessionByTerm(String term);

    Page<Session> pageSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Session findBySessionJid(String sessionJid);

    Session findBySessionId(long sessionId) throws SessionNotFoundException;

    void createSession(String name, String description);

    void updateSession(long sessionId, String name, String description) throws SessionNotFoundException;
}
