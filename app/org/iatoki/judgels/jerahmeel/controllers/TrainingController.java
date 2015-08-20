package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class TrainingController extends AbstractJudgelsController {

    private final CourseSessionService courseSessionService;

    @Inject
    public TrainingController(CourseSessionService courseSessionService) {
        this.courseSessionService = courseSessionService;
    }

    public Result jumpToCurriculums() {
        return redirect(routes.TrainingCurriculumController.viewCurriculums());
    }

    public Result jumpToCourses(long curriculumId) {
        return redirect(routes.TrainingCourseController.viewCourses(curriculumId));
    }

    public Result jumpToSessions(long curriculumId, long curriculumCourseId) {
        return redirect(routes.TrainingSessionController.viewSessions(curriculumId, curriculumCourseId));
    }

    @Transactional(readOnly = true)
    public Result jumpToSession(long curriculumId, long curriculumCourseId, long courseSessionId) throws CourseSessionNotFoundException {
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        if (courseSession.isCompleteable()) {
            return redirect(routes.TrainingLessonController.viewLessons(curriculumId, curriculumCourseId, courseSessionId));
        }

        return redirect(routes.TrainingProblemController.viewProblems(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToLessons(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingLessonController.viewLessons(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToProblems(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingProblemController.viewProblems(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingController.jumpToProgrammingSubmissions(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToBundleSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingBundleSubmissionController.viewSubmissions(curriculumId, curriculumCourseId, courseSessionId));
    }

    public Result jumpToProgrammingSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) {
        return redirect(routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculumId, curriculumCourseId, courseSessionId));
    }
}
