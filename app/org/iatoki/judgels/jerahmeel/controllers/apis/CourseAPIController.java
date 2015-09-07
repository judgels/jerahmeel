package org.iatoki.judgels.jerahmeel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.iatoki.judgels.play.controllers.apis.JudgelsAPIControllerUtils.createJsonPResponse;

@Named
public final class CourseAPIController extends Controller {

    private final CourseService courseService;

    @Inject
    public CourseAPIController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result courseAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");
        String term = dForm.get("term");

        List<Course> courses = courseService.getCoursesByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();

        for (Course course : courses) {
            autoCompleteBuilder.add(new AutoComplete(course.getJid(), course.getJid(), course.getName()));
        }

        return ok(createJsonPResponse(callback, Json.toJson(autoCompleteBuilder.build()).toString()));
    }

}
