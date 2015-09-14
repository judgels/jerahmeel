package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.forms.ListTableSelectionForm;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.session.submission.programming.listSubmissionsView;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
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
public final class SessionProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;
    private final FileSystemProvider submissionLocalFileSystemProvider;
    private final FileSystemProvider submissionRemoteFileSystemProvider;
    private final ProgrammingSubmissionService submissionService;
    private final UserItemService userItemService;

    @Inject
    public SessionProgrammingSubmissionController(SessionProblemService sessionProblemService, SessionService sessionService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider submissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider submissionRemoteFileSystemProvider, ProgrammingSubmissionService submissionService, UserItemService userItemService) {
        this.sessionProblemService = sessionProblemService;
        this.sessionService = sessionService;
        this.submissionLocalFileSystemProvider = submissionLocalFileSystemProvider;
        this.submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider;
        this.submissionService = submissionService;
        this.userItemService = userItemService;
    }

    @Transactional
    public Result postSubmitProblem(long sessionId, String problemJid) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), problemJid);

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(body);
            String submissionJid = submissionService.submit(problemJid, session.getJid(), gradingEngine, gradingLanguage, null, submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            ProgrammingSubmissionUtils.storeSubmissionFiles(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submissionJid, submissionSource);

            JerahmeelControllerUtils.getInstance().addActivityLog("Submit to problem " + sessionProblem.getAlias() + " in session " + session.getName() + ".");

        } catch (ProgrammingSubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(routes.SessionProblemController.viewProblem(sessionId, sessionProblem.getId()));
        }

        return redirect(routes.SessionProgrammingSubmissionController.viewSubmissions(sessionId));
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long sessionId) throws SessionNotFoundException {
        return listSubmissions(sessionId, 0, "id", "desc", null, null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long sessionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        String actualUserJid = "(none)".equals(userJid) ? null : userJid;
        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfSubmissions = submissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualUserJid, actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.getProgrammingProblemJidToAliasMapBySessionJid(session.getJid());
        List<UserItem> userItems = userItemService.getUserItemsByItemJid(session.getJid());
        List<String> userJids = Lists.transform(userItems, u -> u.getUserJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(session.getId(), pageOfSubmissions, userJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualUserJid, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        appendSubtabLayout(content, session);
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long sessionId, long submissionId) throws SessionNotFoundException, ProgrammingSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        ProgrammingSubmission submission = submissionService.findProgrammingSubmissionById(submissionId);

        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), submission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid()), SessionControllerUtils.getCurrentStatementLanguage());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(submission.getGradingEngine()).renderViewSubmission(submission, submissionSource, authorName, sessionProblemAlias, sessionProblemName, gradingLanguageName, session.getName()));
        appendSubtabLayout(content, session);
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(sessionProblemAlias, routes.SessionProgrammingSubmissionController.viewSubmission(session.getId(), submission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result regradeSubmission(long sessionId, long submissionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, ProgrammingSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        ProgrammingSubmission submission = submissionService.findProgrammingSubmissionById(submissionId);
        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        submissionService.regrade(submission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    @Transactional
    public Result regradeSubmissions(long sessionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, ProgrammingSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

        List<ProgrammingSubmission> submissions;

        if (data.selectAll) {
            submissions = submissionService.getProgrammingSubmissionsByFilters(orderBy, orderDir, userJid, problemJid, session.getJid());
        } else if (data.selectJids != null) {
            submissions = submissionService.getProgrammingSubmissionsByJids(data.selectJids);
        } else {
            return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
        }

        for (ProgrammingSubmission submission : submissions) {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
            submissionService.regrade(submission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    private void appendSubtabLayout(LazyHtml content, Session session) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())),
                        new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId()))
                ), c)
        );
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SessionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.submissions"), routes.SessionController.jumpToSubmissions(session.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.SessionProgrammingSubmissionController.viewSubmissions(session.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
