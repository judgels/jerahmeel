package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.jerahmeel.JerahmeelActivityKeys;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.ProblemSetNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.ProblemSetProblemService;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.archive.problemset.submission.programming.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class ProblemSetProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String PROGRAMMING_FILES = "programming_files";
    private static final String PROBLEM = "problem";
    private static final String PROBLEM_SET = "problem_set";

    private final FileSystemProvider programmingSubmissionLocalFileSystemProvider;
    private final FileSystemProvider programmingSubmissionRemoteFileSystemProvider;
    private final ProblemSetProblemService problemSetProblemService;
    private final ProblemSetService problemSetService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    @Inject
    public ProblemSetProgrammingSubmissionController(@ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider programmingSubmissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider programmingSubmissionRemoteFileSystemProvider, ProblemSetProblemService problemSetProblemService, ProblemSetService problemSetService, ProgrammingSubmissionService programmingSubmissionService) {
        this.programmingSubmissionLocalFileSystemProvider = programmingSubmissionLocalFileSystemProvider;
        this.programmingSubmissionRemoteFileSystemProvider = programmingSubmissionRemoteFileSystemProvider;
        this.problemSetProblemService = problemSetProblemService;
        this.problemSetService = problemSetService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Transactional
    public Result postSubmitProblem(long problemSetId, String problemJid) throws ProblemSetNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        ProblemSetProblem problemSetProblem = problemSetProblemService.findProblemSetProblemByProblemSetJidAndProblemJid(problemSet.getJid(), problemJid);

        if (problemSetProblem.getStatus() != ProblemSetProblemStatus.VISIBLE) {
            return notFound();
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

        String submissionJid;
        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(body);
            submissionJid = programmingSubmissionService.submit(problemJid, problemSet.getJid(), gradingEngine, gradingLanguage, null, submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            ProgrammingSubmissionUtils.storeSubmissionFiles(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submissionJid, submissionSource);

        } catch (ProgrammingSubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(routes.ProblemSetProblemController.viewProblemSetProblem(problemSet.getId(), problemSetProblem.getId()));
        }

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.SUBMIT.construct(PROBLEM_SET, problemSet.getJid(), problemSet.getName(), PROBLEM, problemSetProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(problemSetProblem.getProblemJid())), SUBMISSION, submissionJid, PROGRAMMING_FILES));

        return redirect(routes.ProblemSetProgrammingSubmissionController.viewSubmissions(problemSet.getId()));
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long problemSetId) throws ProblemSetNotFoundException {
        return listSubmissions(problemSetId, 0, "id", "desc", null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long problemSetId, long pageIndex, String orderBy, String orderDir, String problemJid) throws ProblemSetNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, problemSet.getJid());
        Map<String, String> problemJidToAliasMap = problemSetProblemService.getProgrammingProblemJidToAliasMapByProblemSetJid(problemSet.getJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(problemSet.getId(), pageOfSubmissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        ProblemSetSubmissionControllerUtils.appendSubtabLayout(content, problemSet);
        ProblemSetControllerUtils.appendTabLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Problem Sets - Programming Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long problemSetId, long submissionId) throws ProblemSetNotFoundException, ProgrammingSubmissionNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        ProgrammingSubmission submission = programmingSubmissionService.findProgrammingSubmissionById(submissionId);

        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        ProblemSetProblem problemSetProblem = problemSetProblemService.findProblemSetProblemByProblemSetJidAndProblemJid(problemSet.getJid(), submission.getProblemJid());
        String problemSetProblemAlias = problemSetProblem.getAlias();
        String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(problemSetProblem.getProblemJid());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(submission.getGradingEngine()).renderViewSubmission(submission, submissionSource, authorName, problemSetProblemAlias, sessionProblemName, gradingLanguageName, problemSet.getName()));
        ProblemSetSubmissionControllerUtils.appendSubtabLayout(content, problemSet);
        ProblemSetControllerUtils.appendTabLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, problemSet,
                new InternalLink(submission.getId() + "", routes.ProblemSetProgrammingSubmissionController.viewSubmission(problemSet.getId(), submission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Problem Sets - Programming Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, ProblemSet problemSet, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ProblemSetControllerUtils.getBreadcrumbsBuilder(problemSet);
        breadcrumbsBuilder.add(new InternalLink(problemSet.getName(), routes.ProblemSetController.jumpToProblems(problemSet.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("archive.problemSet.submissions"), routes.ProblemSetController.jumpToSubmissions(problemSet.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("archive.problemSet.submissions.programming"), routes.ProblemSetProgrammingSubmissionController.viewSubmissions(problemSet.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}