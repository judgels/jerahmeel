package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;

import java.util.List;

public final class SessionProgressCacheUtils {

    private static SessionProgressCacheUtils instance;

    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    public SessionProgressCacheUtils(SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    public static synchronized void buildInstance(SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        if (instance != null) {
            throw new UnsupportedOperationException("SessionProgressCacheUtils instance has already been built");
        }
        instance = new SessionProgressCacheUtils(sessionProblemDao, userItemDao);
    }

    static SessionProgressCacheUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SessionProgressCacheUtils instance has not been built");
        }
        return instance;
    }

    void updateSessionProblemProgressWithBundleSubmissions(String userJid, String containerJid, String problemJid, List<BundleSubmission> bundleSubmissions) {
        boolean completed = false;
        for (int i = 0; (!completed) && (i < bundleSubmissions.size()); ++i) {
            if (Double.compare(bundleSubmissions.get(i).getLatestScore(), 100) == 0) {
                completed = true;
            }
        }

        updateSessionProblemProgress(userJid, containerJid, problemJid, completed);
    }

    void updateSessionProblemProgressWithProgrammingSubmissions(String userJid, String containerJid, String problemJid, List<ProgrammingSubmission> programmingSubmissions) {
        boolean completed = false;
        for (int i = 0; (!completed) && (i < programmingSubmissions.size()); ++i) {
            if (Double.compare(programmingSubmissions.get(i).getLatestScore(), 100) == 0) {
                completed = true;
            }
        }

        updateSessionProblemProgress(userJid, containerJid, problemJid, completed);
    }

    private void updateSessionProblemProgress(String userJid, String containerJid, String problemJid, boolean completed) {
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
}
