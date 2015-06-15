package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionDependencyCreateForm;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.SessionDependency;
import org.iatoki.judgels.jerahmeel.SessionDependencyNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.dependency.listCreateDependenciesView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class SessionDependencyController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SessionDependencyService sessionDependencyService;

    public SessionDependencyController(SessionService sessionService, SessionDependencyService sessionDependencyService) {
        this.sessionService = sessionService;
        this.sessionDependencyService = sessionDependencyService;
    }

    @AddCSRFToken
    public Result viewDependencies(long sessionId) throws SessionNotFoundException {
        return listCreateDependencies(sessionId, 0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result listCreateDependencies(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Page<SessionDependency> sessionPage = sessionDependencyService.findSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<SessionDependencyCreateForm> form = Form.form(SessionDependencyCreateForm.class);

        return showListCreateDependencies(session, form, sessionPage, orderBy, orderDir, filterString);
    }

    @RequireCSRFCheck
    public Result postCreateDependency(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionDependencyCreateForm> form = Form.form(SessionDependencyCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<SessionDependency> sessionPage = sessionDependencyService.findSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateDependencies(session, form, sessionPage, orderBy, orderDir, filterString);
        } else {
            SessionDependencyCreateForm data = form.get();
            if (sessionService.existBySessionJid(data.sessionJid)) {
                if (sessionDependencyService.existBySessionJidAndDependencyJid(session.getJid(), data.sessionJid)) {
                    sessionDependencyService.addSessionDependency(session.getJid(), data.sessionJid);

                    return redirect(routes.SessionDependencyController.viewDependencies(session.getId()));
                } else {
                    form.reject(Messages.get("error.session.existSession"));
                    Page<SessionDependency> sessionPage = sessionDependencyService.findSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                    return showListCreateDependencies(session, form, sessionPage, orderBy, orderDir, filterString);
                }
            } else {
                form.reject(Messages.get("error.session.invalidJid"));
                Page<SessionDependency> sessionPage = sessionDependencyService.findSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                return showListCreateDependencies(session, form, sessionPage, orderBy, orderDir, filterString);
            }
        }
    }

    public Result deleteDependency(long sessionId, long sessionDependencyId) throws SessionNotFoundException, SessionDependencyNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionDependency sessionDependency = sessionDependencyService.findSessionDependencyBySessionDependencyId(sessionDependencyId);

        if (session.getJid().equals(sessionDependency.getSessionJid())) {
            sessionDependencyService.removeSessionDependency(sessionDependencyId);

            return redirect(routes.SessionDependencyController.viewDependencies(session.getId()));
        } else {
            return forbidden();
        }
    }

    private Result showListCreateDependencies(Session session, Form<SessionDependencyCreateForm> form, Page<SessionDependency> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCreateDependenciesView.render(session.getId(), currentPage, orderBy, orderDir, filterString, form));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.dependencies"), routes.SessionDependencyController.viewDependencies(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
