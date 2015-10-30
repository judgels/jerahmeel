package org.iatoki.judgels.jerahmeel.controllers;

import play.mvc.Controller;

final class StatementControllerUtils {

    private StatementControllerUtils() {
        // prevent instantiation
    }

    static void setCurrentStatementLanguage(String languageCode) {
        Controller.session("currentStatementLanguage", languageCode);
    }

    static String getCurrentStatementLanguage() {
        String lang = Controller.session("currentStatementLanguage");
        if (lang == null) {
            return "en-US";
        } else {
            return lang;
        }
    }
}
