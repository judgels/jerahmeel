package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.jerahmeel.JerahmeelActivityKeys;
import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.jerahmeel.ChapterNotFoundException;
import org.iatoki.judgels.jerahmeel.ChapterProblem;
import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.ChapterProblemService;
import org.iatoki.judgels.jerahmeel.services.ChapterService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.chapter.submission.programming.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.forms.ListTableSelectionForm;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class ChapterProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String PROGRAMMING_FILES = "programming_files";
    private static final String PROBLEM = "problem";
    private static final String CHAPTER = "chapter";

    private final ChapterProblemService chapterProblemService;
    private final ChapterService chapterService;
    private final FileSystemProvider submissionLocalFileSystemProvider;
    private final FileSystemProvider submissionRemoteFileSystemProvider;
    private final ProgrammingSubmissionService submissionService;
    private final UserItemService userItemService;

    @Inject
    public ChapterProgrammingSubmissionController(ChapterProblemService chapterProblemService, ChapterService chapterService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider submissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider submissionRemoteFileSystemProvider, ProgrammingSubmissionService submissionService, UserItemService userItemService) {
        this.chapterProblemService = chapterProblemService;
        this.chapterService = chapterService;
        this.submissionLocalFileSystemProvider = submissionLocalFileSystemProvider;
        this.submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider;
        this.submissionService = submissionService;
        this.userItemService = userItemService;
    }

    @Transactional
    public Result postSubmitProblem(long chapterId, String problemJid) throws ChapterNotFoundException {
        Chapter chapter = chapterService.findChapterById(chapterId);
        ChapterProblem chapterProblem = chapterProblemService.findChapterProblemByChapterJidAndProblemJid(chapter.getJid(), problemJid);

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

        String submissionJid;
        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(body);
            submissionJid = submissionService.submit(problemJid, chapter.getJid(), gradingEngine, gradingLanguage, null, submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            ProgrammingSubmissionUtils.storeSubmissionFiles(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submissionJid, submissionSource);

        } catch (ProgrammingSubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(routes.ChapterProblemController.viewChapterProblem(chapterId, chapterProblem.getId()));
        }

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.SUBMIT.construct(CHAPTER, chapter.getJid(), chapter.getName(), PROBLEM, chapterProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(chapterProblem.getProblemJid())), SUBMISSION, submissionJid, PROGRAMMING_FILES));

        return redirect(routes.ChapterProgrammingSubmissionController.viewSubmissions(chapterId));
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long chapterId) throws ChapterNotFoundException {
        return listSubmissions(chapterId, 0, "id", "desc", null, null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long chapterId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws ChapterNotFoundException {
        Chapter chapter = chapterService.findChapterById(chapterId);

        String actualUserJid = "(none)".equals(userJid) ? null : userJid;
        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfSubmissions = submissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualUserJid, actualProblemJid, chapter.getJid());
        Map<String, String> problemJidToAliasMap = chapterProblemService.getProgrammingProblemJidToAliasMapByChapterJid(chapter.getJid());
        List<UserItem> userItems = userItemService.getUserItemsByItemJid(chapter.getJid());
        List<String> userJids = Lists.transform(userItems, u -> u.getUserJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(chapter.getId(), pageOfSubmissions, userJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualUserJid, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        appendSubtabLayout(content, chapter);
        ChapterControllerUtils.appendTabLayout(content, chapter);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, chapter);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Chapters - Programming Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long chapterId, long submissionId) throws ChapterNotFoundException, ProgrammingSubmissionNotFoundException {
        Chapter chapter = chapterService.findChapterById(chapterId);
        ProgrammingSubmission submission = submissionService.findProgrammingSubmissionById(submissionId);

        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        ChapterProblem chapterProblem = chapterProblemService.findChapterProblemByChapterJidAndProblemJid(chapter.getJid(), submission.getProblemJid());
        String chapterProblemAlias = chapterProblem.getAlias();
        String chapterProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(chapterProblem.getProblemJid()), StatementControllerUtils.getCurrentStatementLanguage());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(submission.getGradingEngine()).renderViewSubmission(submission, submissionSource, authorName, chapterProblemAlias, chapterProblemName, gradingLanguageName, chapter.getName()));
        appendSubtabLayout(content, chapter);
        ChapterControllerUtils.appendTabLayout(content, chapter);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, chapter,
                new InternalLink(chapterProblemAlias, routes.ChapterProgrammingSubmissionController.viewSubmission(chapter.getId(), submission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Chapters - Programming Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result regradeSubmission(long chapterId, long submissionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws ChapterNotFoundException, ProgrammingSubmissionNotFoundException {
        Chapter chapter = chapterService.findChapterById(chapterId);

        ProgrammingSubmission submission = submissionService.findProgrammingSubmissionById(submissionId);
        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        submissionService.regrade(submission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.REGRADE.construct(CHAPTER, chapter.getJid(), chapter.getName(), PROBLEM, submission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(submission.getProblemJid()), SUBMISSION, submission.getJid(), submission.getId() + ""));

        return redirect(routes.ChapterProgrammingSubmissionController.listSubmissions(chapterId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    @Transactional
    public Result regradeSubmissions(long chapterId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws ChapterNotFoundException, ProgrammingSubmissionNotFoundException {
        Chapter chapter = chapterService.findChapterById(chapterId);

        ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

        List<ProgrammingSubmission> submissions;

        if (data.selectAll) {
            submissions = submissionService.getProgrammingSubmissionsByFilters(orderBy, orderDir, userJid, problemJid, chapter.getJid());
        } else if (data.selectJids != null) {
            submissions = submissionService.getProgrammingSubmissionsByJids(data.selectJids);
        } else {
            return redirect(routes.ChapterProgrammingSubmissionController.listSubmissions(chapterId, pageIndex, orderBy, orderDir, userJid, problemJid));
        }

        for (ProgrammingSubmission submission : submissions) {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
            submissionService.regrade(submission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.REGRADE.construct(CHAPTER, chapter.getJid(), chapter.getName(), PROBLEM, submission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(submission.getProblemJid()), SUBMISSION, submission.getJid(), submission.getId() + ""));
        }

        return redirect(routes.ChapterProgrammingSubmissionController.listSubmissions(chapterId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    private void appendSubtabLayout(LazyHtml content, Chapter chapter) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("chapter.submissions.programming"), routes.ChapterController.jumpToProgrammingSubmissions(chapter.getId())),
                        new InternalLink(Messages.get("chapter.submissions.bundle"), routes.ChapterController.jumpToBundleSubmissions(chapter.getId()))
                ), c)
        );
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Chapter chapter, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ChapterControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("chapter.submissions"), routes.ChapterController.jumpToSubmissions(chapter.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("chapter.submissions.programming"), routes.ChapterController.jumpToProgrammingSubmissions(chapter.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.ChapterProgrammingSubmissionController.viewSubmissions(chapter.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
