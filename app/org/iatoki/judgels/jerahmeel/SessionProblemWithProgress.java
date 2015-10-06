package org.iatoki.judgels.jerahmeel;

public class SessionProblemWithProgress {

    private final SessionProblem sessionProblem;
    private final ProblemProgress problemProgress;
    private final double scores;

    public SessionProblemWithProgress(SessionProblem sessionProblem, ProblemProgress problemProgress, double scores) {
        this.sessionProblem = sessionProblem;
        this.problemProgress = problemProgress;
        this.scores = scores;
    }

    public SessionProblem getSessionProblem() {
        return sessionProblem;
    }

    public ProblemProgress getProblemProgress() {
        return problemProgress;
    }

    public double getScores() {
        return scores;
    }
}
