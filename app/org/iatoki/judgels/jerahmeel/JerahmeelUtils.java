package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Http;

import java.util.Arrays;
import java.util.List;

public class JerahmeelUtils {

    private JerahmeelUtils() {
        // prevent instantiation
    }

    public static List<String> getDefaultRoles() {
        return ImmutableList.of("user");
    }

    public static String getRolesFromSession() {
        return getFromSession("role");
    }

    public static void saveRolesInSession(List<String> roles) {
        putInSession("role", StringUtils.join(roles, ","));
    }

    public static boolean hasRole(String role) {
        return Arrays.asList(getFromSession("role").split(",")).contains(role);
    }

    public static void backupSession() {
        putInSession("realUserJid", getFromSession("userJid"));
        putInSession("realName", getFromSession("name"));
        putInSession("realUsername", getFromSession("username"));
        putInSession("realRole", getFromSession("role"));
        putInSession("realAvatar", getFromSession("avatar"));
        putInSession("realEmail", getFromSession("email"));
    }

    public static void setUserSession(org.iatoki.judgels.jophiel.commons.User user, User urielUser) {
        putInSession("userJid", user.getJid());
        putInSession("name", user.getName());
        putInSession("username", user.getUsername());
        saveRolesInSession(urielUser.getRoles());
        putInSession("avatar", user.getProfilePictureUrl().toString());
        putInSession("email", user.getEmail());
    }

    public static void restoreSession() {
        putInSession("userJid", getFromSession("realUserJid"));
        Http.Context.current().session().remove("realUserJid");
        putInSession("name", getFromSession("realName"));
        Http.Context.current().session().remove("realName");
        putInSession("username", getFromSession("realUsername"));
        Http.Context.current().session().remove("realUsername");
        putInSession("role", getFromSession("realRole"));
        Http.Context.current().session().remove("realRole");
        putInSession("avatar", getFromSession("realAvatar"));
        Http.Context.current().session().remove("realAvatar");
        putInSession("email", getFromSession("realEmail"));
        Http.Context.current().session().remove("realEmail");
    }

    public static boolean trullyHasRole(String role) {
        if (Http.Context.current().session().containsKey("realRole")) {
            return Arrays.asList(getFromSession("realRole").split(",")).contains(role);
        } else {
            return hasRole(role);
        }
    }

    private static void putInSession(String key, String value) {
        Http.Context.current().session().put(key, value);
    }

    private static String getFromSession(String key) {
        return Http.Context.current().session().get(key);
    }
}
