
package br.ufc.simplex.model;

public record Constraint(
    double[] coefficients,
    ConstraintType type,
    double rightHandSide
) {}
