package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.JerahmeelActivityKeys;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.session.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.forms.ListTableSelectionForm;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleDetailResult;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.problem.bundle.submission.bundleSubmissionView;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class SessionBundleSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String BUNDLE_ANSWER = "bundle answer";
    private static final String PROBLEM = "problem";
    private static final String SESSION = "session";

    private final FileSystemProvider bundleSubmissionLocalFileSystemProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileSystemProvider;
    private final BundleSubmissionService bundleSubmissionService;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;
    private final UserItemService userItemService;

    @Inject
    public SessionBundleSubmissionController(@BundleSubmissionLocalFileSystemProvider FileSystemProvider bundleSubmissionLocalFileSystemProvider, @BundleSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider bundleSubmissionRemoteFileSystemProvider, BundleSubmissionService bundleSubmissionService, SessionProblemService sessionProblemService, SessionService sessionService, UserItemService userItemService) {
        this.bundleSubmissionLocalFileSystemProvider = bundleSubmissionLocalFileSystemProvider;
        this.bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider;
        this.bundleSubmissionService = bundleSubmissionService;
        this.sessionService = sessionService;
        this.sessionProblemService = sessionProblemService;
        this.userItemService = userItemService;
    }

    @Transactional
    public Result postSubmitProblem(long sessionId, String problemJid) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), problemJid);

        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromNewSubmission(dForm, SessionControllerUtils.getCurrentStatementLanguage());
        String submissionJid = bundleSubmissionService.submit(sessionProblem.getProblemJid(), session.getJid(), answer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        bundleSubmissionService.storeSubmissionFiles(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, submissionJid, answer);

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.SUBMIT.construct(SESSION, session.getJid(), session.getName(), PROBLEM, sessionProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid())), SUBMISSION, submissionJid, BUNDLE_ANSWER));

        return redirect(routes.SessionBundleSubmissionController.viewSubmissions(sessionId));
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

        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualUserJid, actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.getBundleProblemJidToAliasMapBySessionJid(session.getJid());
        List<UserItem> userItems = userItemService.getUserItemsByItemJid(session.getJid());
        List<String> userJids = Lists.transform(userItems, u -> u.getUserJid());

        LazyHtml content = new LazyHtml(listSubmissionsView.render(session.getId(), pageOfBundleSubmissions, userJids, problemJidToAliasMap, pageIndex, orderBy, orderDir, actualUserJid, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        appendSubtabLayout(content, session);
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long sessionId, long bundleSubmissionId) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);
        BundleAnswer bundleAnswer;
        try {
            bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), bundleSubmission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid()), SessionControllerUtils.getCurrentStatementLanguage());

        LazyHtml content = new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<Map<String, BundleDetailResult>>() { }.getType()), bundleAnswer, JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), sessionProblemAlias, sessionProblemName, session.getName()));
        appendSubtabLayout(content, session);
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(sessionProblemAlias, routes.SessionBundleSubmissionController.viewSubmission(session.getId(), bundleSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result regradeSubmission(long sessionId, long bundleSubmissionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);
        BundleAnswer bundleAnswer;
        try {
            bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bundleSubmissionService.regrade(bundleSubmission.getJid(), bundleAnswer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.REGRADE.construct(SESSION, session.getJid(), session.getName(), PROBLEM, bundleSubmission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getProblemJid()), SUBMISSION, bundleSubmission.getJid(), bundleSubmission.getId() + ""));

        return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }

    @Transactional
    public Result regradeSubmissions(long sessionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        ListTableSelectionForm listTableSelectionData = Form.form(ListTableSelectionForm.class).bindFromRequest().get();
        List<BundleSubmission> bundleSubmissions;

        if (listTableSelectionData.selectAll) {
            bundleSubmissions = bundleSubmissionService.getBundleSubmissionsByFilters(orderBy, orderDir, userJid, problemJid, session.getJid());
        } else if (listTableSelectionData.selectJids != null) {
            bundleSubmissions = bundleSubmissionService.getBundleSubmissionsByJids(listTableSelectionData.selectJids);
        } else {
            return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
        }

        for (BundleSubmission bundleSubmission : bundleSubmissions) {
            BundleAnswer bundleAnswer;
            try {
                bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bundleSubmissionService.regrade(bundleSubmission.getJid(), bundleAnswer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.REGRADE.construct(SESSION, session.getJid(), session.getName(), PROBLEM, bundleSubmission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getProblemJid()), SUBMISSION, bundleSubmission.getJid(), bundleSubmission.getId() + ""));
        }

        return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
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
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.SessionBundleSubmissionController.viewSubmissions(session.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
