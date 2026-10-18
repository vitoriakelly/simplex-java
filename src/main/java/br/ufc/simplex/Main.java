package br.ufc.simplex;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.model.Constraint;
import br.ufc.simplex.model.LinearProgram;
import br.ufc.simplex.output.TableauPrinter;
import br.ufc.simplex.solver.SolutionStatus;
import br.ufc.simplex.solver.StandardFormConverter;
import br.ufc.simplex.solver.TwoPhaseSimplex;

public class Main {

    public static void main(String[] args) {

        String filePath = args.length > 0
                ? args[0]
                : "entradas/exemplo01.txt";

        try {
            ProblemReader reader = new ProblemReader();
            LinearProgram problem = reader.read(filePath);

            System.out.println(
                    "=== PROBLEMA DE PROGRAMACAO LINEAR ==="
            );

            System.out.println(
                    "Variaveis: " + problem.variableCount()
            );

            System.out.println(
                    "Funcao objetivo: "
                    + Arrays.toString(problem.objective())
            );

            System.out.println("\nRestricoes:");

            for (Constraint constraint : problem.constraints()) {
                System.out.println(
                        Arrays.toString(constraint.coefficients())
                        + " " + constraint.type()
                        + " " + constraint.rightHandSide()
                );
            }
            StandardFormConverter converter
                    = new StandardFormConverter();

            var standard = converter.convert(problem);

            System.out.println("\n=== FORMA PADRAO ===");

            List<String> names = standard.variableNames();

            System.out.println("Variaveis: " + names);

            for (int i = 0; i < standard.matrix().length; i++) {

                System.out.print(
                        "Restricao " + (i + 1) + ": "
                );

                for (int j = 0; j < names.size(); j++) {
                    System.out.printf(
                            Locale.US,
                            "%8.2f ",
                            standard.matrix()[i][j]
                    );
                }

                System.out.printf(
                        Locale.US,
                        " = %.2f%n",
                        standard.rhs()[i]
                );
            }

            System.out.println("\nBase inicial:");

            for (int index : standard.basis()) {
                System.out.println(names.get(index));
            }

            System.out.println("\nVariaveis artificiais:");

            for (int index : standard.artificialColumns()) {
                System.out.println(names.get(index));
            }

            TwoPhaseSimplex solver = new TwoPhaseSimplex();
            TableauPrinter printer = new TableauPrinter();

            TwoPhaseSimplex.Result result = solver.solve(
                    problem,
                    (phase, iteration, tableau) -> {

                        System.out.printf(
                                "%n=== %s - ITERACAO %d ===%n",
                                phase,
                                iteration
                        );

                        printer.print(tableau);
                    }
            );

            System.out.println(
                    "\n=== RESULTADO DA FASE I ==="
            );

            System.out.println(
                    "Total de iteracoes: "
                    + result.phaseOneIterations()
            );

            System.out.printf(
                    Locale.US,
                    "Valor de W: %.6f%n",
                    result.phaseOneW()
            );

            if (result.status() == SolutionStatus.INFEASIBLE) {

                System.out.println(
                        "STATUS: PROBLEMA INVIAVEL"
                );

                return;
            }

            System.out.println(
                    "STATUS: SOLUCAO VIAVEL ENCONTRADA"
            );

            System.out.println("\n=== RESULTADO FINAL ===");

            if (result.status() == SolutionStatus.UNBOUNDED) {

                System.out.println(
                        "STATUS: PROBLEMA ILIMITADO"
                );

            } else {

                System.out.println(
                        "STATUS: SOLUCAO OTIMA"
                );

                double[] solution = result.solution();

                for (int j = 0; j < solution.length; j++) {
                    System.out.printf(
                            Locale.US,
                            "x%d = %.6f%n",
                            j + 1,
                            solution[j]
                    );
                }

                System.out.printf(
                        Locale.US,
                        "Z maximo = %.6f%n",
                        result.objectiveValue()
                );
            }

            System.out.println(
                    "Iteracoes da Fase I: "
                    + result.phaseOneIterations()
            );

            System.out.println(
                    "Iteracoes da Fase II: "
                    + result.phaseTwoIterations()
            );

            System.out.println(
                    "Pivoteamentos de limpeza: "
                    + result.cleanupIterations()
            );

            System.out.println(
                    "Total de pivoteamentos: "
                    + result.totalIterations()
            );

        } catch (Exception e) {

            System.err.println(
                    "Erro: " + e.getMessage()
            );

            System.exit(1);
        }
    }

}
