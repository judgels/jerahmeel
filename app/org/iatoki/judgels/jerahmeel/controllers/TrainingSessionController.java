package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionWithProgress;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.listCourseSessionsView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.listCourseSessionsWithProgressView;
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
public final class TrainingSessionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final UserItemService userItemService;
    private final SessionService sessionService;

    @Inject
    public TrainingSessionController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, UserItemService userItemService, SessionService sessionService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.userItemService = userItemService;
        this.sessionService = sessionService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewSessions(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        return listSessions(curriculumId, curriculumCourseId, 0, "alias", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result listSessions(long curriculumId, long curriculumCourseId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());

        LazyHtml content;
        if (!JerahmeelUtils.isGuest()) {
            Page<CourseSessionWithProgress> pageOfCourseSessionsWithProgress = courseSessionService.getPageOfCourseSessionsWithProgress(IdentityUtils.getUserJid(), curriculumCourse.getCourseJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> sessionJids = pageOfCourseSessionsWithProgress.getData().stream().map(e -> e.getCourseSession().getSessionJid()).collect(Collectors.toList());
            Map<String, Session> sessionsMap = sessionService.getSessionsMapByJids(sessionJids);

            content = new LazyHtml(listCourseSessionsWithProgressView.render(curriculum.getId(), curriculumCourse.getId(), pageOfCourseSessionsWithProgress, sessionsMap, orderBy, orderDir, filterString));
        } else {
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(curriculumCourse.getCourseJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> sessionJids = pageOfCourseSessions.getData().stream().map(e -> e.getSessionJid()).collect(Collectors.toList());
            Map<String, Session> sessionsMap = sessionService.getSessionsMapByJids(sessionJids);

            content = new LazyHtml(listCourseSessionsView.render(curriculum.getId(), curriculumCourse.getId(), pageOfCourseSessions, sessionsMap, orderBy, orderDir, filterString));
        }

        CourseControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
