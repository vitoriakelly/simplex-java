
package br.ufc.simplex;

import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.model.Constraint;
import br.ufc.simplex.model.LinearProgram;
import br.ufc.simplex.solver.StandardFormConverter;
import br.ufc.simplex.solver.Tableau;
import br.ufc.simplex.output.TableauPrinter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
                    + " "
                    + constraint.type()
                    + " "
                    + constraint.rightHandSide()
                );
            }
            StandardFormConverter converter =
                    new StandardFormConverter();

            StandardFormConverter.StandardForm standard =
                    converter.convert(problem);

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
            Tableau tableau = new Tableau(standard);

            TableauPrinter printer = new TableauPrinter();

            System.out.println("\n=== INICIANDO FASE I ===");

            printer.print(tableau);

            int iterations = 0;
            int maxIterations = 1000;

            while (true) {

                int entering =
                        tableau.chooseEnteringVariable();

                if (entering == -1) {
                    break;
                }

                int leaving =
                        tableau.chooseLeavingVariable(entering);

                if (leaving == -1) {
                    throw new IllegalStateException(
                        "Nenhuma variavel sainte na Fase I."
                    );
                }

                String enteringName =
                    tableau.variableNames().get(entering);

                String leavingName =
                    tableau.variableNames().get(
                        tableau.basis()[leaving]
                    );

                System.out.println(
                    "\n=== ITERACAO "
                    + (iterations + 1)
                    + " ==="
                );

                System.out.println(
                    "Variavel entrante: " + enteringName
                );

                System.out.println(
                    "Variavel sainte: " + leavingName
                );

                tableau.pivot(leaving, entering);

                iterations++;

                System.out.println("\nTableau atualizado:");

                printer.print(tableau);

                if (iterations >= maxIterations) {
                    throw new IllegalStateException(
                        "Limite de iteracoes atingido."
                    );
                }
            }

            // 6. Verificar resultado da Fase I
            // Estamos maximizando -W
            double w = -tableau.objectiveValue();

            if (Math.abs(w) < 1e-8) {
                w = 0;
            }

            System.out.println(
                "\n=== RESULTADO DA FASE I ==="
            );

            System.out.println(
                "Total de iteracoes: " + iterations
            );

            System.out.printf(
                Locale.US,
                "Valor de W: %.6f%n",
                w
            );

            if (w > 1e-8) {

                System.out.println(
                    "STATUS: PROBLEMA INVIAVEL"
                );

            } else {

                System.out.println(
                    "STATUS: SOLUCAO VIAVEL ENCONTRADA"
                );

                System.out.println(
                    "O problema pode prosseguir "
                    + "para a Fase II."
                );

// 7. Preparar a Fase II
System.out.println("\n=== INICIANDO FASE II ===");

Tableau phaseTwo = tableau.beginPhaseTwo(
    problem.objective(),
    standard.artificialColumns()
);

System.out.println("\nTableau inicial da Fase II:");
printer.print(phaseTwo);

// 8. Executar as iteracoes da Fase II
int phaseTwoIterations = 0;
boolean unbounded = false;

while (true) {

    int entering = phaseTwo.chooseEnteringVariable();

    if (entering == -1) {
        break;
    }

    int leaving = phaseTwo.chooseLeavingVariable(entering);

    if (leaving == -1) {
        unbounded = true;
        break;
    }

    System.out.println(
        "\n=== FASE II - ITERACAO "
        + (phaseTwoIterations + 1)
        + " ==="
    );

    System.out.println(
        "Variavel entrante: "
        + phaseTwo.variableNames().get(entering)
    );

    System.out.println(
        "Variavel sainte: "
        + phaseTwo.variableNames().get(
            phaseTwo.basis()[leaving]
        )
    );

    phaseTwo.pivot(leaving, entering);

    phaseTwoIterations++;

    printer.print(phaseTwo);

    if (phaseTwoIterations >= maxIterations) {
        throw new IllegalStateException(
            "Limite de iteracoes da Fase II atingido."
        );
    }
}

// 9. Exibir o resultado final
System.out.println("\n=== RESULTADO FINAL ===");

if (unbounded) {

    System.out.println("STATUS: PROBLEMA ILIMITADO");

} else {

    double[] solution =
        new double[phaseTwo.variableNames().size()];

    double[][] finalRows = phaseTwo.rows();
    int[] finalBasis = phaseTwo.basis();

    int rhsColumn = solution.length;

    for (int i = 0; i < finalBasis.length; i++) {
        solution[finalBasis[i]] = finalRows[i][rhsColumn];
    }

    System.out.println("STATUS: SOLUCAO OTIMA");

    for (int j = 0; j < problem.variableCount(); j++) {
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
        phaseTwo.objectiveValue()
    );
}

System.out.println(
    "Iteracoes da Fase I: " + iterations
);

System.out.println(
    "Iteracoes da Fase II: " + phaseTwoIterations
);

            }

        } catch (Exception e) {

            System.err.println(
                "Erro: " + e.getMessage()
            );

            System.exit(1);
        }
    }
}
