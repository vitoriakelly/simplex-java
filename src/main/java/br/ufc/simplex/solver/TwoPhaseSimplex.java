
package br.ufc.simplex.solver;

import br.ufc.simplex.model.LinearProgram;

import java.util.List;

public class TwoPhaseSimplex {

@FunctionalInterface
public interface IterationListener {
    void onTableau(
        String phase,
        int iteration,
        Tableau tableau
    );
}

    private static final double EPS = 1e-8;
    private static final int MAX_ITERATIONS = 1000;

    public record Result(
        SolutionStatus status,
        double[] solution,
        double objectiveValue,
        double phaseOneW,
        int phaseOneIterations,
        int phaseTwoIterations
    ) {}

    
public Result solve(LinearProgram problem) {
    return solve(problem, (phase, iteration, tableau) -> {});
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
        int phaseOneIterations = runSimplex(
    tableau, listener
);

        double w = -tableau.objectiveValue();

        if (w > EPS) {
            return new Result(
                SolutionStatus.INFEASIBLE,
                new double[0],
                Double.NaN,
                w,
                phaseOneIterations,
                0
            );
        }

        if (Math.abs(w) > EPS) {
            throw new IllegalStateException(
                "Resultado numerico inconsistente na Fase I."
            );
        }

        // Preparar a Fase II
        Tableau phaseTwo = tableau.beginPhaseTwo(
            problem.objective(),
            standard.artificialColumns()
        );
        listener.onTableau("FASE II", 0, phaseTwo);

        // Executar a Fase II
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
                    phaseTwoIterations
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

                if (index < solution.length) {
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
            phaseTwoIterations
        );
    }

 
private int runSimplex(
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
                "Falha ao encontrar pivô na Fase I."
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
