package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JerahmeelSubmissionServiceUtils {

    static final String PROBLEM_SET_JID_PREFIX = "JIDPRSE";

    private JerahmeelSubmissionServiceUtils() {
        // prevent instantiation
    }

    static void updateSessionProblemProgressWithBundleSubmissions(UserItemDao userItemDao, SessionProblemDao sessionProblemDao, String userJid, String containerJid, String problemJid, List<BundleSubmission> bundleSubmissions) {
        boolean completed = false;
        for (int i = 0; (!completed) && (i < bundleSubmissions.size()); ++i) {
            if (Double.compare(bundleSubmissions.get(i).getLatestScore(), 100) == 0) {
                completed = true;
            }
        }

        updateSessionProblemProgress(userItemDao, sessionProblemDao, userJid, containerJid, problemJid, completed);
    }

    static void updateSessionProblemProgressWithProgrammingSubmissions(UserItemDao userItemDao, SessionProblemDao sessionProblemDao, String userJid, String containerJid, String problemJid, List<ProgrammingSubmission> programmingSubmissions) {
        boolean completed = false;
        for (int i = 0; (!completed) && (i < programmingSubmissions.size()); ++i) {
            if (Double.compare(programmingSubmissions.get(i).getLatestScore(), 100) == 0) {
                completed = true;
            }
        }

        updateSessionProblemProgress(userItemDao, sessionProblemDao, userJid, containerJid, problemJid, completed);
    }

    private static void updateSessionProblemProgress(UserItemDao userItemDao, SessionProblemDao sessionProblemDao, String userJid, String containerJid, String problemJid, boolean completed) {
        if (userItemDao.existsByUserJidAndItemJid(userJid, problemJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, problemJid);
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = problemJid;
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.persist(userItemModel, userJid, "localhost");
        }

        userItemDao.flush();

        boolean sessionCompleted = true;
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(containerJid);
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (!userItemDao.existsByUserJidItemJidAndStatus(userJid, sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                sessionCompleted = false;
                break;
            }
        }

        if (userItemDao.existsByUserJidAndItemJid(userJid, containerJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, containerJid);
            if (sessionCompleted) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = containerJid;
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.persist(userItemModel, userJid, "localhost");
        }
    }

    static double countBundleSubmissionsMaxScore(List<BundleSubmission> bundleSubmissions) {
        if (bundleSubmissions.isEmpty()) {
            System.out.println("APA SIH");
            return 0;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        for (BundleSubmission bundleSubmission : bundleSubmissions) {
            if (bundleSubmission.getLatestScore() > maxScore) {
                maxScore = bundleSubmission.getLatestScore();
            }
        }

        return maxScore;
    }

    static double countProgrammingSubmissionsMaxScore(List<ProgrammingSubmission> programmingSubmissions) {
        if (programmingSubmissions.isEmpty()) {
            return 0;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        for (ProgrammingSubmission programmingSubmission : programmingSubmissions) {
            if (programmingSubmission.getLatestScore() > maxScore) {
                maxScore = programmingSubmission.getLatestScore();
            }
        }

        return maxScore;
    }

    static void createContainerProblemScoreCache(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, String userJid, String containerJid, String problemJid, double maxScore) {
        ContainerProblemScoreCacheModel containerProblemScoreCacheModel = new ContainerProblemScoreCacheModel();
        containerProblemScoreCacheModel.containerJid = containerJid;
        containerProblemScoreCacheModel.userJid = userJid;
        containerProblemScoreCacheModel.problemJid = problemJid;
        containerProblemScoreCacheModel.score = maxScore;
        containerProblemScoreCacheDao.persist(containerProblemScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
    }

    static void updateContainerProblemScoreCache(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, String userJid, String containerJid, String problemJid, double maxScore) {
        ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, containerJid, problemJid);
        containerProblemScoreCacheModel.score = maxScore;
        containerProblemScoreCacheDao.edit(containerProblemScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
    }

    static void updateProblemSetAndParentsScoreCache(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ArchiveDao archiveDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, String containerJid, double deltaScore) {
        ProblemSetModel problemSetModel = problemSetDao.findByJid(containerJid);
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, containerJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, containerJid);
            containerScoreCacheModel.score = containerScoreCacheModel.score + deltaScore;

            containerScoreCacheDao.edit(containerScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
        } else {
            double problemSetScore = ProblemSetServiceUtils.getUserTotalScoreFromProblemSetModelAndProblemSetProblemModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetModel, problemSetProblemDao.getByProblemSetJid(problemSetModel.jid));

            ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, problemSetModel.jid, problemSetScore);
        }

        ArchiveModel archiveModel = archiveDao.findByJid(problemSetModel.archiveJid);
        do {
            if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, archiveModel.jid)) {
                ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, archiveModel.jid);
                containerScoreCacheModel.score = containerScoreCacheModel.score + deltaScore;

                containerScoreCacheDao.edit(containerScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
            } else {
                double archiveScore = ArchiveServiceUtils.getArchiveScoreWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, archiveDao, problemSetDao, problemSetProblemDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, archiveModel.jid);

                ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, archiveModel.jid, archiveScore);
            }

            if (archiveModel.parentJid.equals("")) {
                archiveModel = null;
            } else {
                archiveModel = archiveDao.findByJid(archiveModel.parentJid);
            }
        } while (archiveModel != null);
    }

    static void updateSessionAndParentsScoreCache(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, CourseSessionDao courseSessionDao, SessionProblemDao sessionProblemDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, String containerJid, double deltaScore) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, containerJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, containerJid);
            containerScoreCacheModel.score = containerScoreCacheModel.score + deltaScore;

            containerScoreCacheDao.edit(containerScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
        } else {
            List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(containerJid);
            double sessionScore = SessionProblemServiceUtils.getUserTotalScoreFromSessionProblemModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, sessionProblemModels);

            ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, containerJid, sessionScore);
        }

        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(CourseSessionModel_.sessionJid, containerJid), 0, -1);
        Map<String, List<CourseSessionModel>> mapCourseJidToCourseSessionModels = Maps.newHashMap();
        ImmutableSet.Builder<String> sessionJidsSetBuilder = ImmutableSet.builder();
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<CourseSessionModel> value;
            if (mapCourseJidToCourseSessionModels.containsKey(courseSessionModel.courseJid)) {
                value = mapCourseJidToCourseSessionModels.get(courseSessionModel.courseJid);
            } else {
                value = Lists.newArrayList();
            }

            value.add(courseSessionModel);
            mapCourseJidToCourseSessionModels.put(courseSessionModel.courseJid, value);
            sessionJidsSetBuilder.add(courseSessionModel.sessionJid);
        }

        Set<String> sessionJidsSet = sessionJidsSetBuilder.build();
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJidsSet), 0, -1);
        Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels = Maps.newHashMap();
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            List<SessionProblemModel> value;
            if (mapSessionJidToSessionProblemModels.containsKey(sessionProblemModel.sessionJid)) {
                value = mapSessionJidToSessionProblemModels.get(sessionProblemModel.sessionJid);
            } else {
                value = Lists.newArrayList();
            }

            value.add(sessionProblemModel);
            mapSessionJidToSessionProblemModels.put(sessionProblemModel.sessionJid, value);
        }

        for (String courseJid : mapCourseJidToCourseSessionModels.keySet()) {
            if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, courseJid)) {
                ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, courseJid);
                containerScoreCacheModel.score = containerScoreCacheModel.score + deltaScore;

                containerScoreCacheDao.edit(containerScoreCacheModel, "cacheAfterGradeUpdater", "localhost");
            } else {
                double courseScore = CourseSessionServiceUtils.getUserTotalScoreFromCourseSessionModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, mapCourseJidToCourseSessionModels.get(courseJid), mapSessionJidToSessionProblemModels);

                ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, courseJid, courseScore);
            }
        }
    }
}
