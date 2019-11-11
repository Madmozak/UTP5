package zad1;


import java.lang.reflect.Field;
import java.util.List;
import zad1.models.*;
import zad1.models.Model1;

public class ReflectionUtils {

    private static final String YEARS_FIELD = "LL";

    private Model1 model1;
    private List<Integer> years;
    private List<RateModel> rateModels;

    public ReflectionUtils(Model1 model1, List<Integer> years, List<RateModel> rateModels) throws Exception {
        this.model1 = model1;
        this.years = years;
        this.rateModels = rateModels;
    }

    public void injectValues() throws Exception {
        injectYearsToModel(years);
        injectRateModelsToModel(rateModels);
    }

    public double[] retrieveArrayFieldFromModel(String fieldName) throws Exception {
        Class<? extends Model1> modelClass = model1.getClass();
        Field pkbField = modelClass.getDeclaredField(fieldName);
        pkbField.setAccessible(true);

        if (pkbField.getAnnotation(Bind.class) == null) {
            throw new Exception("PKB field hasn't got a @Bind annotation");
        }
        return (double[]) pkbField.get(model1);
    }

    private void injectYearsToModel(List<Integer> years) throws Exception {
        Class<? extends Model1> modelClass = model1.getClass();
        Field yearsField = modelClass.getDeclaredField(YEARS_FIELD);
        yearsField.setAccessible(true);

        if (yearsField.getAnnotation(Bind.class) != null) {
            yearsField.setInt(model1, years.size());
        }
    }

    private void injectRateModelsToModel(List<RateModel> rateModels) {
        rateModels.forEach(rateModel -> {
            try {
                injectValuesToField(rateModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void injectValuesToField(RateModel rateModel) throws Exception {
        Class<? extends Model1> modelClass = model1.getClass();
        Field yearsField = modelClass.getDeclaredField(rateModel.getLabel());
        yearsField.setAccessible(true);

        if (yearsField.getAnnotation(Bind.class) != null) {
            yearsField.set(model1, rateModel.getValues());
        }

    }

}