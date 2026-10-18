package br.ufc.simplex.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.ufc.simplex.io.ProblemReader;
import br.ufc.simplex.model.LinearProgram;
import br.ufc.simplex.output.ProblemFormatter;
import br.ufc.simplex.output.SolutionReporter;
import br.ufc.simplex.output.TableauPrinter;
import br.ufc.simplex.solver.SolutionStatus;
import br.ufc.simplex.solver.TwoPhaseSimplex;

public class SimplexFrame extends JFrame {

    private static final Color BG = new Color(248, 249, 251);
    private static final Color PANEL = Color.WHITE;
    private static final Color BORDER = new Color(220, 224, 230);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(90, 98, 110);
    private static final Color ACCENT = new Color(15, 98, 120);
    private static final Color OPTIMAL = new Color(20, 120, 70);
    private static final Color INFEASIBLE = new Color(160, 40, 40);
    private static final Color UNBOUNDED = new Color(160, 100, 20);

    private final JTextField pathField = new JTextField();
    private final JComboBox<String> examplesBox = new JComboBox<>();
    private final JButton browseButton = new JButton("Abrir...");
    private final JButton solveButton = new JButton("Resolver");

    private final JTextArea problemArea = createMonoArea();
    private final JTextArea stepsArea = createMonoArea();
    private final JTextArea resultArea = createMonoArea();
    private final JLabel statusLabel = new JLabel("Selecione um arquivo e clique em Resolver.");
    private final JLabel statusHint = new JLabel(" ");

    private final ProblemReader reader = new ProblemReader();
    private final TableauPrinter printer = new TableauPrinter();

    public SimplexFrame() {
        super("Simplex em Duas Fases");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadExamples();
        wireActions();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
                // Mantem o look padrao se o sistema nao estiver disponivel.
            }

