package com.stupid_genius.mathtutor;

import java.util.Iterator;
import java.util.Scanner;

public class MathTutor implements Iterator<SimpleProblem> {
	private ProblemFactory problemFactory;
	private int numCorrect;
	private boolean normalShutdown = false;

//	private int sessionCount;

	/**
	 * Configurations:
	 * - operation
	 * - difficulty level
	 * - allow negatives
	 * - number of problems?
	 * - time?
	 *
	 * @param op
	 * @param difficulty
	 * @param allowNegatives
	 */
	public void startSession(OperationEnum op, int difficulty, boolean allowNegatives) {
		problemFactory = new ProblemFactory(op, difficulty, allowNegatives);
		numCorrect = 0;
//		sessionCount = 0;
	}

	public void killSession() {

	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public SimpleProblem next() {
		return problemFactory.getProblem();
	}

	public void recordAnswer(boolean isCorrect) {
		if (isCorrect) {
			++numCorrect;
		}
	}

	public int getCorrectProblems() {
		return numCorrect;
	}

	public int getTotalProblems() {
		return problemFactory.getProblemsCreated();
	}

	public String toString() {
		StringBuilder report = new StringBuilder();
		report.append(problemFactory.toString());
		report.append(String.format("\nNumber correct: %d", numCorrect));
		return report.toString();
	}

	public static void main(String[] args) {
		final MathTutor tutor = new MathTutor();
		tutor.startSession(OperationEnum.RANDOM, 10, true);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (tutor.normalShutdown) {
					return;
				}
				tutor.killSession();
				System.out.println(tutor.toString());
			}
		});

		System.out.println("Welcome to MathTutor CLI!");
		Scanner stdin = new Scanner(System.in);
		while (tutor.hasNext()) {
			SimpleProblem problem = tutor.next();
			System.out.println(problem.toString());
			int input = stdin.nextInt();
			boolean isCorrect = problem.checkAnswer(input);
			tutor.recordAnswer(isCorrect);
			if (isCorrect) {
				System.out.println("Correct!");
			} else {
				System.out.printf("Incorrect: %s %d\n", problem.toString(), input);
			}
		}
		tutor.killSession();
		System.out.println(tutor.toString());
		tutor.normalShutdown = true;
	}
}
