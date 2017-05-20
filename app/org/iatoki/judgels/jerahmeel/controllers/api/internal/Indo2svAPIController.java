package org.iatoki.judgels.jerahmeel.controllers.api.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.JerahmeelProperties;
import org.iatoki.judgels.jerahmeel.chapter.dependency.ChapterDependencyDao;
import org.iatoki.judgels.jerahmeel.chapter.dependency.ChapterDependencyModel;
import org.iatoki.judgels.jerahmeel.chapter.dependency.ChapterDependencyModel_;
import org.iatoki.judgels.jerahmeel.chapter.problem.ChapterProblemDao;
import org.iatoki.judgels.jerahmeel.chapter.problem.ChapterProblemModel;
import org.iatoki.judgels.jerahmeel.chapter.problem.ChapterProblemModel_;
import org.iatoki.judgels.jerahmeel.course.Course;
import org.iatoki.judgels.jerahmeel.course.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.course.CourseService;
import org.iatoki.judgels.jerahmeel.course.chapter.ChapterProgress;
import org.iatoki.judgels.jerahmeel.course.chapter.CourseChapterDao;
import org.iatoki.judgels.jerahmeel.course.chapter.CourseChapterModel;
import org.iatoki.judgels.jerahmeel.course.chapter.CourseChapterModel_;
import org.iatoki.judgels.jerahmeel.course.chapter.CourseChapterWithProgress;
import org.iatoki.judgels.jerahmeel.user.item.UserItemDao;
import org.iatoki.judgels.jerahmeel.user.item.UserItemModel;
import org.iatoki.judgels.jerahmeel.user.item.UserItemModel_;
import org.iatoki.judgels.jerahmeel.user.item.UserItemStatus;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Indo2svAPIController extends Controller {
    private final CourseService courseService;
    private final CourseChapterDao courseChapterDao;
    private final ChapterDependencyDao chapterDependencyDao;
    private final ChapterProblemDao chapterProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public Indo2svAPIController(CourseService courseService, CourseChapterDao courseChapterDao, ChapterDependencyDao chapterDependencyDao, ChapterProblemDao chapterProblemDao, UserItemDao userItemDao) {
        this.courseService = courseService;
        this.courseChapterDao = courseChapterDao;
        this.chapterDependencyDao = chapterDependencyDao;
        this.chapterProblemDao = chapterProblemDao;
        this.userItemDao = userItemDao;
    }

    @Transactional
    public Result getResult() throws CourseNotFoundException {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String secret = dForm.get("secret");
        if (secret == null) {
            return unauthorized();
        }
        if (!secret.equals(JerahmeelProperties.getInstance().getIndo2svSecret())) {
            return forbidden();
        }

        Course course = courseService.findCourseById(1);
        List<CourseChapterModel> courseChapterModels = courseChapterDao.findSortedByFiltersEq("alias", "asc", "", ImmutableMap.of(CourseChapterModel_.courseJid, course.getJid()), 0, -1);
        List<String> chapterJids = courseChapterModels.stream().map(m -> m.chapterJid).collect(Collectors.toList());

        Map<String, List<ChapterProblemModel>> mapChapterJidToChapterProblemModels =
                chapterProblemDao.findSortedByFiltersIn("alias", "asc", "", ImmutableMap.of(ChapterProblemModel_.chapterJid, chapterJids), 0, -1)
                .stream().collect(Collectors.groupingBy(m -> m.chapterJid));

        Map<String, List<ChapterDependencyModel>> mapChapterJidToDependencies =
                chapterDependencyDao.findSortedByFiltersIn("chapterJid", "asc", "", ImmutableMap.of(ChapterDependencyModel_.chapterJid, chapterJids), 0, -1)
                .stream().collect(Collectors.groupingBy(m -> m.chapterJid));

        Set<String> userJids = JerahmeelProperties.getInstance().getIndo2svRegistrants().keySet();
        Map<String, List<UserItemModel>> mapUserJidToUserItemModels =
                userItemDao.findSortedByFiltersIn("userJid", "asc", "", ImmutableMap.of(UserItemModel_.userJid, userJids), 0, -1)
                .stream().collect(Collectors.groupingBy(m -> m.userJid));

        Map<String, Object> result = Maps.newHashMap();
        result.put("problemCounts", courseChapterModels.stream()
                .map(model -> mapChapterJidToChapterProblemModels.get(model.chapterJid))
                .map(l -> l.size())
                .collect(Collectors.toList()));

        Map<String, List<Long>> userProgresses = Maps.newHashMap();

        for (Map.Entry<String, String> registrant : JerahmeelProperties.getInstance().getIndo2svRegistrants().entrySet()) {
            String userJid = registrant.getKey();
            String username = registrant.getValue();

            List<UserItemModel> allUserItemModels = mapUserJidToUserItemModels.get(userJid);
            if (allUserItemModels == null) {
                allUserItemModels = Collections.emptyList();
            }
            Map<String, List<UserItemModel>> userItemsByItemJid = allUserItemModels
                    .stream().collect(Collectors.groupingBy(m -> m.itemJid));

            ImmutableList.Builder<CourseChapterWithProgress> courseChapterProgressBuilder = ImmutableList.builder();
            List<UserItemModel> completedUserItemModel = filterUserItemsByStatus(allUserItemModels, UserItemStatus.COMPLETED.name());
            Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
            List<UserItemModel> onProgressUserItemModel = filterUserItemsByStatus(allUserItemModels, UserItemStatus.VIEWED.name());
            Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());

            for (CourseChapterModel courseChapterModel : courseChapterModels) {
                List<ChapterProblemModel> currentChapterProblemModels = mapChapterJidToChapterProblemModels.get(courseChapterModel.chapterJid);

                if (currentChapterProblemModels == null) {
                    currentChapterProblemModels = ImmutableList.of();
                }

                long totalProblems = currentChapterProblemModels.size();
                long solvedProblems = 0;
                double totalScore = 0;
                for (ChapterProblemModel chapterProblemModel : currentChapterProblemModels) {
                    if (existsUserItemsByStatus(userItemsByItemJid.get(chapterProblemModel.problemJid), UserItemStatus.COMPLETED.name())) {
                        solvedProblems++;
                    }
                }

                ChapterProgress progress = ChapterProgress.LOCKED;
                if (completedJids.contains(courseChapterModel.chapterJid)) {
                    progress = ChapterProgress.COMPLETED;
                } else if (onProgressJids.contains(courseChapterModel.chapterJid)) {
                    progress = ChapterProgress.IN_PROGRESS;
                } else {
                    List<ChapterDependencyModel> chapterDependencyModels = mapChapterJidToDependencies.get(courseChapterModel.chapterJid);
                    if (chapterDependencyModels == null) {
                        chapterDependencyModels = Collections.emptyList();
                    }
                    Set<String> dependencyJids = chapterDependencyModels.stream().map(m -> m.dependedChapterJid).collect(Collectors.toSet());
                    dependencyJids.removeAll(completedJids);
                    if (dependencyJids.isEmpty()) {
                        progress = ChapterProgress.AVAILABLE;
                    }
                }
                courseChapterProgressBuilder.add(new CourseChapterWithProgress(null, progress, solvedProblems, totalProblems, totalScore));
            }

            userProgresses.put(username, courseChapterProgressBuilder.build()
                    .stream().map(p -> p.getSolvedProblems()).collect(Collectors.toList()));
        }
        result.put("userProgresses", userProgresses);

        return ok(Json.toJson(result));
    }

    private static List<UserItemModel> filterUserItemsByStatus(List<UserItemModel> models, String status) {
        return models.stream().filter(m -> m.status.equals(status)).collect(Collectors.toList());
    }

    private static boolean existsUserItemsByStatus(List<UserItemModel> models, String status) {
        return models != null && models.stream().anyMatch(m -> m.status.equals(status));
    }
}
