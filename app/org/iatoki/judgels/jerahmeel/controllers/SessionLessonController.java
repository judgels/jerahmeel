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
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonCreateForm;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonService;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.createLessonView;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.listSessionLessonsView;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.viewLessonView;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import java.io.IOException;
import java.net.URI;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class SessionLessonController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SessionLessonService sessionLessonService;
    private final Sandalphon sandalphon;

    public SessionLessonController(SessionService sessionService, SessionLessonService sessionLessonService, Sandalphon sandalphon) {
        this.sessionService = sessionService;
        this.sessionLessonService = sessionLessonService;
        this.sandalphon = sandalphon;
    }

    public Result viewSessionLessons(long sessionId) throws SessionNotFoundException {
        return listSessionLessons(sessionId, 0, "id", "asc", "");
    }

    public Result listSessionLessons(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Page<SessionLesson> sessionPage = sessionLessonService.findSessionLessons(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListSessionLessons(session, sessionPage, orderBy, orderDir, filterString);
    }

    public Result viewLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonBySessionLessonId(sessionLessonId);

        if (session.getJid().equals(sessionLesson.getSessionJid())) {
            String requestUrl = sandalphon.getLessonStatementRenderUri().toString();
            String requestBody = sandalphon.getLessonStatementRenderRequestBody(sessionLesson.getLessonJid(), sessionLesson.getLessonSecret(), System.currentTimeMillis(), SessionControllerUtils.getCurrentStatementLanguage(), routes.SessionLessonController.switchLanguage().absoluteURL(request(), request().secure()));

            LazyHtml content = new LazyHtml(viewLessonView.render(requestUrl, requestBody));
            SessionControllerUtils.appendUpdateLayout(content, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
                  new InternalLink(Messages.get("session.lessons"), routes.SessionController.jumpToLessons(session.getId())),
                  new InternalLink(Messages.get("commons.view"), routes.SessionLessonController.viewSessionLessons(session.getId())),
                  new InternalLink(sessionLesson.getAlias(), routes.SessionLessonController.viewLesson(session.getId(), sessionLesson.getId()))
            ));

            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lesson - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result renderImage(long sessionId, long sessionLessonId, String imageFilename) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonBySessionLessonId(sessionLessonId);

        if (session.getJid().equals(sessionLesson.getSessionJid())) {
            URI imageUri = sandalphon.getLessonMediaRenderUri(sessionLesson.getLessonJid(), imageFilename);

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
    public Result createLesson(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionLessonCreateForm> form = Form.form(SessionLessonCreateForm.class);

        return showCreateLesson(session, form);
    }

    @RequireCSRFCheck
    public Result postCreateLesson(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionLessonCreateForm> form = Form.form(SessionLessonCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateLesson(session, form);
        } else {
            SessionLessonCreateForm data = form.get();
            String lessonName = null;
            try {
                lessonName = sandalphon.verifyLessonJid(data.lessonJid);
            } catch (IOException e) {
                form.reject(Messages.get("error.system.sandalphon.connection"));

                return showCreateLesson(session, form);
            }

            if (lessonName != null) {
                if (!sessionLessonService.isInSessionByLessonJidAndAlias(session.getJid(), data.lessonJid, data.alias)) {
                    sessionLessonService.addSessionLesson(session.getJid(), data.lessonJid, data.lessonSecret, data.alias, SessionLessonStatus.valueOf(data.status));
                    JidCacheService.getInstance().putDisplayName(data.lessonJid, lessonName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

                    return redirect(routes.SessionLessonController.viewSessionLessons(session.getId()));
                } else {
                    form.reject(Messages.get("error.session.lessonExist"));

                    return showCreateLesson(session, form);
                }
            } else {
                form.reject(Messages.get("error.lesson.invalidJid"));

                return showCreateLesson(session, form);
            }
        }
    }

    public Result deleteLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonBySessionLessonId(sessionLessonId);

        if (session.getJid().equals(sessionLesson.getSessionJid())) {
            sessionLessonService.removeSessionLesson(sessionLessonId);

            return redirect(routes.SessionLessonController.viewSessionLessons(session.getId()));
        } else {
            return forbidden();
        }
    }


    private Result showListSessionLessons(Session session, Page<SessionLesson> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listSessionLessonsView.render(session.getId(), currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.lessons"), new InternalLink(Messages.get("commons.add"), routes.SessionLessonController.createLesson(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.lessons"), routes.SessionController.jumpToLessons(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionLessonController.viewSessionLessons(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lessons");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateLesson(Session session, Form<SessionLessonCreateForm> form) {
        LazyHtml content = new LazyHtml(createLessonView.render(session.getId(), form));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.lessons"), routes.SessionController.jumpToLessons(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionLessonController.viewSessionLessons(session.getId())),
              new InternalLink(Messages.get("commons.add"), routes.SessionLessonController.createLesson(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lessons - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

}
