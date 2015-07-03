package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.ListTableSelectionForm;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.submission.programming.listSubmissionsView;
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.sandalphon.SubmissionAdapters;
import org.iatoki.judgels.sandalphon.SubmissionException;
import org.iatoki.judgels.sandalphon.SubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
@Singleton
@Named
public final class SessionProgrammingSubmissionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SubmissionService submissionService;
    private final SessionProblemService sessionProblemService;
    private final FileSystemProvider submissionLocalFileSystemProvider;
    private final FileSystemProvider submissionRemoteFileSystemProvider;
    private final UserItemService userItemService;

    @Inject
    public SessionProgrammingSubmissionController(SessionService sessionService, SubmissionService submissionService, SessionProblemService sessionProblemService, FileSystemProvider submissionLocalFileSystemProvider, FileSystemProvider submissionRemoteFileSystemProvider, UserItemService userItemService) {
        this.sessionService = sessionService;
        this.submissionService = submissionService;
        this.sessionProblemService = sessionProblemService;
        this.submissionLocalFileSystemProvider = submissionLocalFileSystemProvider;
        this.submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider;
        this.userItemService = userItemService;
    }

    @Transactional
    public Result postSubmitProblem(long sessionId, String problemJid) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), problemJid);

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

        try {
            GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
            String submissionJid = submissionService.submit(problemJid, session.getJid(), gradingEngine, gradingLanguage, null, source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submissionJid, source);

            ControllerUtils.getInstance().addActivityLog("Submit to problem " + sessionProblem.getAlias() + " in session " + session.getName() + ".");

        } catch (SubmissionException e) {
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
        Session session = sessionService.findSessionBySessionId(sessionId);

        String actualUserJid = "(none)".equals(userJid) ? null : userJid;
        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualUserJid, actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.findProgrammingProblemJidToAliasMapBySessionJid(session.getJid());
        List<UserItem> userItems = userItemService.findAllUserItemByItemJid(session.getJid());
        List<String> userJids = Lists.transform(userItems, u -> u.getUserJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(session.getId(), submissions, userJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualUserJid, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())),
                new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId()))
              ), c)
        );
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.submissions"), routes.SessionController.jumpToSubmissions(session.getId())),
              new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProgrammingSubmissionController.viewSubmissions(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long sessionId, long submissionId) throws SessionNotFoundException, SubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Submission submission = submissionService.findSubmissionById(submissionId);

        GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), submission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).renderViewSubmission(submission, source, authorName, sessionProblemAlias, sessionProblemName, gradingLanguageName, session.getName()));

        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())),
                    new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId()))
        ), c)
        );
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.submissions"), routes.SessionController.jumpToSubmissions(session.getId())),
              new InternalLink(Messages.get("session.submissions.programming"), routes.SessionController.jumpToProgrammingSubmissions(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProgrammingSubmissionController.viewSubmissions(session.getId())),
              new InternalLink(sessionProblemAlias, routes.SessionProgrammingSubmissionController.viewSubmission(session.getId(), submission.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions - View");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result regradeSubmission(long sessionId, long submissionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, SubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Submission submission = submissionService.findSubmissionById(submissionId);
        GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
        submissionService.regrade(submission.getJid(), source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    @Transactional
    public Result regradeSubmissions(long sessionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, SubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

        List<Submission> submissions;

        if (data.selectAll) {
            submissions = submissionService.findSubmissionsWithoutGradingsByFilters(orderBy, orderDir, userJid, problemJid, session.getJid());
        } else if (data.selectJids != null) {
            submissions = submissionService.findSubmissionsWithoutGradingsByJids(data.selectJids);
        } else {
            return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
        }

        for (Submission submission : submissions) {
            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionLocalFileSystemProvider, submissionRemoteFileSystemProvider, submission.getJid());
            submissionService.regrade(submission.getJid(), source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return redirect(routes.SessionProgrammingSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }
}
