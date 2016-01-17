package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonLessonStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseChapter;
import org.iatoki.judgels.jerahmeel.CourseChapterNotFoundException;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.jerahmeel.ChapterLesson;
import org.iatoki.judgels.jerahmeel.ChapterLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.ChapterLessonStatus;
import org.iatoki.judgels.jerahmeel.ChapterLessonWithProgress;
import org.iatoki.judgels.jerahmeel.ChapterNotFoundException;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseChapterService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.ChapterDependencyService;
import org.iatoki.judgels.jerahmeel.services.ChapterLessonService;
import org.iatoki.judgels.jerahmeel.services.ChapterService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.training.course.chapter.lesson.listChapterLessonsView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.chapter.lesson.listChapterLessonsWithProgressView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.chapter.lesson.viewLessonView;
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
public final class TrainingLessonController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final SandalphonClientAPI sandalphonClientAPI;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;
    private final CourseService courseService;
    private final CourseChapterService courseChapterService;
    private final ChapterDependencyService chapterDependencyService;
    private final ChapterLessonService chapterLessonService;
    private final ChapterService chapterService;
    private final UserItemService userItemService;

    @Inject
    public TrainingLessonController(SandalphonClientAPI sandalphonClientAPI, CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseChapterService courseChapterService, ChapterDependencyService chapterDependencyService, ChapterLessonService chapterLessonService, ChapterService chapterService, UserItemService userItemService) {
        this.sandalphonClientAPI = sandalphonClientAPI;
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseChapterService = courseChapterService;
        this.chapterDependencyService = chapterDependencyService;
        this.chapterLessonService = chapterLessonService;
        this.chapterService = chapterService;
        this.userItemService = userItemService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewLessons(long curriculumId, long curriculumCourseId, long courseChapterId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseChapterNotFoundException, ChapterNotFoundException {
        return listLessons(curriculumId, curriculumCourseId, courseChapterId, 0, "alias", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listLessons(long curriculumId, long curriculumCourseId, long courseChapterId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseChapterNotFoundException, ChapterNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseChapter courseChapter = courseChapterService.findCourseChapterById(courseChapterId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseChapter.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Chapter chapter = chapterService.findChapterByJid(courseChapter.getChapterJid());
        LazyHtml content;

        if (!JerahmeelUtils.isGuest()) {
            Page<ChapterLessonWithProgress> pageOfChapterLessonsWithProgress = chapterLessonService.getPageOfChapterLessonsWithProgress(IdentityUtils.getUserJid(), courseChapter.getChapterJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> lessonJids = pageOfChapterLessonsWithProgress.getData().stream().map(cp -> cp.getChapterLesson().getLessonJid()).collect(Collectors.toList());
            Map<String, String> lessonTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(lessonJids), StatementControllerUtils.getCurrentStatementLanguage());

            content = new LazyHtml(listChapterLessonsWithProgressView.render(curriculum.getId(), curriculumCourse.getId(), courseChapter.getId(), pageOfChapterLessonsWithProgress, orderBy, orderDir, filterString, lessonTitlesMap));
        } else {
            Page<ChapterLesson> pageOfChapterLessons = chapterLessonService.getPageOfChapterLessons(courseChapter.getChapterJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
            List<String> lessonJids = pageOfChapterLessons.getData().stream().map(cp -> cp.getLessonJid()).collect(Collectors.toList());
            Map<String, String> lessonTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(lessonJids), StatementControllerUtils.getCurrentStatementLanguage());

            content = new LazyHtml(listChapterLessonsView.render(curriculum.getId(), curriculumCourse.getId(), courseChapter.getId(), pageOfChapterLessons, orderBy, orderDir, filterString, lessonTitlesMap));
        }

        TrainingChapterControllerUtils.appendTabLayout(content, curriculum, curriculumCourse, course, courseChapter, chapter);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseChapter, chapter);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewLesson(long curriculumId, long curriculumCourseId, long courseChapterId, long chapterLessonId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseChapterNotFoundException, ChapterNotFoundException, ChapterLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseChapter courseChapter = courseChapterService.findCourseChapterById(courseChapterId);
        ChapterLesson chapterLesson = chapterLessonService.findChapterLessonById(chapterLessonId);

        if ((chapterLesson.getStatus() != ChapterLessonStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseChapter.getCourseJid()) || !chapterLesson.getChapterJid().equals(courseChapter.getChapterJid())) {
            return notFound();
        }

        response().setHeader("X-Frame-Options", "Allow-From *");

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Chapter chapter = chapterService.findChapterByJid(courseChapter.getChapterJid());

        SandalphonLessonStatementRenderRequestParam param = new SandalphonLessonStatementRenderRequestParam();

        param.setLessonSecret(chapterLesson.getLessonSecret());
        param.setCurrentMillis(System.currentTimeMillis());
        param.setStatementLanguage(StatementControllerUtils.getCurrentStatementLanguage());
        param.setSwitchStatementLanguageUrl(routes.ChapterLessonController.switchLanguage().absoluteURL(request(), request().secure()));

        String requestUrl = sandalphonClientAPI.getLessonStatementRenderAPIEndpoint(chapterLesson.getLessonJid());
        String requestBody = sandalphonClientAPI.constructLessonStatementRenderAPIRequestBody(chapterLesson.getLessonJid(), param);

        LazyHtml content = new LazyHtml(viewLessonView.render(requestUrl, requestBody, chapterLesson.getId()));
        TrainingChapterControllerUtils.appendTabLayout(content, curriculum, curriculumCourse, course, courseChapter, chapter);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseChapter, chapter,
                new InternalLink(chapterLesson.getAlias(), routes.TrainingLessonController.viewLesson(curriculum.getId(), curriculumCourse.getId(), courseChapter.getId(), chapterLesson.getId()))
        );

        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result renderLessonMedia(long curriculumId, long curriculumCourseId, long courseChapterId, long chapterLessonId, String filename) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseChapterNotFoundException, ChapterNotFoundException, ChapterLessonNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseChapter courseChapter = courseChapterService.findCourseChapterById(courseChapterId);
        ChapterLesson chapterLesson = chapterLessonService.findChapterLessonById(chapterLessonId);

        if ((chapterLesson.getStatus() != ChapterLessonStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseChapter.getCourseJid()) || !chapterLesson.getChapterJid().equals(courseChapter.getChapterJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Chapter chapter = chapterService.findChapterByJid(courseChapter.getChapterJid());

        String mediaUrl = sandalphonClientAPI.getLessonStatementMediaRenderAPIEndpoint(chapterLesson.getLessonJid(), filename);

        return redirect(mediaUrl);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseChapter courseChapter, Chapter chapter, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingChapterController.viewChapters(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(new InternalLink(chapter.getName(), routes.TrainingLessonController.viewLessons(curriculum.getId(), curriculumCourse.getId(), courseChapter.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
