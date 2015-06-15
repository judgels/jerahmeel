package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseService;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumService;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonService;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.UserItemService;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.viewLessonView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.lesson.listSessionLessonsView;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import java.net.URI;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingLessonController extends BaseController {
    private static final long PAGE_SIZE = 20;

    private final Sandalphon sandalphon;
    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;
    private final SessionDependencyService sessionDependencyService;
    private final SessionLessonService sessionLessonService;
    private final UserItemService userItemService;

    public TrainingLessonController(Sandalphon sandalphon, CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService, SessionDependencyService sessionDependencyService, SessionLessonService sessionLessonService, UserItemService userItemService) {
        this.sandalphon = sandalphon;
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionLessonService = sessionLessonService;
        this.userItemService = userItemService;
    }

    public Result viewLessons(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listLessons(curriculumId, curriculumCourseId, courseSessionId, 0, "id", "asc", "");
    }

    public Result listLessons(long curriculumId, long curriculumCourseId, long courseSessionId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());
            Page<SessionLessonProgress> sessionLessonPage = sessionLessonService.findSessionLessons(IdentityUtils.getUserJid(), courseSession.getSessionJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            if ((!userItemService.isUserItemExist(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED)) && (sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid()))) {
                userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED);
            }

            return showListLessons(curriculum, curriculumCourse, course, courseSession, session, sessionLessonPage, orderBy, orderDir, filterString);
        } else {
            return notFound();
        }
    }

    public Result viewLesson(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionLessonId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonBySessionLessonId(sessionLessonId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) && (sessionLesson.getSessionJid().equals(courseSession.getSessionJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            String requestUrl = sandalphon.getLessonStatementRenderUri().toString();
            String requestBody = sandalphon.getLessonStatementRenderRequestBody(sessionLesson.getLessonJid(), sessionLesson.getLessonSecret(), System.currentTimeMillis(), SessionControllerUtils.getCurrentStatementLanguage(), routes.SessionLessonController.switchLanguage().absoluteURL(request(), request().secure()));

            LazyHtml content = new LazyHtml(viewLessonView.render(requestUrl, requestBody));
            SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
                  new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
                  new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
                  new InternalLink(session.getName(), routes.TrainingController.jumpToLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                  new InternalLink(sessionLesson.getAlias(), routes.TrainingLessonController.viewLesson(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionLesson.getId()))
            ));

            if (!userItemService.isUserItemExist(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED)) {
                userItemService.upsertUserItem(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED);
            }

            ControllerUtils.getInstance().appendTemplateLayout(content, "Training");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }    
    }

    public Result renderLessonMedia(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionLessonId, String filename) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonBySessionLessonId(sessionLessonId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) && (sessionLesson.getSessionJid().equals(courseSession.getSessionJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            URI uri = sandalphon.getLessonMediaRenderUri(sessionLesson.getLessonJid(), filename);

            return redirect(uri.toString());
        } else {
            return notFound();
        }
    }

    private Result showListLessons(Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, Page<SessionLessonProgress> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listSessionLessonsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), currentPage, orderBy, orderDir, filterString));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
              new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
              new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
              new InternalLink(session.getName(), routes.TrainingController.jumpToLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
