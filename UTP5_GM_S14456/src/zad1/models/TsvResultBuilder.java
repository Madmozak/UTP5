package zad1.models;


import java.util.Arrays;
import java.util.List;

public class TsvResultBuilder {

    private static final String YEARS_TEXT = "LATA";
    private static final String PKB_TEXT = "PKB";
    private static final String ZDEKS_TEXT = "ZDEKS";
    private static final String TABULATOR = "\t";
    private static final String NEW_LINE = "\n";

    private List<Integer> years;
    private List<RateModel> rateModels;
    private List<Double> pkb;
    private List<Double> zdeks;

    public TsvResultBuilder(List<Integer> years, List<RateModel> rateModels,  List<Double> pkb,List<Double> zdeks) {
        this.years = years;
        this.rateModels = rateModels;
        this.pkb = pkb;
        this.zdeks = zdeks;
    }

    public String generateTsvResult(){
        StringBuilder builder = new StringBuilder();

        appendYears(builder);
        appendValues(builder);
        appendPkb(builder);
        appendZdeksIfExist(builder);

        return builder.toString();
    }

    private void appendYears(StringBuilder builder) {
        builder.append(YEARS_TEXT);
        years.forEach(year -> builder.append(TABULATOR).append(year));
        builder.append(NEW_LINE);
    }

    private void appendValues(StringBuilder builder) {
        rateModels.forEach(rateModel -> {
            builder.append(rateModel.getLabel());
            Arrays.stream(rateModel.getValues())
                    .forEach(value -> builder.append(TABULATOR).append(value));
            builder.append(NEW_LINE);
        });
    }

    private void appendPkb(StringBuilder builder) {
        builder.append(PKB_TEXT);
        pkb.forEach(pkbValue -> builder.append(TABULATOR).append(pkbValue));
        builder.append(NEW_LINE);
    }

    private void appendZdeksIfExist(StringBuilder builder) {
        if(zdeks != null) {
            builder.append(ZDEKS_TEXT);
            zdeks.forEach(zdeksValue -> builder.append(TABULATOR).append(zdeksValue));
            builder.append(NEW_LINE);
        }
    }

}
