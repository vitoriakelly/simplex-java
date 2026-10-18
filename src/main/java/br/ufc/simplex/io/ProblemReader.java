
package br.ufc.simplex.io;

import br.ufc.simplex.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProblemReader {

    public LinearProgram read(String filePath)
            throws IOException {

        List<Constraint> constraints = new ArrayList<>();

        try (Scanner scanner =
                 new Scanner(Files.newBufferedReader(Path.of(filePath)))) {

            scanner.useLocale(java.util.Locale.US);

            int n = scanner.nextInt();

            double[] objective = new double[n];

            for (int i = 0; i < n; i++) {
                objective[i] = scanner.nextDouble();
            }

            readConstraints(scanner, n,
                    ConstraintType.LESS_OR_EQUAL, constraints);

            readConstraints(scanner, n,
                    ConstraintType.EQUAL, constraints);

            readConstraints(scanner, n,
                    ConstraintType.GREATER_OR_EQUAL, constraints);

            if (scanner.hasNext()) {
                throw new IllegalArgumentException(
                    "Dados adicionais no final do arquivo."
                );
            }

            return new LinearProgram(n, objective, constraints);
        }
    }

    private void readConstraints(
            Scanner scanner,
            int n,
            ConstraintType type,
            List<Constraint> constraints) {

        int count = scanner.nextInt();

        for (int i = 0; i < count; i++) {

            double[] coefficients = new double[n];

            for (int j = 0; j < n; j++) {
                coefficients[j] = scanner.nextDouble();
            }

            double rhs = scanner.nextDouble();

            constraints.add(
                new Constraint(coefficients, type, rhs)
            );
        }
    }
}
