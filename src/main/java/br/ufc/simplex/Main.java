package br.ufc.simplex;

import java.util.List;
import java.util.Locale;

import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.model.LinearProgram;
import br.ufc.simplex.output.ProblemFormatter;
import br.ufc.simplex.output.SolutionReporter;
import br.ufc.simplex.output.TableauPrinter;
import br.ufc.simplex.solver.SolutionStatus;
import br.ufc.simplex.solver.StandardFormConverter;
import br.ufc.simplex.solver.TwoPhaseSimplex;
import br.ufc.simplex.ui.SimplexFrame;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || "--gui".equals(args[0])) {
            SimplexFrame.launch();
            return;
        }

        if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            printHelp();
            return;
        }

        runCli(args[0]);
    }

    private static void printHelp() {
        System.out.println("Uso:");
        System.out.println("  java -cp target/classes br.ufc.simplex.Main");
        System.out.println("      Abre a interface grafica.");
        System.out.println();
        System.out.println("  java -cp target/classes br.ufc.simplex.Main <arquivo.txt>");
        System.out.println("      Executa no terminal com saida detalhada.");
        System.out.println();
        System.out.println("  java -cp target/classes br.ufc.simplex.Main --gui");
        System.out.println("      Forca a abertura da interface grafica.");
    }

    private static void runCli(String filePath) {
        try {
            ProblemReader reader = new ProblemReader();
            LinearProgram problem = reader.read(filePath);

            System.out.println("=== PROBLEMA DE PROGRAMACAO LINEAR ===");
            System.out.println();
            System.out.print(ProblemFormatter.format(problem));

            StandardFormConverter converter = new StandardFormConverter();
            var standard = converter.convert(problem);

            System.out.println("=== FORMA PADRAO ===");
            List<String> names = standard.variableNames();
            System.out.println("Variaveis: " + names);

            for (int i = 0; i < standard.matrix().length; i++) {
                System.out.print("Restricao " + (i + 1) + ": ");

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
                System.out.println("  " + names.get(index));
            }

            System.out.println("\nVariaveis artificiais:");
            if (standard.artificialColumns().isEmpty()) {
                System.out.println("  (nenhuma)");
            } else {
                for (int index : standard.artificialColumns()) {
                    System.out.println("  " + names.get(index));
                }
            }

            TwoPhaseSimplex solver = new TwoPhaseSimplex();
            TableauPrinter printer = new TableauPrinter();

            TwoPhaseSimplex.Result result = solver.solve(
                    problem,
                    (phase, iteration, tableau) -> {
                        System.out.println();
                        System.out.println(phaseHeader(phase, iteration));
                        printer.print(tableau);
                    }
            );

            System.out.println();
            System.out.println("=== RESULTADO DA FASE I ===");
            System.out.println(
                    "Total de iteracoes: " + result.phaseOneIterations()
            );
            System.out.printf(
                    Locale.US,
                    "Valor de W: %.6f%n",
                    result.phaseOneW()
            );

            if (result.status() == SolutionStatus.INFEASIBLE) {
                System.out.println("STATUS: PROBLEMA INVIAVEL");
                System.out.println();
                System.out.print(SolutionReporter.format(result));
                return;
            }

            System.out.println("STATUS: SOLUCAO VIAVEL ENCONTRADA");
            System.out.println();
            System.out.print(SolutionReporter.format(result));

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String phaseHeader(String phase, int iteration) {
        return switch (phase) {
            case "FASE I" -> iteration == 0
                    ? "=== FASE I (viabilidade) — tableau inicial ==="
                    : "=== FASE I — iteracao " + iteration + " ===";
            case "TRANSICAO" ->
                    "=== TRANSICAO — limpeza de artificial #" + iteration + " ===";
            case "FASE II" -> iteration == 0
                    ? "=== FASE II (otimizacao) — tableau inicial ==="
                    : "=== FASE II — iteracao " + iteration + " ===";
            default -> "=== " + phase + " - ITERACAO " + iteration + " ===";
        };
    }
}
