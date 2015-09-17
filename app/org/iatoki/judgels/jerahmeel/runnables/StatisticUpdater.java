package org.iatoki.judgels.jerahmeel.runnables;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.iatoki.judgels.jerahmeel.PointStatisticEntry;
import org.iatoki.judgels.jerahmeel.ProblemStatisticEntry;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.db.jpa.JPA;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class StatisticUpdater implements Runnable {

    private final BundleSubmissionService bundleSubmissionService;
    private final PointStatisticService pointStatisticService;
    private final ProblemStatisticService problemStatisticService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    public StatisticUpdater(BundleSubmissionService bundleSubmissionService, PointStatisticService pointStatisticService, ProblemStatisticService problemStatisticService, ProgrammingSubmissionService programmingSubmissionService) {
        this.bundleSubmissionService = bundleSubmissionService;
        this.pointStatisticService = pointStatisticService;
        this.problemStatisticService = problemStatisticService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Override
    public void run() {
        JPA.withTransaction(() -> {
                long timeNow = System.currentTimeMillis();
                Map<String, Map<String, Double>> userJidToMapProblemJidToPoints = Maps.newHashMap();
                Map<String, Long> problemJidToSubmissions = Maps.newHashMap();

                List<BundleSubmission> bundleSubmissions = bundleSubmissionService.getAllBundleSubmissions();
                for (BundleSubmission bundleSubmission : bundleSubmissions) {
                    String userJid = bundleSubmission.getAuthorJid();
                    Map<String, Double> problemJidToPoints;
                    if (userJidToMapProblemJidToPoints.containsKey(userJid)) {
                        problemJidToPoints = userJidToMapProblemJidToPoints.get(userJid);
                    } else {
                        problemJidToPoints = Maps.newHashMap();
                    }

                    String problemJid = bundleSubmission.getProblemJid();
                    double point;
                    if (problemJidToPoints.containsKey(problemJid)) {
                        point = problemJidToPoints.get(problemJid);
                    } else {
                        point = -1;
                    }
                    if (bundleSubmission.getLatestScore() > point) {
                        problemJidToPoints.put(problemJid, bundleSubmission.getLatestScore());
                    }

                    userJidToMapProblemJidToPoints.put(userJid, problemJidToPoints);

                    if ((timeNow - bundleSubmission.getTime().getTime()) <= TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)) {
                        long total;
                        if (problemJidToSubmissions.containsKey(problemJid)) {
                            total = problemJidToSubmissions.get(problemJid);
                        } else {
                            total = 0;
                        }

                        total++;
                        problemJidToSubmissions.put(problemJid, total);
                    }
                }

                List<ProgrammingSubmission> programmingSubmissions = programmingSubmissionService.getAllProgrammingSubmissions();
                for (ProgrammingSubmission programmingSubmission : programmingSubmissions) {
                    String userJid = programmingSubmission.getAuthorJid();
                    Map<String, Double> problemJidToPoints;
                    if (userJidToMapProblemJidToPoints.containsKey(userJid)) {
                        problemJidToPoints = userJidToMapProblemJidToPoints.get(userJid);
                    } else {
                        problemJidToPoints = Maps.newHashMap();
                    }

                    String problemJid = programmingSubmission.getProblemJid();
                    double point;
                    if (problemJidToPoints.containsKey(problemJid)) {
                        point = problemJidToPoints.get(problemJid);
                    } else {
                        point = -1;
                    }
                    if (programmingSubmission.getLatestScore() > point) {
                        problemJidToPoints.put(problemJid, (double) programmingSubmission.getLatestScore());
                    }

                    userJidToMapProblemJidToPoints.put(userJid, problemJidToPoints);

                    if ((timeNow - programmingSubmission.getTime().getTime()) <= TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)) {
                        long total;
                        if (problemJidToSubmissions.containsKey(problemJid)) {
                            total = problemJidToSubmissions.get(problemJid);
                        } else {
                            total = 0;
                        }

                        total++;
                        problemJidToSubmissions.put(problemJid, total);
                    }
                }

                List<PointStatisticEntry> pointStatisticEntries = Lists.newArrayList();
                for (String userJid : userJidToMapProblemJidToPoints.keySet()) {
                    Map<String, Double> problemJidToPoints = userJidToMapProblemJidToPoints.get(userJid);
                    double point = problemJidToPoints.values().stream().mapToDouble(Double::doubleValue).sum();
                    pointStatisticEntries.add(new PointStatisticEntry(userJid, point, problemJidToPoints.size()));
                }
                Collections.sort(pointStatisticEntries);

                List<ProblemStatisticEntry> problemStatisticEntries = Lists.newArrayList();
                for (String problemJid : problemJidToSubmissions.keySet()) {
                    problemStatisticEntries.add(new ProblemStatisticEntry(problemJid, problemJidToSubmissions.get(problemJid)));
                }
                Collections.sort(problemStatisticEntries);

                pointStatisticService.updatePointStatistic(pointStatisticEntries, timeNow);
                problemStatisticService.updateProblemStatistic(problemStatisticEntries, timeNow);
            });
    }
}
