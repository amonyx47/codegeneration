package sk.fmph.uniba.dp.services;

import sk.fmph.uniba.dp.interfaces.TestSuiteInterface;
import sk.fmph.uniba.dp.models.TestResponse;

import java.util.Arrays;
import java.util.List;

public class TestSuite implements TestSuiteInterface{


    @Override
    public void showResults(List<TestResponse> testResponses) {
        for(TestResponse testResponse : testResponses){
            System.out.println("Sender: " + testResponse.getSender());
            System.out.println("Message: " + testResponse.getMessage());
            System.out.println("Code: " + testResponse.getCode());
            System.out.println();
        }
    }
}
