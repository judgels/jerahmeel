package org.iatoki.judgels.jerahmeel;

public final class SessionProblemWithProgress {

    public static final double MINIMUM_SCORE = -1234567890;

    private final SessionProblem sessionProblem;
    private final ProblemProgress problemProgress;
    private final double score;

    public SessionProblemWithProgress(SessionProblem sessionProblem, ProblemProgress problemProgress, double score) {
        this.sessionProblem = sessionProblem;
        this.problemProgress = problemProgress;
        this.score = score;
    }

    public SessionProblem getSessionProblem() {
        return sessionProblem;
    }

    public ProblemProgress getProblemProgress() {
        return problemProgress;
    }

    public double getScore() {
        return score;
    }
}
