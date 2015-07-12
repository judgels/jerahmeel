package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.controllers.forms.SessionDependencyCreateForm;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.SessionDependency;
import org.iatoki.judgels.jerahmeel.SessionDependencyNotFoundException;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.dependency.listCreateDependenciesView;
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
@Authorized(value = {"admin"})
@Singleton
@Named
public final class SessionDependencyController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SessionDependencyService sessionDependencyService;

    @Inject
    public SessionDependencyController(SessionService sessionService, SessionDependencyService sessionDependencyService) {
        this.sessionService = sessionService;
        this.sessionDependencyService = sessionDependencyService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewDependencies(long sessionId) throws SessionNotFoundException {
        return listCreateDependencies(sessionId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateDependencies(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Page<SessionDependency> sessionPage = sessionDependencyService.findSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<SessionDependencyCreateForm> form = Form.form(SessionDependencyCreateForm.class);

        return showListCreateDependencies(session, form, sessionPage, orderBy, orderDir, filterString);
    }

    @Transactional
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
                if (!sessionDependencyService.existBySessionJidAndDependencyJid(session.getJid(), data.sessionJid)) {
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

    @Transactional
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
