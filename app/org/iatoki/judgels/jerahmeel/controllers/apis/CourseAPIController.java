package org.iatoki.judgels.jerahmeel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.AutoComplete;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public final class CourseAPIController extends Controller {

    private final CourseService courseService;

    public CourseAPIController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result courseAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        String term = form.get("term");
        List<Course> courses = courseService.findAllCourseByTerm(term);
        ImmutableList.Builder<AutoComplete> responseBuilder = ImmutableList.builder();

        for (Course course : courses) {
            responseBuilder.add(new AutoComplete(course.getJid(), course.getJid(), course.getName()));
        }

        String callback = form.get("callback");

        return ok(callback + "(" + Json.toJson(responseBuilder.build()).toString() + ")");
    }

}
