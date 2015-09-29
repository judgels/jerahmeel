package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.play.Page;

import java.util.Map;

public interface SessionProblemService {

    boolean aliasExistsInSession(String sessionJid, String alias);

    SessionProblem findSessionProblemById(long sessionProblemId) throws SessionProblemNotFoundException;

    Page<SessionProblem> getPageOfSessionProblems(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<SessionProblemProgress> getPageOfSessionProblemsProgress(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionProblem(String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status, String userJid, String userIpAddress);

    void updateSessionProblem(long sessionProblemId, String alias, SessionProblemStatus status, String userJid, String userIpAddress);

    void removeSessionProblem(long sessionProblemId);

    Map<String, String> getProgrammingProblemJidToAliasMapBySessionJid(String sessionJid);

    Map<String, String> getBundleProblemJidToAliasMapBySessionJid(String sessionJid);

    SessionProblem findSessionProblemBySessionJidAndProblemJid(String sessionJid, String problemJid);
}
