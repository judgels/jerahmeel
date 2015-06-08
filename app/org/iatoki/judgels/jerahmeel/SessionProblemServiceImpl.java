package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel_;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SessionProblemServiceImpl implements SessionProblemService {
    private final SessionProblemDao sessionProblemDao;

    public SessionProblemServiceImpl(SessionProblemDao sessionProblemDao) {
        this.sessionProblemDao = sessionProblemDao;
    }

    @Override
    public boolean isInSessionByProblemJidAndAlias(String sessionJid, String problemJid, String alias) {
        return sessionProblemDao.existBySessionJidProblemJidAndAlias(sessionJid, problemJid, alias);
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

        List<SessionProblem> sessionProblems = sessionProblemModels.stream().map(s -> createFromModel(s)).collect(Collectors.toList());

        return new Page<>(sessionProblems, totalPages, pageIndex, pageSize);

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

    private SessionProblem createFromModel(SessionProblemModel s) {
        return new SessionProblem(s.id, s.sessionJid, s.problemJid, s.problemSecret, s.alias, SessionProblemType.valueOf(s.type), SessionProblemStatus.valueOf(s.status));
    }
}
