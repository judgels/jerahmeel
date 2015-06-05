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
import org.iatoki.judgels.jerahmeel.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumService;
import org.iatoki.judgels.jerahmeel.UserItemService;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.listCourseSessionsView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingSessionController extends BaseController {
    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final UserItemService userItemService;

    public TrainingSessionController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, UserItemService userItemService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.userItemService = userItemService;
    }

    public Result viewSessions(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        return listSessions(curriculumId, curriculumCourseId, 0, "id", "asc", "");
    }

    public Result listSessions(long curriculumId, long curriculumCourseId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Page<CourseSession> courseSessionPage = courseSessionService.findCourseSessions(curriculumCourse.getCourseJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            if (!userItemService.isUserItemExist(IdentityUtils.getUserJid(), course.getJid(), UserItemStatus.VIEWED)) {
                userItemService.upsertUserItem(IdentityUtils.getUserJid(), course.getJid(), UserItemStatus.VIEWED);
            }

            return showListSessions(curriculum, curriculumCourse, course, courseSessionPage, orderBy, orderDir, filterString);
        } else {
            return notFound();
        }
    }

    private Result showListSessions(Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, Page<CourseSession> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCourseSessionsView.render(curriculum.getId(), curriculumCourse.getId(), currentPage, orderBy, orderDir, filterString));
        CourseControllerUtils.appendViewLayout(content, course);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
              new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
              new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
