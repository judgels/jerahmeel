package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.ListTableSelectionForm;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.BaseController;
import org.iatoki.judgels.play.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionLocalFile;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionRemoteFile;
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
import org.iatoki.judgels.jerahmeel.views.html.session.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleDetailResult;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.bundleSubmissionView;
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
@Authorized(value = {"admin"})
@Singleton
@Named
public final class SessionBundleSubmissionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final BundleSubmissionService bundleSubmissionService;
    private final SessionProblemService sessionProblemService;
    private final FileSystemProvider bundleSubmissionLocalFileSystemProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileSystemProvider;
    private final UserItemService userItemService;

    @Inject
    public SessionBundleSubmissionController(SessionService sessionService, BundleSubmissionService bundleSubmissionService, SessionProblemService sessionProblemService, @BundleSubmissionLocalFile FileSystemProvider bundleSubmissionLocalFileSystemProvider, @BundleSubmissionRemoteFile @Nullable FileSystemProvider bundleSubmissionRemoteFileSystemProvider, UserItemService userItemService) {
        this.sessionService = sessionService;
        this.bundleSubmissionService = bundleSubmissionService;
        this.sessionProblemService = sessionProblemService;
        this.bundleSubmissionLocalFileSystemProvider = bundleSubmissionLocalFileSystemProvider;
        this.bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider;
        this.userItemService = userItemService;
    }

    @Transactional
    public Result postSubmitProblem(long sessionId, String problemJid) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), problemJid);

        DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();

        BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromNewSubmission(dynamicForm, SessionControllerUtils.getCurrentStatementLanguage());
        String submissionJid = bundleSubmissionService.submit(sessionProblem.getProblemJid(), session.getJid(), answer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        bundleSubmissionService.storeSubmissionFiles(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, submissionJid, answer);

        return redirect(routes.SessionBundleSubmissionController.viewSubmissions(sessionId));
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

        Page<BundleSubmission> bundleSubmissions = bundleSubmissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualUserJid, actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.findBundleProblemJidToAliasMapBySessionJid(session.getJid());
        List<UserItem> userItems = userItemService.findAllUserItemByItemJid(session.getJid());
        List<String> userJids = Lists.transform(userItems, u -> u.getUserJid());

        LazyHtml content = new LazyHtml(listSubmissionsView.render(session.getId(), bundleSubmissions, userJids, problemJidToAliasMap, pageIndex, orderBy, orderDir, actualUserJid, actualProblemJid));
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
              new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionBundleSubmissionController.viewSubmissions(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long sessionId, long bundleSubmissionId) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        BundleSubmission bundleSubmission = bundleSubmissionService.findSubmissionById(bundleSubmissionId);
        try {
            BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
            SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), bundleSubmission.getProblemJid());
            String sessionProblemAlias = sessionProblem.getAlias();
            String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid());

            LazyHtml content = new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<Map<String, BundleDetailResult>>() {}.getType()), answer, JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), sessionProblemAlias, sessionProblemName, session.getName()));
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
                  new InternalLink(Messages.get("session.submissions.bundle"), routes.SessionController.jumpToBundleSubmissions(session.getId())),
                  new InternalLink(Messages.get("commons.view"), routes.SessionBundleSubmissionController.viewSubmissions(session.getId())),
                  new InternalLink(sessionProblemAlias, routes.SessionBundleSubmissionController.viewSubmission(session.getId(), bundleSubmission.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Result regradeSubmission(long sessionId, long bundleSubmissionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        BundleSubmission bundleSubmission = bundleSubmissionService.findSubmissionById(bundleSubmissionId);
        try {
            BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
            bundleSubmissionService.regrade(bundleSubmission.getJid(), answer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Result regradeSubmissions(long sessionId, long pageIndex, String orderBy, String orderDir, String userJid, String problemJid) throws SessionNotFoundException, BundleSubmissionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

        List<BundleSubmission> bundleSubmissions;

        if (data.selectAll) {
            bundleSubmissions = bundleSubmissionService.findSubmissionsWithoutGradingsByFilters(orderBy, orderDir, userJid, problemJid, session.getJid());
        } else if (data.selectJids != null) {
            bundleSubmissions = bundleSubmissionService.findSubmissionsWithoutGradingsByJids(data.selectJids);
        } else {
            return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
        }

        for (BundleSubmission bundleSubmission : bundleSubmissions) {
            try {
                BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
                bundleSubmissionService.regrade(bundleSubmission.getJid(), answer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return redirect(routes.SessionBundleSubmissionController.listSubmissions(sessionId, pageIndex, orderBy, orderDir, userJid, problemJid));
    }
}
