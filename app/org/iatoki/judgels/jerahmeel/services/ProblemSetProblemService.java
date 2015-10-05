package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemWithScore;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemType;
import org.iatoki.judgels.play.Page;

import java.util.Map;

public interface ProblemSetProblemService {

    boolean aliasExistsInProblemSet(String problemSetJid, String alias);

    ProblemSetProblem findProblemSetProblemById(long problemSetProblemId) throws ProblemSetProblemNotFoundException;

    Page<ProblemSetProblem> getPageOfProblemSetProblems(String problemSetJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ProblemSetProblemWithScore> getPageOfProblemSetProblemsWithScore(String userJid, String problemSetJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addProblemSetProblem(String problemSetJid, String problemJid, String problemSecret, String alias, ProblemSetProblemType type, ProblemSetProblemStatus status, String userJid, String userIpAddress);

    void updateProblemSetProblem(long problemSetProblemId, String alias, ProblemSetProblemStatus status, String userJid, String userIpAddress);

    void removeProblemSetProblem(long problemSetProblemId);

    Map<String, String> getProgrammingProblemJidToAliasMapByProblemSetJid(String problemSetJid);

    Map<String, String> getBundleProblemJidToAliasMapByProblemSetJid(String problemSetJid);

    ProblemSetProblem findProblemSetProblemByProblemSetJidAndProblemJid(String problemSetJid, String problemJid);
}
