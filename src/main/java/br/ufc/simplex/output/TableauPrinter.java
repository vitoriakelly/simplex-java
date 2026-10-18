
package br.ufc.simplex.output;

import java.util.List;
import java.util.Locale;

import br.ufc.simplex.solver.Tableau;

public class TableauPrinter {

    private static final int WIDTH = 12;

    public void print(Tableau tableau) {

        double[][] rows = tableau.rows();
        int[] basis = tableau.basis();
        List<String> names = tableau.variableNames();

        System.out.printf("%-10s", "Base");

        for (String name : names) {
            System.out.printf("%12s", name);
        }

        System.out.printf("%12s%n", "b");

        for (int i = 0; i < rows.length; i++) {

            System.out.printf(
                "%-10s", names.get(basis[i])
            );

            for (double value : rows[i]) {
                System.out.printf(
                    Locale.US, "%12.2f", value
                );
            }

            System.out.println();
        }

        System.out.printf("%-10s", "Cj-Zj");

        for (double value : tableau.reducedCosts()) {
            System.out.printf(
                Locale.US, "%12.2f", value
            );
        }

        System.out.printf(
            Locale.US,
            "%12.2f%n",
            tableau.objectiveValue()
        );
    }
}
