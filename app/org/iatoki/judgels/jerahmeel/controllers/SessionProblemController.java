package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
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
import org.iatoki.judgels.jerahmeel.views.html.session.problem.listSessionProblemsView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.viewProblemView;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestriction;
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

    public Result viewSessionProblems(long sessionId) throws SessionNotFoundException {
        return listSessionProblems(sessionId, 0, "id", "asc", "");
    }

    public Result listSessionProblems(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Page<SessionProblem> sessionPage = sessionProblemService.findSessionProblems(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListSessionProblems(session, sessionPage, orderBy, orderDir, filterString);
    }

    public Result viewProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            int tOTPCode = sandalphon.calculateTOTPCode(sessionProblem.getProblemSecret(), System.currentTimeMillis());
            String submitUrl = "";
            if (SessionProblemType.BUNDLE.equals(sessionProblem.getType())) {
                submitUrl = routes.SessionBundleSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
            } else if (SessionProblemType.PROGRAMMING.equals(sessionProblem.getType())) {
                submitUrl = routes.SessionProgrammingSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
            }

            String requestUrl = sandalphon.getProblemTOTPEndpoint().toString();
            String requestBody = sandalphon.getProblemTOTPRequestBody(sessionProblem.getProblemJid(), tOTPCode, SessionControllerUtils.getCurrentStatementLanguage(), submitUrl, routes.SessionProblemController.switchLanguage().absoluteURL(request(), request().secure()), null, LanguageRestriction.defaultRestriction());

            LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody));
            SessionControllerUtils.appendUpdateLayout(content, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
                  new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
                  new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())),
                  new InternalLink(sessionProblem.getAlias(), routes.SessionProblemController.viewProblem(session.getId(), sessionProblem.getId()))
            ));

            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problem - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return forbidden();
        }
    }
    
    public Result renderImage(long sessionId, long sessionProblemId, String imageFilename) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            URI imageUri = sandalphon.getProblemRenderUri(sessionProblem.getProblemJid(), imageFilename);

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
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionProblemCreateForm> form = Form.form(SessionProblemCreateForm.class);

        return showCreateProblem(session, form);
    }

    @RequireCSRFCheck
    public Result postCreateProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
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

                    return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
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
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            sessionProblemService.removeSessionProblem(sessionProblemId);

            return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
        } else {
            return forbidden();
        }
    }

    private Result showListSessionProblems(Session session, Page<SessionProblem> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(session.getId(), currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.problems"), new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateProblem(Session session, Form<SessionProblemCreateForm> form) {
        LazyHtml content = new LazyHtml(createProblemView.render(session.getId(), form));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())),
              new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

}
