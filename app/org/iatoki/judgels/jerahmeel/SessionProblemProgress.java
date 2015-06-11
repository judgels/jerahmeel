package org.iatoki.judgels.jerahmeel;

public class SessionProblemProgress {
    private final SessionProblem sessionProblem;
    private final ProblemProgress problemProgress;

    public SessionProblemProgress(SessionProblem sessionProblem, ProblemProgress problemProgress) {
        this.sessionProblem = sessionProblem;
        this.problemProgress = problemProgress;
    }

    public SessionProblem getSessionProblem() {
        return sessionProblem;
    }

    public ProblemProgress getProblemProgress() {
        return problemProgress;
    }
}
