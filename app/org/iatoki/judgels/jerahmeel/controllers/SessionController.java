package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.forms.SessionUpsertForm;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.createSessionView;
import org.iatoki.judgels.jerahmeel.views.html.session.listSessionsView;
import org.iatoki.judgels.jerahmeel.views.html.session.updateSessionGeneralView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
public final class SessionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final UserItemService userItemService;

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
        Page<Session> currentPage = sessionService.pageSessions(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listSessionsView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.list"), new InternalLink(Messages.get("commons.create"), routes.SessionController.createSession()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    public Result jumpToLessons(long sessionId) {
        return redirect(routes.SessionLessonController.viewSessionLessons(sessionId));
    }

    public Result jumpToProblems(long sessionId) {
        return redirect(routes.SessionProblemController.viewSessionProblems(sessionId));
    }

    public Result jumpToSubmissions(long sessionId) {
        return redirect(routes.SessionController.jumpToBundleSubmissions(sessionId));
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
        Form<SessionUpsertForm> form = Form.form(SessionUpsertForm.class);

        return showCreateSession(form);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSession() {
        Form<SessionUpsertForm> form = Form.form(SessionUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateSession(form);
        } else {
            SessionUpsertForm sessionUpsertForm = form.get();
            sessionService.createSession(sessionUpsertForm.name, sessionUpsertForm.description);

            return redirect(routes.SessionController.viewSessions());
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateSessionGeneral(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionUpsertForm sessionUpsertForm = new SessionUpsertForm();
        sessionUpsertForm.name = session.getName();
        sessionUpsertForm.description = session.getDescription();

        Form<SessionUpsertForm> form = Form.form(SessionUpsertForm.class).fill(sessionUpsertForm);

        if (!userItemService.isUserItemExist(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED)) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED);
        }

        return showUpdateSessionGeneral(form, session);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateSessionGeneral(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionUpsertForm> form = Form.form(SessionUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdateSessionGeneral(form, session);
        } else {
            SessionUpsertForm sessionUpsertForm = form.get();
            sessionService.updateSession(session.getId(), sessionUpsertForm.name, sessionUpsertForm.description);

            return redirect(routes.SessionController.viewSessions());
        }
    }

    private Result showCreateSession(Form<SessionUpsertForm> form) {
        LazyHtml content = new LazyHtml(createSessionView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("session.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.create"), routes.SessionController.createSession())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Session - Create");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateSessionGeneral(Form<SessionUpsertForm> form, Session session) {
        LazyHtml content = new LazyHtml(updateSessionGeneralView.render(form, session.getId()));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.update"), routes.SessionController.updateSessionGeneral(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Session - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
