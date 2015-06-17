package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.ProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SessionProblemServiceImpl implements SessionProblemService {

    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    public SessionProblemServiceImpl(SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean isInSessionByAlias(String sessionJid, String alias) {
        return sessionProblemDao.existBySessionJidAndAlias(sessionJid, alias);
    }

    @Override
    public SessionProblem findSessionProblemBySessionProblemId(long sessionProblemId) throws SessionProblemNotFoundException {
        SessionProblemModel sessionProblemModel = sessionProblemDao.findById(sessionProblemId);
        if (sessionProblemModel != null) {
            return createFromModel(sessionProblemModel);
        } else {
            throw new SessionProblemNotFoundException("Session Problem Not Found");
        }
    }

    @Override
    public Page<SessionProblem> findSessionProblems(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionProblemDao.countByFilters(filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionProblem> sessionProblems = sessionProblemModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(sessionProblems, totalPages, pageIndex, pageSize);

    }

    @Override
    public Page<SessionProblemProgress> findSessionProblems(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionProblemDao.countByFilters(filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<SessionProblemProgress> sessionProblemProgressBuilder = ImmutableList.builder();
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            ProblemProgress progress = ProblemProgress.NOT_VIEWED;
            if (userItemDao.existByUserJidAndItemJid(IdentityUtils.getUserJid(), sessionProblemModel.problemJid)) {
                UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, sessionProblemModel.problemJid);
                if (UserItemStatus.VIEWED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.VIEWED;
                } else if (UserItemStatus.COMPLETED.name().equals(userItemModel.status)) {
                    progress = ProblemProgress.COMPLETED;
                }
            }

            sessionProblemProgressBuilder.add(new SessionProblemProgress(createFromModel(sessionProblemModel), progress));
        }

        return new Page<>(sessionProblemProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addSessionProblem(String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status) {
        SessionProblemModel sessionProblemModel = new SessionProblemModel();
        sessionProblemModel.sessionJid = sessionJid;
        sessionProblemModel.problemJid = problemJid;
        sessionProblemModel.problemSecret = problemSecret;
        sessionProblemModel.alias = alias;
        sessionProblemModel.type = type.name();
        sessionProblemModel.status = status.name();

        sessionProblemDao.persist(sessionProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeSessionProblem(long sessionProblemId) throws SessionProblemNotFoundException {
        SessionProblemModel sessionProblemModel = sessionProblemDao.findById(sessionProblemId);
        if (sessionProblemModel != null) {
            sessionProblemDao.remove(sessionProblemModel);
        } else {
            throw new SessionProblemNotFoundException("Session Problem Not Found");
        }
    }

    @Override
    public Map<String, String> findProgrammingProblemJidToAliasMapBySessionJid(String sessionJid) {
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findBySessionJid(sessionJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (SessionProblemType.PROGRAMMING.name().equals(sessionProblemModel.type)) {
                map.put(sessionProblemModel.problemJid, sessionProblemModel.alias);
            }
        }

        return map;
    }

    @Override
    public Map<String, String> findBundleProblemJidToAliasMapBySessionJid(String sessionJid) {
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findBySessionJid(sessionJid);

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
        return createFromModel(sessionProblemDao.findBySesssionJidAndProblemJid(sessionJid, problemJid));
    }

    private SessionProblem createFromModel(SessionProblemModel model) {
        return new SessionProblem(model.id, model.sessionJid, model.problemJid, model.problemSecret, model.alias, SessionProblemType.valueOf(model.type), SessionProblemStatus.valueOf(model.status));
    }
}
