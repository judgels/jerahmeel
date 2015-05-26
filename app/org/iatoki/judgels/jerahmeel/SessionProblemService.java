package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface SessionProblemService {

    boolean isInSessionByProblemJidAndAlias(String sessionJid, String problemJid, String alias);

    SessionProblem findBySessionProblemId(long sessionProblemId) throws SessionProblemNotFoundException;

    Page<SessionProblem> findSessionProblems(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionProblem(String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status);

    void removeSessionProblem(long sessionProblemId) throws SessionProblemNotFoundException;
}
