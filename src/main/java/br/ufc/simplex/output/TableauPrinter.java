package br.ufc.simplex.output;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;

import br.ufc.simplex.solver.Tableau;

public class TableauPrinter {

    public void print(Tableau tableau) {
        print(tableau, new PrintWriter(System.out, true));
    }

    public String format(Tableau tableau) {
        StringWriter buffer = new StringWriter();
        print(tableau, new PrintWriter(buffer, true));
        return buffer.toString();
    }

    public void print(Tableau tableau, PrintWriter out) {
        double[][] rows = tableau.rows();
        int[] basis = tableau.basis();
        List<String> names = tableau.variableNames();

        out.printf("%-10s", "Base");

        for (String name : names) {
            out.printf("%12s", name);
        }

        out.printf("%12s%n", "b");

        for (int i = 0; i < rows.length; i++) {
            out.printf("%-10s", names.get(basis[i]));

            for (double value : rows[i]) {
                out.printf(Locale.US, "%12.2f", value);
            }

            out.println();
        }

        out.printf("%-10s", "Cj-Zj");

        for (double value : tableau.reducedCosts()) {
            out.printf(Locale.US, "%12.2f", value);
        }

        out.printf(
                Locale.US,
                "%12.2f%n",
                tableau.objectiveValue()
        );
    }
}
