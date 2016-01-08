package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseChapter;
import org.iatoki.judgels.jerahmeel.CourseChapterWithProgress;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseChapterService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.ChapterService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.views.html.training.course.chapter.listCourseChaptersView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.chapter.listCourseChaptersWithProgressView;
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
public final class TrainingChapterController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseChapterService courseChapterService;
    private final UserItemService userItemService;
    private final ChapterService chapterService;

    @Inject
    public TrainingChapterController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseChapterService courseChapterService, UserItemService userItemService, ChapterService chapterService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseChapterService = courseChapterService;
        this.userItemService = userItemService;
        this.chapterService = chapterService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewChapters(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        return listChapters(curriculumId, curriculumCourseId, 0, "alias", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result listChapters(long curriculumId, long curriculumCourseId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());

        LazyHtml content;
        if (!JerahmeelUtils.isGuest()) {
            Page<CourseChapterWithProgress> pageOfCourseChaptersWithProgress = courseChapterService.getPageOfCourseChaptersWithProgress(IdentityUtils.getUserJid(), curriculumCourse.getCourseJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> chapterJids = pageOfCourseChaptersWithProgress.getData().stream().map(e -> e.getCourseChapter().getChapterJid()).collect(Collectors.toList());
            Map<String, Chapter> chaptersMap = chapterService.getChaptersMapByJids(chapterJids);

            content = new LazyHtml(listCourseChaptersWithProgressView.render(curriculum.getId(), curriculumCourse.getId(), pageOfCourseChaptersWithProgress, chaptersMap, orderBy, orderDir, filterString));
        } else {
            Page<CourseChapter> pageOfCourseChapters = courseChapterService.getPageOfCourseChapters(curriculumCourse.getCourseJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> chapterJids = pageOfCourseChapters.getData().stream().map(e -> e.getChapterJid()).collect(Collectors.toList());
            Map<String, Chapter> chaptersMap = chapterService.getChaptersMapByJids(chapterJids);

            content = new LazyHtml(listCourseChaptersView.render(curriculum.getId(), curriculumCourse.getId(), pageOfCourseChapters, chaptersMap, orderBy, orderDir, filterString));
        }

        TrainingCourseControllerUtils.appendTabLayout(content, curriculum, curriculumCourse, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingChapterController.viewChapters(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
