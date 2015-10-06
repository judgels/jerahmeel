package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class TrainingController extends AbstractJudgelsController {

    private final CourseSessionService courseSessionService;

    @Inject
    public TrainingController(CourseSessionService courseSessionService) {
        this.courseSessionService = courseSessionService;
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToCurriculums() {
        return redirect(routes.TrainingCurriculumController.viewCurriculums());
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToCourses(long curriculumId) {
        return redirect(routes.TrainingCourseController.viewCourses(curriculumId));
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToSessions(long curriculumId, long curriculumCourseId) {
        return redirect(routes.TrainingSessionController.viewSessions(curriculumId, curriculumCourseId));
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result jumpToSession(long curriculumId, long curriculumCourseId, long courseSessionId) throws CourseSessionNotFoundException {
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        return redirect(routes.TrainingLessonController.viewLessons(curriculumId, curriculumCourseId, courseSessionId));
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToLessons(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingLessonController.viewLessons(curriculumId, curriculumCourseId, courseSessionId));
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToProblems(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingProblemController.viewProblems(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculumId, curriculumCourseId, courseSessionId));
    }
}
