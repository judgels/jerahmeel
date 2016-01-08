package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.ChapterProblem;
import org.iatoki.judgels.jerahmeel.ChapterProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.ChapterProblemWithProgress;
import org.iatoki.judgels.jerahmeel.ChapterProblemStatus;
import org.iatoki.judgels.jerahmeel.ChapterProblemType;
import org.iatoki.judgels.play.Page;

import java.util.Map;

public interface ChapterProblemService {

    boolean aliasExistsInChapter(String chapterJid, String alias);

    ChapterProblem findChapterProblemById(long chapterProblemId) throws ChapterProblemNotFoundException;

    Page<ChapterProblem> getPageOfChapterProblems(String chapterJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ChapterProblemWithProgress> getPageOfChapterProblemsWithProgress(String userJid, String chapterJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addChapterProblem(String chapterJid, String problemJid, String problemSecret, String alias, ChapterProblemType type, ChapterProblemStatus status, String userJid, String userIpAddress);

    void updateChapterProblem(long chapterProblemId, String alias, ChapterProblemStatus status, String userJid, String userIpAddress);

    void removeChapterProblem(long chapterProblemId);

    Map<String, String> getProgrammingProblemJidToAliasMapByChapterJid(String chapterJid);

    Map<String, String> getBundleProblemJidToAliasMapByChapterJid(String chapterJid);

    ChapterProblem findChapterProblemByChapterJidAndProblemJid(String chapterJid, String problemJid);
}
