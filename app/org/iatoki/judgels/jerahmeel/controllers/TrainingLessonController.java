package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonLessonStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.viewLessonView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.lesson.listSessionLessonsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named
public final class TrainingLessonController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final SandalphonClientAPI sandalphonClientAPI;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionDependencyService sessionDependencyService;
    private final SessionLessonService sessionLessonService;
    private final SessionService sessionService;
    private final UserItemService userItemService;

    @Inject
    public TrainingLessonController(SandalphonClientAPI sandalphonClientAPI, CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, SessionDependencyService sessionDependencyService, SessionLessonService sessionLessonService, SessionService sessionService, UserItemService userItemService) {
        this.sandalphonClientAPI = sandalphonClientAPI;
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionLessonService = sessionLessonService;
        this.sessionService = sessionService;
        this.userItemService = userItemService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewLessons(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listLessons(curriculumId, curriculumCourseId, courseSessionId, 0, "alias", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result listLessons(long curriculumId, long curriculumCourseId, long courseSessionId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());
        Page<SessionLessonProgress> pageOfSessionLessonProgress = sessionLessonService.getPageOfSessionLessonsProgress(IdentityUtils.getUserJid(), courseSession.getSessionJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> lessonJids = pageOfSessionLessonProgress.getData().stream().map(cp -> cp.getSessionLesson().getLessonJid()).collect(Collectors.toList());
        Map<String, String> lessonTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(lessonJids), SessionControllerUtils.getCurrentStatementLanguage());

        if (!JerahmeelUtils.isGuest() && !userItemService.userItemExistsByUserJidAndItemJid(IdentityUtils.getUserJid(), session.getJid()) && sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid())) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return showListLessons(curriculum, curriculumCourse, course, courseSession, session, pageOfSessionLessonProgress, orderBy, orderDir, filterString, lessonTitlesMap);
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewLesson(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionLessonId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if ((sessionLesson.getStatus() != SessionLessonStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()) || !sessionLesson.getSessionJid().equals(courseSession.getSessionJid())) {
            return notFound();
        }

        response().setHeader("X-Frame-Options", "Allow-From *");

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        SandalphonLessonStatementRenderRequestParam param = new SandalphonLessonStatementRenderRequestParam();

        param.setLessonSecret(sessionLesson.getLessonSecret());
        param.setCurrentMillis(System.currentTimeMillis());
        param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
        param.setSwitchStatementLanguageUrl(routes.SessionLessonController.switchLanguage().absoluteURL(request(), request().secure()));

        String requestUrl = sandalphonClientAPI.getLessonStatementRenderAPIEndpoint(sessionLesson.getLessonJid());
        String requestBody = sandalphonClientAPI.constructLessonStatementRenderAPIRequestBody(sessionLesson.getLessonJid(), param);

        LazyHtml content = new LazyHtml(viewLessonView.render(requestUrl, requestBody));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session,
                new InternalLink(sessionLesson.getAlias(), routes.TrainingLessonController.viewLesson(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionLesson.getId()))
        );

        if (!JerahmeelUtils.isGuest() && !userItemService.userItemExistsByUserJidAndItemJidAndStatus(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED)) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result renderLessonMedia(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionLessonId, String filename) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if ((sessionLesson.getStatus() != SessionLessonStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()) || !sessionLesson.getSessionJid().equals(courseSession.getSessionJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        String mediaUrl = sandalphonClientAPI.getLessonStatementMediaRenderAPIEndpoint(sessionLesson.getLessonJid(), filename);

        return redirect(mediaUrl);
    }

    private Result showListLessons(Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, Page<SessionLessonProgress> pageOfSessionLessonsProgress, String orderBy, String orderDir, String filterString, Map<String, String> lessonTitlesMap) {
        LazyHtml content = new LazyHtml(listSessionLessonsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), pageOfSessionLessonsProgress, orderBy, orderDir, filterString, lessonTitlesMap));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(new InternalLink(session.getName(), routes.TrainingController.jumpToLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
