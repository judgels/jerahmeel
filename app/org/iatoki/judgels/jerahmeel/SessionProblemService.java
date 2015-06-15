package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

import java.util.Map;

public interface SessionProblemService {

    boolean isInSessionByAlias(String sessionJid, String alias);

    SessionProblem findSessionProblemBySessionProblemId(long sessionProblemId) throws SessionProblemNotFoundException;

    Page<SessionProblem> findSessionProblems(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<SessionProblemProgress> findSessionProblems(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionProblem(String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status);

    void removeSessionProblem(long sessionProblemId) throws SessionProblemNotFoundException;

    Map<String, String> findProgrammingProblemJidToAliasMapBySessionJid(String sessionJid);

    Map<String, String> findBundleProblemJidToAliasMapBySessionJid(String sessionJid);

    SessionProblem findSessionProblemBySessionJidAndProblemJid(String sessionJid, String problemJid);
}
