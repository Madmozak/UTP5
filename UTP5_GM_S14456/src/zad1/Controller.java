package zad1;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import zad1.models.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Controller {

    private static final String YEARS_LABEL = "LATA";
    private static final String REPLACE_CHAR = ".";
    private static final String REPLACEMENT = ",";
    private static final String RATE_LABEL_PREFIX = "tw";
    private static final int LINES_TO_SKIP = 1;
    private static final String YEARS_TEXT = "LL";
    private static final String EXPORT_TEXT = "EKS";
    private static final String PKB_TEXT = "PKB";
    private static final String EXPORT_RATE = "ZDEKS";

    private String modelName;
    private Model1 model1;
    private ReflectionUtils reflectionUtils;

    private List<Integer> years;
    private List<RateModel> rateModels;
    private List<Double> zdeks;

    public Controller(String modelName) {
        this.modelName = modelName;
        model1 = new Model1();
    }

    public Controller readDataFrom(String fName) {
        try {
            List<String> lines = readAllLines(fName);
            years = getAllYears(lines);
            rateModels = getRateModels(lines, years.size());

            reflectionUtils = new ReflectionUtils(model1, years, rateModels);
            reflectionUtils.injectValues();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public Controller runModel() {
        model1.run();
        return this;
    }

    public void runScriptFromFile(String fname) {
        try {
            GroovyShell shell = prepareShell();
            evaluateScriptFromFile(shell, fname);
            saveScriptResult(shell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runScript(String script) {
        try {
            GroovyShell shell = prepareShell();
            evaluateScriptFromString(shell, script);
            saveScriptResult(shell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GroovyShell prepareShell() throws Exception {
        Binding binding = new Binding();
        binding.setVariable(YEARS_TEXT, years.size());
        binding.setVariable(EXPORT_TEXT, reflectionUtils.retrieveArrayFieldFromModel(EXPORT_TEXT));
        binding.setVariable(PKB_TEXT, reflectionUtils.retrieveArrayFieldFromModel(PKB_TEXT));

        return new GroovyShell(binding);
    }

    private void evaluateScriptFromFile(GroovyShell shell, String filePath) throws IOException {
        File scriptFile = new File(filePath);
        shell.evaluate(scriptFile);
    }

    private void evaluateScriptFromString(GroovyShell shell, String script) {
        shell.evaluate(script);
    }

    private void saveScriptResult(GroovyShell shell) {
        double[] zdeks = (double[]) shell.getProperty(EXPORT_RATE);
        this.zdeks = DoubleStream.of(zdeks)
                .mapToObj(Double::valueOf)
                .collect(Collectors.toList());
    }

    public String getResultsAsTsv() {
        try {
            double[] pkb = reflectionUtils.retrieveArrayFieldFromModel(PKB_TEXT);
            List<Double> pkbValues = DoubleStream.of(pkb)
                    .mapToObj(Double::valueOf)
                    .collect(Collectors.toList());

            TsvResultBuilder tsvResultBuilder = new TsvResultBuilder(years, rateModels, pkbValues, zdeks);
            return tsvResultBuilder.generateTsvResult();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate Tsv result", e);
        }

    }

    private List<String> readAllLines(String fName) throws IOException {
        Path filePath = Paths.get(fName);
        return Files.readAllLines(filePath);
    }

    private List<Integer> getAllYears(List<String> lines) {
        String firstLine = lines.iterator().next();
        Scanner scanner = new Scanner(firstLine);
        scanner.skip(YEARS_LABEL);

        List<Integer> years = new ArrayList<>();
        while (scanner.hasNextInt()) {
            years.add(scanner.nextInt());
        }
        return years;
    }

    private List<RateModel> getRateModels(List<String> lines, int years) {
        return lines.stream()
                .skip(LINES_TO_SKIP)
                .map(line -> line.replace(REPLACE_CHAR, REPLACEMENT))
                .map(line -> mapToRateModel(line, years))
                .collect(Collectors.toList());
    }

    private RateModel mapToRateModel(String line, int years) {
        Scanner scanner = new Scanner(line);
        String label = scanner.next();

        List<Double> values = new ArrayList<>();
        while (scanner.hasNextDouble()) {
            values.add(scanner.nextDouble());
        }

        if (values.size() < years) {
            if (label.contains(RATE_LABEL_PREFIX)) {
                extendValues(values, years);
            } else {
                extendWithZero(values, years);
            }
        }

        double[] doubleValues = values.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        return new RateModel(label, doubleValues);
    }

    private void extendValues(List<Double> values, int years) {
        double lastValue = values.get(values.size() - 1);
        int howManyToAdd = years - values.size();
        for (int i = 0; i < howManyToAdd; i++) {
            values.add(lastValue);
        }
    }

    private void extendWithZero(List<Double> values, int years) {
        int howManyToAdd = years - values.size();
        for (int i = 0; i < howManyToAdd; i++) {
            values.add(0.0);
        }
    }

}