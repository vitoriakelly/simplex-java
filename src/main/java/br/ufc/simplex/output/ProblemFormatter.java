package br.ufc.simplex.output;

import java.util.Locale;

import br.ufc.simplex.model.Constraint;
import br.ufc.simplex.model.ConstraintType;
import br.ufc.simplex.model.LinearProgram;

public final class ProblemFormatter {

    private ProblemFormatter() {}

    public static String format(LinearProgram problem) {
        StringBuilder sb = new StringBuilder();

        sb.append("Maximizar\n");
        sb.append("  Z = ").append(formatExpression(problem.objective())).append('\n');
        sb.append('\n');
        sb.append("sujeito a\n");

        int index = 1;
        for (Constraint constraint : problem.constraints()) {
            sb.append("  (").append(index++).append(") ");
            sb.append(formatExpression(constraint.coefficients()));
            sb.append(' ').append(symbol(constraint.type())).append(' ');
            sb.append(formatNumber(constraint.rightHandSide()));
            sb.append('\n');
        }

        sb.append('\n');
        sb.append("com\n");
        sb.append("  ");
        for (int j = 0; j < problem.variableCount(); j++) {
            if (j > 0) {
                sb.append(", ");
            }
            sb.append('x').append(j + 1).append(" >= 0");
        }
        sb.append('\n');

        return sb.toString();
    }

    public static String formatExpression(double[] coefficients) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (int j = 0; j < coefficients.length; j++) {
            double value = coefficients[j];

            if (Math.abs(value) < 1e-12) {
                continue;
            }

            if (first) {
                if (value < 0) {
                    sb.append("-");
                }
                first = false;
            } else {
                sb.append(value < 0 ? " - " : " + ");
            }

            double abs = Math.abs(value);
            if (Math.abs(abs - 1.0) > 1e-12) {
                sb.append(formatNumber(abs));
            }
            sb.append('x').append(j + 1);
        }

        if (first) {
            sb.append('0');
        }

        return sb.toString();
    }

    public static String symbol(ConstraintType type) {
        return switch (type) {
            case LESS_OR_EQUAL -> "<=";
            case EQUAL -> "=";
            case GREATER_OR_EQUAL -> ">=";
        };
    }

    public static String formatNumber(double value) {
        if (Math.abs(value) < 1e-12) {
            return "0";
        }

        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.format(Locale.US, "%.0f", value);
        }

        return String.format(Locale.US, "%.4g", value);
    }
}
