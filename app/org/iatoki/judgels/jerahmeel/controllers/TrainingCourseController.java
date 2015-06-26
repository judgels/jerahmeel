package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CurriculumCourseProgress;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.training.course.listCurriculumCoursesView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingCourseController extends BaseController {
    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseSessionService courseSessionService;
    private final UserItemService userItemService;

    public TrainingCourseController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseSessionService courseSessionService, UserItemService userItemService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseSessionService = courseSessionService;
        this.userItemService = userItemService;
    }

    @Transactional(readOnly = true)
    public Result viewCourses(long curriculumId) throws CurriculumNotFoundException {
        return listCourses(curriculumId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listCourses(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);

        Page<CurriculumCourseProgress> curriculumPage = curriculumCourseService.findCurriculumCourses(IdentityUtils.getUserJid(), curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListCourses(curriculum, curriculumPage, orderBy, orderDir, filterString);
    }

    private Result showListCourses(Curriculum curriculum, Page<CurriculumCourseProgress> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCurriculumCoursesView.render(curriculum.getId(), currentPage, orderBy, orderDir, filterString));
        CurriculumControllerUtils.appendViewLayout(content, curriculum);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.TrainingController.jumpToCurriculums()),
              new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
