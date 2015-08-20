package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;

import java.util.List;

public interface SessionService {

    boolean sessionExistsByJid(String sessionJid);

    List<Session> getSessionsByTerm(String term);

    Page<Session> getPageOfSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Session findSessionByJid(String sessionJid);

    Session findSessionById(long sessionId) throws SessionNotFoundException;

    void createSession(String name, String description);

    void updateSession(long sessionId, String name, String description) throws SessionNotFoundException;
}
