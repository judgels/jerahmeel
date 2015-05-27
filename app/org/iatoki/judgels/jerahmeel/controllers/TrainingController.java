package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import play.db.jpa.Transactional;
import play.mvc.Result;

@Transactional
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
}
