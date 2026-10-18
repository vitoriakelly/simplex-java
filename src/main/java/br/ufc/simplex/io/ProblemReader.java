
package br.ufc.simplex.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import br.ufc.simplex.model.Constraint;
import br.ufc.simplex.model.ConstraintType;
import br.ufc.simplex.model.LinearProgram;

public class ProblemReader {

    public LinearProgram read(String filePath)
            throws IOException {

        List<Constraint> constraints = new ArrayList<>();

        try (Scanner scanner = new Scanner(
                Files.newBufferedReader(Path.of(filePath)))) {

            scanner.useLocale(Locale.US);

            int n = readInteger(
                scanner,
                "quantidade de variaveis"
            );

            if (n <= 0) {
                throw new IllegalArgumentException(
                    "A quantidade de variaveis deve ser maior que zero."
                );
            }

            double[] objective = new double[n];

            for (int i = 0; i < n; i++) {
                objective[i] = readDouble(
                    scanner,
                    "coeficiente " + (i + 1)
                        + " da funcao objetivo"
                );
            }

            readConstraints(
                scanner,
                n,
                ConstraintType.LESS_OR_EQUAL,
                constraints
            );

            readConstraints(
                scanner,
                n,
                ConstraintType.EQUAL,
                constraints
            );

            readConstraints(
                scanner,
                n,
                ConstraintType.GREATER_OR_EQUAL,
                constraints
            );

            if (scanner.hasNext()) {
                throw new IllegalArgumentException(
                    "Dados adicionais no final do arquivo: "
                        + scanner.next()
                );
            }

            return new LinearProgram(
                n,
                objective,
                constraints
            );
        }
    }

    private void readConstraints(
            Scanner scanner,
            int n,
            ConstraintType type,
            List<Constraint> constraints) {

        String description = switch (type) {
            case LESS_OR_EQUAL -> "menor ou igual";
            case EQUAL -> "igualdade";
            case GREATER_OR_EQUAL -> "maior ou igual";
        };

        int count = readInteger(
            scanner,
            "quantidade de restricoes do tipo " + description
        );

        if (count < 0) {
            throw new IllegalArgumentException(
                "A quantidade de restricoes do tipo "
                    + description
                    + " nao pode ser negativa."
            );
        }

        for (int i = 0; i < count; i++) {

            double[] coefficients = new double[n];

            for (int j = 0; j < n; j++) {

                coefficients[j] = readDouble(
                    scanner,
                    "coeficiente " + (j + 1)
                        + " da restricao " + (i + 1)
                        + " do tipo " + description
                );
            }

            double rhs = readDouble(
                scanner,
                "termo independente da restricao "
                    + (i + 1)
                    + " do tipo " + description
            );

            constraints.add(
                new Constraint(coefficients, type, rhs)
            );
        }
    }

    private int readInteger(
            Scanner scanner,
            String description) {

        if (!scanner.hasNext()) {
            throw new IllegalArgumentException(
                "Arquivo incompleto. Esperado: "
                    + description
            );
        }

        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException(
                "Valor inteiro invalido para "
                    + description
                    + ": " + scanner.next()
            );
        }

        return scanner.nextInt();
    }

    private double readDouble(
            Scanner scanner,
            String description) {

        if (!scanner.hasNext()) {
            throw new IllegalArgumentException(
                "Arquivo incompleto. Esperado: "
                    + description
            );
        }

        if (!scanner.hasNextDouble()) {
            throw new IllegalArgumentException(
                "Valor numerico invalido para "
                    + description
                    + ": " + scanner.next()
            );
        }

        double value = scanner.nextDouble();

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                "Valor nao finito para "
                    + description
            );
        }

        return value;
    }
}
