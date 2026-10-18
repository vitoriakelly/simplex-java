
package br.ufc.simplex.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tableau {

    private static final double EPS = 1e-9;

    @FunctionalInterface
    public interface CleanupListener {
        void onPivot(
            String entering,
            String leaving,
            Tableau tableau
        );
    }

    private final double[][] rows;
    private final double[] costs;
    private final int[] basis;
    private final List<String> variableNames;

    // Construir o tableau inicial da Fase I
    public Tableau(
            StandardFormConverter.StandardForm standard) {

        int m = standard.matrix().length;
        int n = standard.variableNames().size();

        rows = new double[m][n + 1];
        costs = new double[n];
        basis = Arrays.copyOf(standard.basis(), m);
        variableNames = List.copyOf(standard.variableNames());

        for (int i = 0; i < m; i++) {
            System.arraycopy(
                standard.matrix()[i], 0,
                rows[i], 0, n
            );

            rows[i][n] = standard.rhs()[i];
        }

        for (int col : standard.artificialColumns()) {
            costs[col] = -1.0;
        }
    }

    private Tableau(
            double[][] rows,
            double[] costs,
            int[] basis,
            List<String> variableNames) {

        this.rows = rows;
        this.costs = costs;
        this.basis = basis;
        this.variableNames = List.copyOf(variableNames);
    }

    public double[] reducedCosts() {

        int n = costs.length;
        double[] result = new double[n];

        for (int j = 0; j < n; j++) {

            double zj = 0;

            for (int i = 0; i < rows.length; i++) {
                double cb = costs[basis[i]];
                zj += cb * rows[i][j];
            }

            result[j] = costs[j] - zj;
        }

        return result;
    }

    public double objectiveValue() {

        double value = 0;
        int rhsColumn = costs.length;

        for (int i = 0; i < rows.length; i++) {
            value += costs[basis[i]] * rows[i][rhsColumn];
        }

        return value;
    }

    public int chooseEnteringVariable() {

        double[] reduced = reducedCosts();

        for (int j = 0; j < reduced.length; j++) {
            if (reduced[j] > EPS) {
                return j;
            }
        }

        return -1;
    }
    public int chooseLeavingVariable(int entering) {

        int leaving = -1;
        double smallestRatio = Double.POSITIVE_INFINITY;
        int rhsColumn = costs.length;

        for (int i = 0; i < rows.length; i++) {

            double coefficient = rows[i][entering];

            if (coefficient <= EPS) {
                continue;
            }

            double ratio =
                rows[i][rhsColumn] / coefficient;

            if (ratio < -EPS) {
                continue;
            }

            if (leaving == -1
                    || ratio < smallestRatio - EPS
                    || (Math.abs(ratio - smallestRatio) <= EPS
                        && basis[i] < basis[leaving])) {

                smallestRatio = ratio;
                leaving = i;
            }
        }

        return leaving;
    }

    public void pivot(int leaving, int entering) {

        double pivotValue = rows[leaving][entering];
        int columns = rows[leaving].length;

        if (Math.abs(pivotValue) <= EPS) {
            throw new IllegalArgumentException(
                "Elemento pivo invalido."
            );
        }

        for (int j = 0; j < columns; j++) {
            rows[leaving][j] /= pivotValue;
        }

        for (int i = 0; i < rows.length; i++) {

            if (i == leaving) {
                continue;
            }

            double factor = rows[i][entering];

            for (int j = 0; j < columns; j++) {
                rows[i][j] -= factor * rows[leaving][j];
            }
        }

        basis[leaving] = entering;
    }

    private boolean isBasic(int column) {

        for (int basic : basis) {
            if (basic == column) {
                return true;
            }
        }

        return false;
    }

    public Tableau beginPhaseTwo(
            double[] originalObjective,
            List<Integer> artificialColumns) {

        return beginPhaseTwo(
            originalObjective,
            artificialColumns,
            (entering, leaving, tableau) -> {}
        );
    }

    public Tableau beginPhaseTwo(
            double[] originalObjective,
            List<Integer> artificialColumns,
            CleanupListener listener) {

        boolean[] artificial = new boolean[costs.length];

        for (int col : artificialColumns) {
            artificial[col] = true;
        }

        boolean[] keepRows = new boolean[rows.length];
        Arrays.fill(keepRows, true);

        for (int i = 0; i < rows.length; i++) {

            if (!artificial[basis[i]]) {
                continue;
            }

            if (Math.abs(rows[i][costs.length]) > EPS) {
                throw new IllegalStateException(
                    "Artificial basica nao nula na Fase II."
                );
            }

            int entering = -1;

            for (int j = 0; j < costs.length; j++) {

                if (!artificial[j]
                        && !isBasic(j)
                        && Math.abs(rows[i][j]) > EPS) {

                    entering = j;
                    break;
                }
            }

            if (entering != -1) {

                String leavingName =
                    variableNames.get(basis[i]);

                String enteringName =
                    variableNames.get(entering);

                pivot(i, entering);

                listener.onPivot(
                    enteringName,
                    leavingName,
                    this
                );

            } else {

                keepRows[i] = false;
            }
        }

        int[] columnMap = new int[costs.length];
        Arrays.fill(columnMap, -1);

        List<String> newNames = new ArrayList<>();

        for (int j = 0; j < costs.length; j++) {

            if (!artificial[j]) {

                columnMap[j] = newNames.size();
                newNames.add(variableNames.get(j));
            }
        }

        int remainingRows = 0;

        for (boolean keep : keepRows) {
            if (keep) {
                remainingRows++;
            }
        }
        double[][] newRows =
            new double[remainingRows][newNames.size() + 1];

        int[] newBasis = new int[remainingRows];

        int newRow = 0;

        for (int i = 0; i < rows.length; i++) {

            if (!keepRows[i]) {
                continue;
            }

            for (int j = 0; j < costs.length; j++) {

                if (columnMap[j] >= 0) {
                    newRows[newRow][columnMap[j]] =
                        rows[i][j];
                }
            }

            // Copiar lado direito
            newRows[newRow][newNames.size()] =
                rows[i][costs.length];

            // Atualizar indice da variavel basica
            newBasis[newRow] = columnMap[basis[i]];

            if (newBasis[newRow] < 0) {
                throw new IllegalStateException(
                    "Artificial nao removida da base."
                );
            }

            newRow++;
        }

        // Recuperar funcao objetivo original
        double[] newCosts = new double[newNames.size()];

        if (originalObjective.length > newCosts.length) {
            throw new IllegalArgumentException(
                "Dimensao invalida da funcao objetivo."
            );
        }

        for (int j = 0; j < originalObjective.length; j++) {
            newCosts[j] = originalObjective[j];
        }

        return new Tableau(
            newRows,
            newCosts,
            newBasis,
            newNames
        );
    }

    public double[][] rows() {
        return rows;
    }

    public int[] basis() {
        return basis;
    }

    public List<String> variableNames() {
        return variableNames;
    }
}