            SimplexFrame frame = new SimplexFrame();
            frame.setVisible(true);
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel title = new JLabel("Metodo Simplex em Duas Fases");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Leia o problema em notacao matematica, acompanhe as fases e confira o resultado com interpretacao."
        );
        subtitle.setForeground(MUTED);

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 8);
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        pathField.setEditable(false);
        pathField.setColumns(28);

        c.gridx = 0;
        c.weightx = 0;
        controls.add(new JLabel("Arquivo:"), c);

        c.gridx = 1;
        c.weightx = 1;
        controls.add(pathField, c);

        c.gridx = 2;
        c.weightx = 0;
        controls.add(browseButton, c);

        c.gridx = 3;
        controls.add(new JLabel("Exemplos:"), c);

        c.gridx = 4;
        controls.add(examplesBox, c);

        solveButton.setBackground(ACCENT);
        solveButton.setForeground(Color.WHITE);
        solveButton.setOpaque(true);
        solveButton.setBorderPainted(false);
        solveButton.setFocusPainted(false);

        c.gridx = 5;
        c.insets = new Insets(0, 0, 0, 0);
        controls.add(solveButton, c);

        header.add(titles, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                section("1. Problema", "Formulacao matematica carregada do arquivo.", problemArea),
                section("2. Passo a passo", "Tableaux e transicoes entre as fases.", stepsArea)
        );
        split.setResizeWeight(0.38);
        split.setBorder(null);
        split.setContinuousLayout(true);

        JPanel resultPanel = section(
                "3. Resultado",
                "Status, valores das variaveis e interpretacao.",
                resultArea
        );
        resultPanel.setPreferredSize(new Dimension(100, 220));

        JPanel statusBar = new JPanel(new BorderLayout(0, 2));
        statusBar.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(8, 4, 0, 4));

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        statusLabel.setForeground(ACCENT);
        statusHint.setForeground(MUTED);

        statusBar.add(statusLabel, BorderLayout.NORTH);
        statusBar.add(statusHint, BorderLayout.SOUTH);

        JPanel south = new JPanel(new BorderLayout(0, 0));
        south.setOpaque(false);
        south.add(resultPanel, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);

        body.add(split, BorderLayout.CENTER);
        body.add(south, BorderLayout.SOUTH);
        return body;
    }

    private JPanel section(String title, String hint, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        heading.setForeground(TEXT);

        JLabel description = new JLabel(hint);
        description.setForeground(MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(heading, BorderLayout.NORTH);
        header.add(description, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static JTextArea createMonoArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(TEXT);
        area.setBackground(new Color(252, 253, 255));
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        area.setLineWrap(false);
        return area;
    }

    private void loadExamples() {
        examplesBox.addItem("— escolher —");

        File folder = Path.of("entradas").toFile();
        if (!folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles(
                (dir, name) -> name.endsWith(".txt") && name.startsWith("exemplo")
        );

        if (files == null) {
            return;
        }

        Arrays.sort(files);
        for (File file : files) {
            examplesBox.addItem(file.getName());
        }

        if (files.length > 0) {
            examplesBox.setSelectedItem(files[0].getName());
            pathField.setText(new File(folder, files[0].getName()).getPath());
            previewProblem(pathField.getText());
        }
    }

    private void wireActions() {
        browseButton.addActionListener(e -> chooseFile());
        solveButton.addActionListener(e -> solve());
        examplesBox.addActionListener(e -> {
            Object selected = examplesBox.getSelectedItem();
            if (selected == null || selected.toString().startsWith("—")) {
                return;
            }
            String path = Path.of("entradas", selected.toString()).toString();
            pathField.setText(path);
            previewProblem(path);
        });
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(Path.of("entradas").toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de texto", "txt"));

        int option = chooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        pathField.setText(file.getAbsolutePath());
        examplesBox.setSelectedIndex(0);
        previewProblem(file.getAbsolutePath());
    }

    private void previewProblem(String path) {
        try {
            LinearProgram problem = reader.read(path);
            problemArea.setText(ProblemFormatter.format(problem));
            problemArea.setCaretPosition(0);
            stepsArea.setText("Clique em Resolver para executar as Fases I e II.");
            resultArea.setText("");
            setStatus(
                    "Problema carregado. Pronto para resolver.",
                    "A Fase I verifica viabilidade; a Fase II otimiza Z.",
                    ACCENT
            );
        } catch (Exception ex) {
            problemArea.setText("");
            stepsArea.setText("");
            resultArea.setText("");
            setStatus(
                    "Arquivo invalido",
                    ex.getMessage(),
                    INFEASIBLE
            );
        }
    }

    private void solve() {
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um arquivo de entrada.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        solveButton.setEnabled(false);
        browseButton.setEnabled(false);
        examplesBox.setEnabled(false);
        setStatus("Resolvendo...", "Executando pivoteamentos do Simplex.", ACCENT);

        new SwingWorker<Void, String>() {
            private LinearProgram problem;
            private TwoPhaseSimplex.Result result;
            private final StringBuilder steps = new StringBuilder();

            @Override
            protected Void doInBackground() throws Exception {
                problem = reader.read(path);

                TwoPhaseSimplex solver = new TwoPhaseSimplex();
                result = solver.solve(problem, (phase, iteration, tableau) -> {
                    steps.append(phaseExplanation(phase, iteration));
                    steps.append('\n');
                    steps.append(printer.format(tableau));
                    steps.append('\n');
                });

                return null;
            }

            @Override
            protected void done() {
                solveButton.setEnabled(true);
                browseButton.setEnabled(true);
                examplesBox.setEnabled(true);

                try {
                    get();
                    problemArea.setText(ProblemFormatter.format(problem));
                    problemArea.setCaretPosition(0);
                    stepsArea.setText(steps.toString());
                    stepsArea.setCaretPosition(0);
                    resultArea.setText(SolutionReporter.format(result));
                    resultArea.setCaretPosition(0);
                    applyResultStatus(result.status());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    resultArea.setText("Erro: " + cause.getMessage());
                    setStatus("Falha na execucao", cause.getMessage(), INFEASIBLE);
                }
            }
        }.execute();
    }

    private static String phaseExplanation(String phase, int iteration) {
        return switch (phase) {
            case "FASE I" -> iteration == 0
                    ? "=== FASE I (viabilidade) — tableau inicial ==="
                    : "=== FASE I — iteracao " + iteration + " ===";
            case "TRANSICAO" ->
                    "=== TRANSICAO — limpeza de artificial #" + iteration + " ===";
            case "FASE II" -> iteration == 0
                    ? "=== FASE II (otimizacao) — tableau inicial ==="
                    : "=== FASE II — iteracao " + iteration + " ===";
            default -> "=== " + phase + " - ITERACAO " + iteration + " ===";
        };
    }

    private void applyResultStatus(SolutionStatus status) {
        Color color = switch (status) {
            case OPTIMAL -> OPTIMAL;
            case INFEASIBLE -> INFEASIBLE;
            case UNBOUNDED -> UNBOUNDED;
        };

        setStatus(
                SolutionReporter.statusLabel(status),
                SolutionReporter.statusHint(status),
                color
        );
    }

    private void setStatus(String title, String hint, Color color) {
        statusLabel.setText(title);
        statusLabel.setForeground(color);
        statusHint.setText(hint == null ? " " : hint);
    }
}
