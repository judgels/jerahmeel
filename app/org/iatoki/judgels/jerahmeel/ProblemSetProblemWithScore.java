package org.iatoki.judgels.jerahmeel;

public final class ProblemSetProblemWithScore {

    public static final double MINIMUM_SCORE = -1234567890;

    private final ProblemSetProblem problemSetProblem;
    private final double score;

    public ProblemSetProblemWithScore(ProblemSetProblem problemSetProblem, double score) {
        this.problemSetProblem = problemSetProblem;
        this.score = score;
    }

    public ProblemSetProblem getProblemSetProblem() {
        return problemSetProblem;
    }

    public double getScore() {
        return score;
    }
}
