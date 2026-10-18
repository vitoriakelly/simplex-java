
package br.ufc.simplex.output;

import br.ufc.simplex.solver.Tableau;
import java.util.List;
import java.util.Locale;

public class TableauPrinter {

    public void print(Tableau tableau) {

        double[][] rows = tableau.rows();
        int[] basis = tableau.basis();
        List<String> names = tableau.variableNames();

        

        System.out.printf("%-8s", "Base");

        for (String name : names) {
            System.out.printf("%12s", name);
        }

        System.out.printf("%10s%n", "b");

        for (int i = 0; i < rows.length; i++) {

            System.out.printf("%-8s", names.get(basis[i]));

            for (double value : rows[i]) {
                System.out.printf(
                    Locale.US, "%10.2f", value
                );
            }

            System.out.println();
        }

        System.out.printf("%-8s", "Cj-Zj");

        for (double value : tableau.reducedCosts()) {
            System.out.printf(
                Locale.US, "%10.2f", value
            );
        }

        System.out.printf(
            Locale.US,
            "%10.2f%n",
            tableau.objectiveValue()
        );
    }
}
