
package br.ufc.simplex;

import org.junit.jupiter.api.Test;
import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.solver.TwoPhaseSimplex;
import br.ufc.simplex.solver.SolutionStatus;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TwoPhaseSimplexTest {

    private String executar(String arquivo) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        try {
            System.setOut(
                new PrintStream(
                    buffer, true, StandardCharsets.UTF_8
                )
            );

            Main.main(new String[] {
                "entradas/" + arquivo
            });

            return buffer.toString(
                StandardCharsets.UTF_8
            );

        } finally {
            System.setOut(original);
        }
    }

    @Test
    void exemplo01SolucaoOtima() {
        String saida = executar("exemplo01.txt");

        assertTrue(saida.contains(
            "STATUS: SOLUCAO OTIMA"
        ));
        assertTrue(saida.contains(
            "x1 = 5.000000"
        ));
        assertTrue(saida.contains(
            "x2 = 5.000000"
        ));
        assertTrue(saida.contains(
            "Z maximo = 35.000000"
        ));
    }

    @Test
    void exemplo02ProblemaInviavel() {
        String saida = executar("exemplo02.txt");

        assertTrue(saida.contains(
            "STATUS: PROBLEMA INVIAVEL"
        ));
        assertTrue(saida.contains(
            "Valor de W: 4.000000"
        ));
    }

    @Test
    void exemplo03ProblemaIlimitado() {
        String saida = executar("exemplo03.txt");

        assertTrue(saida.contains(
            "STATUS: PROBLEMA ILIMITADO"
        ));
    }

    @Test
    void exemplo04SemArtificiais() {
        String saida = executar("exemplo04.txt");

        assertTrue(saida.contains(
            "Total de iteracoes: 0"
        ));
        assertTrue(saida.contains(
            "STATUS: SOLUCAO OTIMA"
        ));
        assertTrue(saida.contains(
            "Z maximo = 36.000000"
        ));
    }

    @Test
    void exemplo05RestricaoRedundante() {
        String saida = executar("exemplo05.txt");

        assertTrue(saida.contains(
            "STATUS: SOLUCAO OTIMA"
        ));
        assertTrue(saida.contains(
            "x1 = 4.000000"
        ));
        assertTrue(saida.contains(
            "x2 = 0.000000"
        ));
        assertTrue(saida.contains(
            "Z maximo = 12.000000"
        ));
    }

@Test
void solucionadorRetornaResultadoCorreto() throws Exception {

    ProblemReader reader = new ProblemReader();

    var problem =
        reader.read("entradas/exemplo01.txt");

    TwoPhaseSimplex solver = new TwoPhaseSimplex();

    TwoPhaseSimplex.Result result =
        solver.solve(problem);

    assertEquals(
        SolutionStatus.OPTIMAL,
        result.status()
    );

    assertEquals(
        5.0,
        result.solution()[0],
        1e-6
    );

    assertEquals(
        5.0,
        result.solution()[1],
        1e-6
    );

    assertEquals(
        35.0,
        result.objectiveValue(),
        1e-6
    );
}

}
