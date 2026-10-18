
package br.ufc.simplex.solver;

import br.ufc.simplex.model.*;
import java.util.ArrayList;
import java.util.List;

public class StandardFormConverter {

    public record StandardForm(
        double[][] matrix,
        double[] rhs,
        List<String> variableNames,
        int[] basis,
        List<Integer> artificialColumns
    ) {}

    public StandardForm convert(LinearProgram problem) {

        int n = problem.variableCount();
        int m = problem.constraints().size();

        int extraColumns = 0;

       
for (Constraint constraint : problem.constraints()) {

    ConstraintType type = constraint.type();

    if (constraint.rightHandSide() < 0) {
        type = switch (type) {
            case LESS_OR_EQUAL ->
                ConstraintType.GREATER_OR_EQUAL;
            case GREATER_OR_EQUAL ->
                ConstraintType.LESS_OR_EQUAL;
            case EQUAL -> ConstraintType.EQUAL;
        };
    }

    extraColumns += switch (type) {
        case LESS_OR_EQUAL -> 1;
        case EQUAL -> 1;
        case GREATER_OR_EQUAL -> 2;
    };
}


        int totalColumns = n + extraColumns;

        double[][] matrix = new double[m][totalColumns];
        double[] rhs = new double[m];
        int[] basis = new int[m];

        List<String> names = new ArrayList<>();
        List<Integer> artificial = new ArrayList<>();

        for (int j = 0; j < n; j++) {
            names.add("x" + (j + 1));
        }

        int nextColumn = n;
        int slackCount = 0;
        int excessCount = 0;
        int artificialCount = 0;

        for (int i = 0; i < m; i++) {

            Constraint constraint = problem.constraints().get(i);

            double sign = constraint.rightHandSide() < 0 ? -1 : 1;
            rhs[i] = sign * constraint.rightHandSide();

            for (int j = 0; j < n; j++) {
                matrix[i][j] = sign * constraint.coefficients()[j];
            }

            ConstraintType type = constraint.type();

            if (sign < 0) {
                type = switch (type) {
                    case LESS_OR_EQUAL -> ConstraintType.GREATER_OR_EQUAL;
                    case GREATER_OR_EQUAL -> ConstraintType.LESS_OR_EQUAL;
                    case EQUAL -> ConstraintType.EQUAL;
                };
            }

            switch (type) {

                case LESS_OR_EQUAL -> {
                    int col = nextColumn++;
                    matrix[i][col] = 1;
                    names.add("s" + (++slackCount));
                    basis[i] = col;
                }

                case EQUAL -> {
                    int col = nextColumn++;
                    matrix[i][col] = 1;
                    names.add("a" + (++artificialCount));
                    artificial.add(col);
                    basis[i] = col;
                }

                case GREATER_OR_EQUAL -> {
                    int excess = nextColumn++;
                    matrix[i][excess] = -1;
                    names.add("e" + (++excessCount));

                    int art = nextColumn++;
                    matrix[i][art] = 1;
                    names.add("a" + (++artificialCount));
                    artificial.add(art);
                    basis[i] = art;
                }
            }
        }

        return new StandardForm(
            matrix, rhs, names, basis, artificial
        );
    }
}
