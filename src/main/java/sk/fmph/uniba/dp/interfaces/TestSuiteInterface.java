package sk.fmph.uniba.dp.interfaces;

import sk.fmph.uniba.dp.models.TestResponse;

import java.util.List;

public interface TestSuiteInterface {

    public abstract void showResults(List<TestResponse> clientTests);

}
