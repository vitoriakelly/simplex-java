
package br.ufc.simplex.solver;

import java.util.List;

import br.ufc.simplex.model.LinearProgram;

public class TwoPhaseSimplex {

    private static final double EPS = 1e-8;
    private static final int MAX_ITERATIONS = 1000;

    @FunctionalInterface
    public interface IterationListener {
        void onTableau(
            String phase,
            int iteration,
            Tableau tableau
        );
    }

    public record Result(
        SolutionStatus status,
        double[] solution,
        double objectiveValue,
        double phaseOneW,
        int phaseOneIterations,
        int phaseTwoIterations,
        int cleanupIterations
    ) {
        public int totalIterations() {
            return phaseOneIterations
                + phaseTwoIterations
                + cleanupIterations;
        }
    }

    public Result solve(LinearProgram problem) {
        return solve(
            problem,
            (phase, iteration, tableau) -> {}
        );
    }

    public Result solve(
            LinearProgram problem,
            IterationListener listener) {

        StandardFormConverter converter =
            new StandardFormConverter();

        StandardFormConverter.StandardForm standard =
            converter.convert(problem);

        Tableau tableau = new Tableau(standard);

        listener.onTableau("FASE I", 0, tableau);

        int phaseOneIterations =
            runPhaseOne(tableau, listener);

        double w = -tableau.objectiveValue();

        if (Math.abs(w) < EPS) {
            w = 0;
        }

        if (w > EPS) {
            return new Result(
                SolutionStatus.INFEASIBLE,
                new double[0],
                Double.NaN,
                w,
                phaseOneIterations,
                0,
                0
            );
        }

        if (w < -EPS) {
            throw new IllegalStateException(
                "Valor inconsistente na Fase I."
            );
        }

        final int[] cleanupCount = {0};

        Tableau phaseTwo = tableau.beginPhaseTwo(
            problem.objective(),
            standard.artificialColumns(),

            (entering, leaving, currentTableau) -> {

                cleanupCount[0]++;

                System.out.println(
                    "\n=== LIMPEZA DA BASE ==="
                );

                System.out.println(
                    "Entrante: " + entering
                );

                System.out.println(
                    "Sainte: " + leaving
                );

                listener.onTableau(
                    "TRANSICAO",
                    cleanupCount[0],
                    currentTableau
                );
            }
        );

        listener.onTableau("FASE II", 0, phaseTwo);

        int phaseTwoIterations = 0;

        while (true) {

            int entering =
                phaseTwo.chooseEnteringVariable();

            if (entering == -1) {
                break;
            }

            int leaving =
                phaseTwo.chooseLeavingVariable(entering);

            if (leaving == -1) {
                return new Result(
                    SolutionStatus.UNBOUNDED,
                    new double[0],
                    Double.POSITIVE_INFINITY,
                    0,
                    phaseOneIterations,
                    phaseTwoIterations,
                    cleanupCount[0]
                );
            }

            phaseTwo.pivot(leaving, entering);
            phaseTwoIterations++;

            listener.onTableau(
                "FASE II",
                phaseTwoIterations,
                phaseTwo
            );

            if (phaseTwoIterations >= MAX_ITERATIONS) {
                throw new IllegalStateException(
                    "Limite de iteracoes da Fase II atingido."
                );
            }
        }

        double[] solution =
            new double[problem.variableCount()];

        double[][] rows = phaseTwo.rows();
        int[] basis = phaseTwo.basis();

        int rhsColumn =
            phaseTwo.variableNames().size();

        List<String> names =
            phaseTwo.variableNames();

        for (int i = 0; i < basis.length; i++) {

            int column = basis[i];
            String name = names.get(column);

            if (name.matches("x\\d+")) {

                int index =
                    Integer.parseInt(name.substring(1)) - 1;

                if (index >= 0 && index < solution.length) {
                    solution[index] =
                        rows[i][rhsColumn];
                }
            }
        }

        return new Result(
            SolutionStatus.OPTIMAL,
            solution,
            phaseTwo.objectiveValue(),
            0,
            phaseOneIterations,
            phaseTwoIterations,
            cleanupCount[0]
        );
    }

    private int runPhaseOne(
            Tableau tableau,
            IterationListener listener) {

        int iterations = 0;

        while (true) {

            int entering =
                tableau.chooseEnteringVariable();

            if (entering == -1) {
                return iterations;
            }

            int leaving =
                tableau.chooseLeavingVariable(entering);

            if (leaving == -1) {
                throw new IllegalStateException(
                    "Nenhuma variavel sainte na Fase I."
                );
            }

            tableau.pivot(leaving, entering);
            iterations++;

            listener.onTableau(
                "FASE I",
                iterations,
                tableau
            );

            if (iterations >= MAX_ITERATIONS) {
                throw new IllegalStateException(
                    "Limite de iteracoes da Fase I atingido."
                );
            }
        }
    }
}
