package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.ProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemWithProgress;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.sandalphon.ProblemType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named("sessionProblemService")
public final class SessionProblemServiceImpl implements SessionProblemService {

    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public SessionProblemServiceImpl(BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean aliasExistsInSession(String sessionJid, String alias) {
        return sessionProblemDao.existsBySessionJidAndAlias(sessionJid, alias);
    }

    @Override
    public SessionProblem findSessionProblemById(long sessionProblemId) throws SessionProblemNotFoundException {
        SessionProblemModel sessionProblemModel = sessionProblemDao.findById(sessionProblemId);
        if (sessionProblemModel != null) {
            return SessionProblemServiceUtils.createFromModel(sessionProblemModel);
        } else {
            throw new SessionProblemNotFoundException("Session Problem Not Found");
        }
    }

    @Override
    public Page<SessionProblem> getPageOfSessionProblems(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionProblemDao.countByFilters(filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionProblem> sessionProblems = sessionProblemModels.stream().map(m -> SessionProblemServiceUtils.createFromModel(m)).collect(Collectors.toList());

        return new Page<>(sessionProblems, totalPages, pageIndex, pageSize);

    }

    @Override
    public Page<SessionProblemWithProgress> getPageOfSessionProblemsWithProgress(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionProblemDao.countByFilters(filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid, SessionProblemModel_.status, SessionProblemStatus.VISIBLE.name()), ImmutableMap.of());
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid, SessionProblemModel_.status, SessionProblemStatus.VISIBLE.name()), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<SessionProblemWithProgress> sessionProblemProgressBuilder = ImmutableList.builder();
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            ProblemProgress progress = ProblemProgress.NOT_VIEWED;
            if (userItemDao.existsByUserJidAndItemJid(userJid, sessionProblemModel.problemJid)) {
                UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, sessionProblemModel.problemJid);
                if (UserItemStatus.VIEWED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.VIEWED;
                } else if (UserItemStatus.COMPLETED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.COMPLETED;
                }
            }

            double maxScore = -1;
            if (!progress.equals(ProblemProgress.NOT_VIEWED)) {
                if (sessionProblemModel.type.equals(ProblemType.BUNDLE.name())) {
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<BundleGradingModel>> bundleGradingModels = bundleGradingDao.getBySubmissionJids(bundleSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : bundleGradingModels.keySet()) {
                        double submissionScore = bundleGradingModels.get(submissionJid).get(bundleGradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                } else if (sessionProblemModel.type.equals(ProblemType.PROGRAMMING.name())) {
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<ProgrammingGradingModel>> gradingModels = programmingGradingDao.getBySubmissionJids(programmingSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : gradingModels.keySet()) {
                        double submissionScore = gradingModels.get(submissionJid).get(gradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                }
            }

            sessionProblemProgressBuilder.add(new SessionProblemWithProgress(SessionProblemServiceUtils.createFromModel(sessionProblemModel), progress, maxScore));
        }

        return new Page<>(sessionProblemProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addSessionProblem(String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status, String userJid, String userIpAddress) {
        SessionProblemModel sessionProblemModel = new SessionProblemModel();
        sessionProblemModel.sessionJid = sessionJid;
        sessionProblemModel.problemJid = problemJid;
        sessionProblemModel.problemSecret = problemSecret;
        sessionProblemModel.alias = alias;
        sessionProblemModel.type = type.name();
        sessionProblemModel.status = status.name();

        sessionProblemDao.persist(sessionProblemModel, userJid, userIpAddress);
    }

    @Override
    public void updateSessionProblem(long sessionProblemId, String alias, SessionProblemStatus status, String userJid, String userIpAddress) {
        SessionProblemModel sessionProblemModel = sessionProblemDao.findById(sessionProblemId);
        sessionProblemModel.alias = alias;
        sessionProblemModel.status = status.name();

        sessionProblemDao.edit(sessionProblemModel, userJid, userIpAddress);
    }

    @Override
    public void removeSessionProblem(long sessionProblemId) {
        SessionProblemModel sessionProblemModel = sessionProblemDao.findById(sessionProblemId);

        sessionProblemDao.remove(sessionProblemModel);
    }

    @Override
    public Map<String, String> getProgrammingProblemJidToAliasMapBySessionJid(String sessionJid) {
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(sessionJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (SessionProblemType.PROGRAMMING.name().equals(sessionProblemModel.type)) {
                map.put(sessionProblemModel.problemJid, sessionProblemModel.alias);
            }
        }

        return map;
    }

    @Override
    public Map<String, String> getBundleProblemJidToAliasMapBySessionJid(String sessionJid) {
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(sessionJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (SessionProblemType.BUNDLE.name().equals(sessionProblemModel.type)) {
                map.put(sessionProblemModel.problemJid, sessionProblemModel.alias);
            }
        }

        return map;
    }

    @Override
    public SessionProblem findSessionProblemBySessionJidAndProblemJid(String sessionJid, String problemJid) {
        return SessionProblemServiceUtils.createFromModel(sessionProblemDao.findBySessionJidAndProblemJid(sessionJid, problemJid));
    }
}
