package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.ProblemProgress;
import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemWithScore;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemType;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.ProblemSetProblemService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.sandalphon.ProblemType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named("problemSetProblemService")
public final class ProblemSetProblemServiceImpl implements ProblemSetProblemService {

    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public ProblemSetProblemServiceImpl(BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, ProblemSetProblemDao problemSetProblemDao, UserItemDao userItemDao) {
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
        this.problemSetProblemDao = problemSetProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean aliasExistsInProblemSet(String problemSetJid, String alias) {
        return problemSetProblemDao.existsByProblemSetJidAndAlias(problemSetJid, alias);
    }

    @Override
    public ProblemSetProblem findProblemSetProblemById(long problemSetProblemId) throws ProblemSetProblemNotFoundException {
        ProblemSetProblemModel problemSetProblemModel = problemSetProblemDao.findById(problemSetProblemId);
        if (problemSetProblemModel != null) {
            return ProblemSetProblemServiceUtils.createFromModel(problemSetProblemModel);
        } else {
            throw new ProblemSetProblemNotFoundException("ProblemSet Problem Not Found");
        }
    }

    @Override
    public Page<ProblemSetProblem> getPageOfProblemSetProblems(String problemSetJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = problemSetProblemDao.countByFilters(filterString, ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetJid), ImmutableMap.of());
        List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<ProblemSetProblem> problemSetProblems = problemSetProblemModels.stream().map(m -> ProblemSetProblemServiceUtils.createFromModel(m)).collect(Collectors.toList());

        return new Page<>(problemSetProblems, totalPages, pageIndex, pageSize);

    }

    @Override
    public Page<ProblemSetProblemWithScore> getPageOfProblemSetProblemsWithScore(String userJid, String problemSetJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = problemSetProblemDao.countByFilters(filterString, ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetJid, ProblemSetProblemModel_.status, ProblemSetProblemStatus.VISIBLE.name()), ImmutableMap.of());
        List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetJid, ProblemSetProblemModel_.status, ProblemSetProblemStatus.VISIBLE.name()), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<ProblemSetProblemWithScore> problemSetProblemProgressBuilder = ImmutableList.builder();
        for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
            ProblemProgress progress = ProblemProgress.NOT_VIEWED;
            if (userItemDao.existsByUserJidAndItemJid(userJid, problemSetProblemModel.problemJid)) {
                UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, problemSetProblemModel.problemJid);
                if (UserItemStatus.VIEWED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.VIEWED;
                } else if (UserItemStatus.COMPLETED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.COMPLETED;
                }
            }

            double maxScore = -1;
            if (!progress.equals(ProblemProgress.NOT_VIEWED)) {
                if (problemSetProblemModel.type.equals(ProblemType.BUNDLE.name())) {
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);
                    Map<String, List<BundleGradingModel>> bundleGradingModels = bundleGradingDao.getBySubmissionJids(bundleSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : bundleGradingModels.keySet()) {
                        double submissionScore = bundleGradingModels.get(submissionJid).get(bundleGradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                } else if (problemSetProblemModel.type.equals(ProblemType.PROGRAMMING.name())) {
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);
                    Map<String, List<ProgrammingGradingModel>> gradingModels = programmingGradingDao.getBySubmissionJids(programmingSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : gradingModels.keySet()) {
                        double submissionScore = gradingModels.get(submissionJid).get(gradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                }
            }

            problemSetProblemProgressBuilder.add(new ProblemSetProblemWithScore(ProblemSetProblemServiceUtils.createFromModel(problemSetProblemModel), maxScore));
        }

        return new Page<>(problemSetProblemProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addProblemSetProblem(String problemSetJid, String problemJid, String problemSecret, String alias, ProblemSetProblemType type, ProblemSetProblemStatus status, String userJid, String userIpAddress) {
        ProblemSetProblemModel problemSetProblemModel = new ProblemSetProblemModel();
        problemSetProblemModel.problemSetJid = problemSetJid;
        problemSetProblemModel.problemJid = problemJid;
        problemSetProblemModel.problemSecret = problemSecret;
        problemSetProblemModel.alias = alias;
        problemSetProblemModel.type = type.name();
        problemSetProblemModel.status = status.name();

        problemSetProblemDao.persist(problemSetProblemModel, userJid, userIpAddress);
    }

    @Override
    public void updateProblemSetProblem(long problemSetProblemId, String alias, ProblemSetProblemStatus status, String userJid, String userIpAddress) {
        ProblemSetProblemModel problemSetProblemModel = problemSetProblemDao.findById(problemSetProblemId);
        problemSetProblemModel.alias = alias;
        problemSetProblemModel.status = status.name();

        problemSetProblemDao.edit(problemSetProblemModel, userJid, userIpAddress);
    }

    @Override
    public void removeProblemSetProblem(long problemSetProblemId) {
        ProblemSetProblemModel problemSetProblemModel = problemSetProblemDao.findById(problemSetProblemId);

        problemSetProblemDao.remove(problemSetProblemModel);
    }

    @Override
    public Map<String, String> getProgrammingProblemJidToAliasMapByProblemSetJid(String problemSetJid) {
        List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.getByProblemSetJid(problemSetJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
            if (ProblemSetProblemType.PROGRAMMING.name().equals(problemSetProblemModel.type)) {
                map.put(problemSetProblemModel.problemJid, problemSetProblemModel.alias);
            }
        }

        return map;
    }

    @Override
    public Map<String, String> getBundleProblemJidToAliasMapByProblemSetJid(String problemSetJid) {
        List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.getByProblemSetJid(problemSetJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
            if (ProblemSetProblemType.BUNDLE.name().equals(problemSetProblemModel.type)) {
                map.put(problemSetProblemModel.problemJid, problemSetProblemModel.alias);
            }
        }

        return map;
    }

    @Override
    public ProblemSetProblem findProblemSetProblemByProblemSetJidAndProblemJid(String problemSetJid, String problemJid) {
        return ProblemSetProblemServiceUtils.createFromModel(problemSetProblemDao.findByProblemSetJidAndProblemJid(problemSetJid, problemJid));
    }
}
