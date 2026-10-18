package br.ufc.simplex.output;

import java.util.Locale;

import br.ufc.simplex.solver.SolutionStatus;
import br.ufc.simplex.solver.TwoPhaseSimplex;

public final class SolutionReporter {

    private SolutionReporter() {}

    public static String format(TwoPhaseSimplex.Result result) {
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("              RESULTADO FINAL\n");
        sb.append("========================================\n\n");

        sb.append("STATUS: ").append(statusLabel(result.status())).append('\n');
        sb.append("Interpretacao: ").append(statusHint(result.status())).append('\n');
        sb.append('\n');

        if (result.status() == SolutionStatus.INFEASIBLE) {
            sb.append("A Fase I nao encontrou solucao viavel.\n");
            sb.append(String.format(
                    Locale.US,
                    "Valor de W: %.6f (deveria ser 0).%n",
                    result.phaseOneW()
            ));
            sb.append('\n');
            appendCounters(sb, result);
            return sb.toString();
        }

        if (result.status() == SolutionStatus.UNBOUNDED) {
            sb.append("A regiao viavel e ilimitada na direcao de melhoria.\n");
            sb.append("Nao existe valor maximo finito para Z.\n");
            sb.append('\n');
            appendCounters(sb, result);
            return sb.toString();
        }

        sb.append("Solucao otima encontrada:\n");
        double[] solution = result.solution();
        for (int j = 0; j < solution.length; j++) {
            double value = Math.abs(solution[j]) < 1e-9 ? 0.0 : solution[j];
            sb.append(String.format(
                    Locale.US,
                    "x%d = %.6f%n",
                    j + 1,
                    value
            ));
        }

        sb.append(String.format(
                Locale.US,
                "Z maximo = %.6f%n",
                result.objectiveValue()
        ));
        sb.append('\n');
        appendCounters(sb, result);

        return sb.toString();
    }

    public static String statusLabel(SolutionStatus status) {
        return switch (status) {
            case OPTIMAL -> "SOLUCAO OTIMA";
            case INFEASIBLE -> "PROBLEMA INVIAVEL";
            case UNBOUNDED -> "PROBLEMA ILIMITADO";
        };
    }

    public static String statusHint(SolutionStatus status) {
        return switch (status) {
            case OPTIMAL ->
                "Existe uma solucao viavel que maximiza Z.";
            case INFEASIBLE ->
                "Nenhuma solucao satisfaz todas as restricoes.";
            case UNBOUNDED ->
                "Z pode crescer indefinidamente.";
        };
    }

    private static void appendCounters(
            StringBuilder sb,
            TwoPhaseSimplex.Result result) {

        sb.append("Resumo do metodo:\n");
        sb.append("  Fase I (viabilidade) .... ")
                .append(result.phaseOneIterations())
                .append(" pivoteamento(s)\n");
        sb.append("  Transicao (limpeza) ..... ")
                .append(result.cleanupIterations())
                .append(" pivoteamento(s)\n");
        sb.append("  Fase II (otimizacao) .... ")
                .append(result.phaseTwoIterations())
                .append(" pivoteamento(s)\n");
        sb.append("  Total ................... ")
                .append(result.totalIterations())
                .append(" pivoteamento(s)\n");
    }
}
