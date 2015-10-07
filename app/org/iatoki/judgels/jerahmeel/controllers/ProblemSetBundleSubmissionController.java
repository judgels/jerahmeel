package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.JerahmeelActivityKeys;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.ProblemSetNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.ProblemSetProblemService;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.archive.problemset.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleDetailResult;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.problem.bundle.submission.bundleSubmissionView;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class ProblemSetBundleSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String BUNDLE_ANSWER = "bundle answer";
    private static final String PROBLEM = "problem";
    private static final String PROBLEM_SET = "problem set";

    private final FileSystemProvider bundleSubmissionLocalFileSystemProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileSystemProvider;
    private final BundleSubmissionService bundleSubmissionService;
    private final ProblemSetProblemService problemSetProblemService;
    private final ProblemSetService problemSetService;

    @Inject
    public ProblemSetBundleSubmissionController(@BundleSubmissionLocalFileSystemProvider FileSystemProvider bundleSubmissionLocalFileSystemProvider, @BundleSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider bundleSubmissionRemoteFileSystemProvider, BundleSubmissionService bundleSubmissionService, ProblemSetProblemService problemSetProblemService, ProblemSetService problemSetService) {
        this.bundleSubmissionLocalFileSystemProvider = bundleSubmissionLocalFileSystemProvider;
        this.bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider;
        this.bundleSubmissionService = bundleSubmissionService;
        this.problemSetProblemService = problemSetProblemService;
        this.problemSetService = problemSetService;
    }

    @Transactional
    public Result postSubmitProblem(long problemSetId, String problemJid) throws ProblemSetNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        ProblemSetProblem problemSetProblem = problemSetProblemService.findProblemSetProblemByProblemSetJidAndProblemJid(problemSet.getJid(), problemJid);

        if (problemSetProblem.getStatus() != ProblemSetProblemStatus.VISIBLE) {
            return notFound();
        }

        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        BundleAnswer bundleAnswer = bundleSubmissionService.createBundleAnswerFromNewSubmission(dForm, SessionControllerUtils.getCurrentStatementLanguage());
        String submissionJid = bundleSubmissionService.submit(problemSetProblem.getProblemJid(), problemSet.getJid(), bundleAnswer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        bundleSubmissionService.storeSubmissionFiles(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, submissionJid, bundleAnswer);

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.SUBMIT.construct(PROBLEM_SET, problemSet.getJid(), problemSet.getName(), PROBLEM, problemSetProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(problemSetProblem.getProblemJid())), SUBMISSION, submissionJid, BUNDLE_ANSWER));

        return redirect(routes.ProblemSetBundleSubmissionController.viewSubmissions(problemSet.getId()));
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long problemSetId) throws ProblemSetNotFoundException {
        return listSubmissions(problemSetId, 0, "id", "desc", null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long problemSetId, long pageIndex, String orderBy, String orderDir, String problemJid) throws ProblemSetNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, problemSet.getJid());
        Map<String, String> problemJidToAliasMap = problemSetProblemService.getBundleProblemJidToAliasMapByProblemSetJid(problemSet.getJid());

        LazyHtml content = new LazyHtml(listSubmissionsView.render(problemSet.getId(), pageOfBundleSubmissions, problemJidToAliasMap, pageIndex, orderBy, orderDir, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        ProblemSetSubmissionControllerUtils.appendSubtabLayout(content, problemSet);
        ProblemSetControllerUtils.appendTabLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Problem Sets - Bundle Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long problemSetId, long bundleSubmissionId) throws ProblemSetNotFoundException, BundleSubmissionNotFoundException {
        ProblemSet problemSet = problemSetService.findProblemSetById(problemSetId);

        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);
        BundleAnswer bundleAnswer;
        try {
            bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ProblemSetProblem problemSetProblem = problemSetProblemService.findProblemSetProblemByProblemSetJidAndProblemJid(problemSet.getJid(), bundleSubmission.getProblemJid());
        String problemSetProblemAlias = problemSetProblem.getAlias();
        String problemSetProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(problemSetProblem.getProblemJid()), DeprecatedControllerUtils.getHardcodedDefaultLanguage());

        LazyHtml content = new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<LinkedHashMap<String, BundleDetailResult>>() { }.getType()), bundleAnswer, JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), problemSetProblemAlias, problemSetProblemName, problemSet.getName()));
        ProblemSetSubmissionControllerUtils.appendSubtabLayout(content, problemSet);
        ProblemSetControllerUtils.appendTabLayout(content, problemSet);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, problemSet,
                new InternalLink(bundleSubmission.getId() + "", routes.ProblemSetBundleSubmissionController.viewSubmission(problemSet.getId(), bundleSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Problem Sets - Bundle Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, ProblemSet problemSet, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ProblemSetControllerUtils.getBreadcrumbsBuilder(problemSet);
        breadcrumbsBuilder.add(new InternalLink(problemSet.getName(), routes.ProblemSetController.jumpToProblems(problemSet.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("archive.problemSet.submissions"), routes.ProblemSetController.jumpToSubmissions(problemSet.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("archive.problemSet.submissions.bundle"), routes.ProblemSetBundleSubmissionController.viewSubmissions(problemSet.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
