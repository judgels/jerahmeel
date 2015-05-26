package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.JidCacheService;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemCreateForm;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemService;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.createProblemView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.listProblemsView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.listVisibleProblemsView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.viewProblemView;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import java.net.URI;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class SessionProblemController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SessionProblemService sessionProblemService;
    private final Sandalphon sandalphon;

    public SessionProblemController(SessionService sessionService, SessionProblemService sessionProblemService, Sandalphon sandalphon) {
        this.sessionService = sessionService;
        this.sessionProblemService = sessionProblemService;
        this.sandalphon = sandalphon;
    }

    public Result viewVisibleProblems(long sessionId) throws SessionNotFoundException {
        return listVisibleProblems(sessionId, 0, "id", "asc", "");
    }

    public Result listVisibleProblems(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);

        Page<SessionProblem> sessionPage = sessionProblemService.findSessionProblems(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListVisibleProblems(session, sessionPage, orderBy, orderDir, filterString);
    }

    public Result viewProblems(long sessionId) throws SessionNotFoundException {
        return listProblems(sessionId, 0, "id", "asc", "");
    }

    public Result listProblems(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);

        Page<SessionProblem> sessionPage = sessionProblemService.findSessionProblems(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListProblems(session, sessionPage, orderBy, orderDir, filterString);
    }

    public Result viewProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            int tOTPCode = sandalphon.calculateTOTPCode(sessionProblem.getProblemSecret(), System.currentTimeMillis());
            String requestUrl = sandalphon.getProblemTOTPEndpoint(sessionProblem.getProblemJid(), tOTPCode, SessionControllerUtils.getCurrentStatementLanguage(), routes.SessionProblemController.switchLanguage().absoluteURL(request(), request().secure()), routes.SessionProblemController.switchLanguage().absoluteURL(request(), request().secure())).toString();
            String requestBody = "";

            LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody));
            SessionControllerUtils.appendViewTabLayout(content, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
                  new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewVisibleProblems(session.getId())),
                  new InternalLink(Messages.get("session.problem.view"), routes.SessionProblemController.viewProblem(session.getId(), sessionProblem.getId()))
            ));

            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problem - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return forbidden();
        }
    }
    
    public Result renderImage(long sessionId, long sessionProblemId, String imageFilename) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            URI imageUri = sandalphon.getProblemRenderImageUri(sessionProblem.getProblemJid(), imageFilename);

            return redirect(imageUri.toString());
        } else {
            return notFound();
        }
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }
    
    @AddCSRFToken
    public Result createProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);
        Form<SessionProblemCreateForm> form = Form.form(SessionProblemCreateForm.class);

        return showCreateProblem(session, form);
    }

    @RequireCSRFCheck
    public Result postCreateProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);
        Form<SessionProblemCreateForm> form = Form.form(SessionProblemCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateProblem(session, form);
        } else {
            SessionProblemCreateForm data = form.get();
            String problemName = sandalphon.verifyProblemJid(data.problemJid);
            if (problemName != null) {
                if (!sessionProblemService.isInSessionByProblemJidAndAlias(session.getJid(), data.problemJid, data.alias)) {
                    sessionProblemService.addSessionProblem(session.getJid(), data.problemJid, data.problemSecret, data.alias, SessionProblemType.valueOf(data.type), SessionProblemStatus.valueOf(data.status));
                    JidCacheService.getInstance().putDisplayName(data.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

                    return redirect(routes.SessionProblemController.viewProblems(session.getId()));
                } else {
                    form.reject(Messages.get("error.session.problemExist"));

                    return showCreateProblem(session, form);
                }
            } else {
                form.reject(Messages.get("error.problem.invalidJid"));

                return showCreateProblem(session, form);
            }
        }
    }

    public Result deleteProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            sessionProblemService.removeSessionProblem(sessionProblemId);

            return redirect(routes.SessionProblemController.viewProblems(session.getId()));
        } else {
            return forbidden();
        }
    }

    private Result showListVisibleProblems(Session session, Page<SessionProblem> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listVisibleProblemsView.render(session.getId(), currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingLayout.render(Messages.get("session.problems"), c));
        SessionControllerUtils.appendViewTabLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewVisibleProblems(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showListProblems(Session session, Page<SessionProblem> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listProblemsView.render(session.getId(), currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.problems"), new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId())), c));
        SessionControllerUtils.appendUpdateTabLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewProblems(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateProblem(Session session, Form<SessionProblemCreateForm> form) {
        LazyHtml content = new LazyHtml(createProblemView.render(session.getId(), form));
        SessionControllerUtils.appendUpdateTabLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewProblems(session.getId())),
              new InternalLink(Messages.get("session.problem.create"), routes.SessionProblemController.createProblem(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

}
