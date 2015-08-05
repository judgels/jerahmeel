package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.UserItemStatus;

import java.util.List;

public interface UserItemService {
    boolean isViewed(String userJid, String itemJid);

    boolean isUserItemExist(String userJid, String itemJid);

    boolean isUserItemExist(String userJid, String itemJid, UserItemStatus status);

    void upsertUserItem(String userJid, String itemJid, UserItemStatus status);

    List<UserItem> findAllUserItemByUserJid(String userJid);

    List<UserItem> findAllUserItemByItemJid(String itemJid);
}
