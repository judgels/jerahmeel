package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.forms.SessionDependencyCreateForm;
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
@Authorized(value = "admin")
@Singleton
@Named
public final class SessionDependencyController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final SessionDependencyService sessionDependencyService;
    private final SessionService sessionService;

    @Inject
    public SessionDependencyController(SessionDependencyService sessionDependencyService, SessionService sessionService) {
        this.sessionDependencyService = sessionDependencyService;
        this.sessionService = sessionService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewDependencies(long sessionId) throws SessionNotFoundException {
        return listCreateDependencies(sessionId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateDependencies(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        Page<SessionDependency> pageOfSessionDependencies = sessionDependencyService.getPageOfSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<SessionDependencyCreateForm> sessionDependencyCreateForm = Form.form(SessionDependencyCreateForm.class);

        return showListCreateDependencies(session, sessionDependencyCreateForm, pageOfSessionDependencies, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateDependency(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionDependencyCreateForm> sessionDependencyCreateForm = Form.form(SessionDependencyCreateForm.class).bindFromRequest();

        if (formHasErrors(sessionDependencyCreateForm)) {
            Page<SessionDependency> pageOfSessionDependencies = sessionDependencyService.getPageOfSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateDependencies(session, sessionDependencyCreateForm, pageOfSessionDependencies, orderBy, orderDir, filterString);
        }

        SessionDependencyCreateForm sessionDependencyCreateData = sessionDependencyCreateForm.get();
        if (!sessionService.sessionExistsByJid(sessionDependencyCreateData.sessionJid)) {
            sessionDependencyCreateForm.reject(Messages.get("error.session.invalidJid"));
            Page<SessionDependency> pageOfSessionDependencies = sessionDependencyService.getPageOfSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateDependencies(session, sessionDependencyCreateForm, pageOfSessionDependencies, orderBy, orderDir, filterString);
        }

        if (sessionDependencyService.existsBySessionJidAndDependencyJid(session.getJid(), sessionDependencyCreateData.sessionJid)) {
            sessionDependencyCreateForm.reject(Messages.get("error.session.existSession"));
            Page<SessionDependency> pageOfSessionDependencies = sessionDependencyService.getPageOfSessionDependencies(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateDependencies(session, sessionDependencyCreateForm, pageOfSessionDependencies, orderBy, orderDir, filterString);
        }

        sessionDependencyService.addSessionDependency(session.getJid(), sessionDependencyCreateData.sessionJid, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.SessionDependencyController.viewDependencies(session.getId()));
    }

    @Transactional
    public Result deleteDependency(long sessionId, long sessionDependencyId) throws SessionNotFoundException, SessionDependencyNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionDependency sessionDependency = sessionDependencyService.findSessionDependencyById(sessionDependencyId);

        if (!session.getJid().equals(sessionDependency.getSessionJid())) {
            return forbidden();
        }

        sessionDependencyService.removeSessionDependency(sessionDependencyId);

        return redirect(routes.SessionDependencyController.viewDependencies(session.getId()));
    }

    private Result showListCreateDependencies(Session session, Form<SessionDependencyCreateForm> sessionDependencyCreateForm, Page<SessionDependency> pageOfSessionDependencies, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCreateDependenciesView.render(session.getId(), pageOfSessionDependencies, orderBy, orderDir, filterString, sessionDependencyCreateForm));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SessionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.dependencies"), routes.SessionDependencyController.viewDependencies(session.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
