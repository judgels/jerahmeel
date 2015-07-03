package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import play.mvc.Result;

@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingController extends BaseController {

    public TrainingController() {
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
