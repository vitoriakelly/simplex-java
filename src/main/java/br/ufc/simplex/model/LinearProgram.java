
package br.ufc.simplex.model;

import java.util.List;

public record LinearProgram(
    int variableCount,
    double[] objective,
    List<Constraint> constraints
) {}
