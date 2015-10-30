package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.SessionUpsertForm;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.views.html.session.createSessionView;
import org.iatoki.judgels.jerahmeel.views.html.session.editSessionGeneralView;
import org.iatoki.judgels.jerahmeel.views.html.session.listSessionsView;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class SessionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SESSION = "session";

    private final SessionService sessionService;
    private final UserItemService userItemService;

    @Inject
    public SessionController(SessionService sessionService, UserItemService userItemService) {
        this.sessionService = sessionService;
        this.userItemService = userItemService;
    }

    @Transactional(readOnly = true)
    public Result viewSessions() {
        return listSessions(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listSessions(long page, String orderBy, String orderDir, String filterString) {
        Page<Session> pageOfSessions = sessionService.getPageOfSessions(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listSessionsView.render(pageOfSessions, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.list"), new InternalLink(Messages.get("commons.create"), routes.SessionController.createSession()), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    public Result jumpToLessons(long sessionId) {
        return redirect(routes.SessionLessonController.viewSessionLessons(sessionId));
    }

    public Result jumpToProblems(long sessionId) {
        return redirect(routes.SessionProblemController.viewSessionProblems(sessionId));
    }

    public Result jumpToSubmissions(long sessionId) {
        return redirect(routes.SessionController.jumpToProgrammingSubmissions(sessionId));
    }

    public Result jumpToBundleSubmissions(long sessionId) {
        return redirect(routes.SessionBundleSubmissionController.viewSubmissions(sessionId));
    }

    public Result jumpToProgrammingSubmissions(long sessionId) {
        return redirect(routes.SessionProgrammingSubmissionController.viewSubmissions(sessionId));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createSession() {
        Form<SessionUpsertForm> sessionUpsertForm = Form.form(SessionUpsertForm.class);

        return showCreateSession(sessionUpsertForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSession() {
        Form<SessionUpsertForm> sessionUpsertForm = Form.form(SessionUpsertForm.class).bindFromRequest();

        if (formHasErrors(sessionUpsertForm)) {
            return showCreateSession(sessionUpsertForm);
        }

        SessionUpsertForm sessionUpsertData = sessionUpsertForm.get();
        Session session = sessionService.createSession(sessionUpsertData.name, sessionUpsertData.description, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.CREATE.construct(SESSION, session.getJid(), session.getName()));

        return redirect(routes.SessionController.viewSessions());
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editSessionGeneral(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionUpsertForm sessionUpsertData = new SessionUpsertForm();
        sessionUpsertData.name = session.getName();
        sessionUpsertData.description = session.getDescription();

        Form<SessionUpsertForm> sessionUpsertForm = Form.form(SessionUpsertForm.class).fill(sessionUpsertData);

        if (!userItemService.userItemExistsByUserJidAndItemJidAndStatus(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED)) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return showEditSessionGeneral(sessionUpsertForm, session);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditSessionGeneral(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionUpsertForm> sessionUpsertForm = Form.form(SessionUpsertForm.class).bindFromRequest();

        if (formHasErrors(sessionUpsertForm)) {
            return showEditSessionGeneral(sessionUpsertForm, session);
        }

        SessionUpsertForm sessionUpsertData = sessionUpsertForm.get();
        sessionService.updateSession(session.getJid(), sessionUpsertData.name, sessionUpsertData.description, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        if (!session.getName().equals(sessionUpsertData.name)) {
            JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.RENAME.construct(SESSION, session.getJid(), session.getName(), sessionUpsertData.name));
        }
        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.CREATE.construct(SESSION, session.getJid(), sessionUpsertData.name));

        return redirect(routes.SessionController.viewSessions());
    }

    private Result showCreateSession(Form<SessionUpsertForm> sessionUpsertForm) {
        LazyHtml content = new LazyHtml(createSessionView.render(sessionUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("session.create"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("session.create"), routes.SessionController.createSession())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Session - Create");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditSessionGeneral(Form<SessionUpsertForm> sessionUpsertForm, Session session) {
        LazyHtml content = new LazyHtml(editSessionGeneralView.render(sessionUpsertForm, session.getId()));
        SessionControllerUtils.appendTabLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("session.update"), routes.SessionController.editSessionGeneral(session.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Session - Update");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SessionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
