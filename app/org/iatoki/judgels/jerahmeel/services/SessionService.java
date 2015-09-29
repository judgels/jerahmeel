package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SessionService {

    boolean sessionExistsByJid(String sessionJid);

    List<Session> getSessionsByTerm(String term);

    Page<Session> getPageOfSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Session findSessionByJid(String sessionJid);

    Session findSessionById(long sessionId) throws SessionNotFoundException;

    Map<String, String> getSessionJidToNameMapBySessionJids(Collection<String> sessionJids);

    Session createSession(String name, String description, String userJid, String userIpAddress);

    void updateSession(String sessionJid, String name, String description, String userJid, String userIpAddress);
}
