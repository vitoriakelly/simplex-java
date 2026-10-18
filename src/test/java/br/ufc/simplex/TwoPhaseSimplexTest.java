package br.ufc.simplex;

import org.junit.jupiter.api.Test;
import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.solver.TwoPhaseSimplex;
import br.ufc.simplex.solver.SolutionStatus;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.solver.TwoPhaseSimplex;
import br.ufc.simplex.solver.SolutionStatus;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class TwoPhaseSimplexTest {

    private String executar(String arquivo) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer
                = new ByteArrayOutputStream();

        try {
            System.setOut(
                    new PrintStream(
                            buffer, true, StandardCharsets.UTF_8
                    )
            );

            Main.main(new String[]{
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

        var problem
                = reader.read("entradas/exemplo01.txt");

        TwoPhaseSimplex solver = new TwoPhaseSimplex();

        TwoPhaseSimplex.Result result
                = solver.solve(problem);

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

    @Test
    void deveRealizarPivoteamentoDeLimpeza() throws Exception {

        ProblemReader reader = new ProblemReader();

        var problem = reader.read(
                "entradas/exemplo06.txt"
        );

        TwoPhaseSimplex solver = new TwoPhaseSimplex();

        TwoPhaseSimplex.Result result = solver.solve(problem);

        assertEquals(
                SolutionStatus.OPTIMAL,
                result.status()
        );

        assertEquals(0.0, result.solution()[0], 1e-6);
        assertEquals(0.0, result.solution()[1], 1e-6);
        assertEquals(0.0, result.objectiveValue(), 1e-6);

        assertEquals(1, result.phaseOneIterations());
        assertEquals(1, result.cleanupIterations());
        assertEquals(0, result.phaseTwoIterations());
        assertEquals(2, result.totalIterations());
    }

    @Test
    void deveRejeitarQuantidadeDeVariaveisInvalida()
            throws Exception {

        ProblemReader reader = new ProblemReader();

        IllegalArgumentException exception
                = assertThrows(
                        IllegalArgumentException.class,
                        () -> reader.read("entradas/invalido01.txt")
                );

        assertTrue(
                exception.getMessage().contains(
                        "A quantidade de variaveis deve ser maior que zero"
                )
        );
    }

    @Test
    void deveRejeitarArquivoIncompleto()
            throws Exception {

        ProblemReader reader = new ProblemReader();

        IllegalArgumentException exception
                = assertThrows(
                        IllegalArgumentException.class,
                        () -> reader.read("entradas/invalido02.txt")
                );

        assertTrue(
                exception.getMessage().contains("Arquivo incompleto")
        );
    }

    @Test
    void deveRejeitarArquivoComDadosExtras()
            throws Exception {

        ProblemReader reader = new ProblemReader();

        Path arquivo = Files.createTempFile(
                "simplex-invalido-", ".txt"
        );

        try {
            Files.writeString(
                    arquivo,
                    "2\n3 4\n0\n0\n0\nDADO_EXTRA\n"
            );

            IllegalArgumentException exception
                    = assertThrows(
                            IllegalArgumentException.class,
                            () -> reader.read(arquivo.toString())
                    );

            assertTrue(
                    exception.getMessage().contains(
                            "Dados adicionais no final do arquivo"
                    )
            );

        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

}
