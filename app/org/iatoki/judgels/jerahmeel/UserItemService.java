package org.iatoki.judgels.jerahmeel;

import java.util.List;

public interface UserItemService {
    boolean isViewed(String userJid, String itemJid);

    boolean isUserItemExist(String userJid, String itemJid, UserItemStatus status);

    void upsertUserItem(String userJid, String itemJid, UserItemStatus status);

    List<UserItem> findAllUserItemByUserJid(String userJid);

    List<UserItem> findAllUserItemByItemJid(String itemJid);
}
