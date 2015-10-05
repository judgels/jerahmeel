package org.iatoki.judgels.jerahmeel;

public class ProblemSetProblemWithScore {

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
