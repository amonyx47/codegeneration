package sk.fmph.uniba.dp;

import sk.fmph.uniba.dp.models.TestResponse;
import sk.fmph.uniba.dp.services.BackendTest;
import sk.fmph.uniba.dp.services.ClientTest;
import sk.fmph.uniba.dp.services.TestSuite;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        ClientTest clientTest1 = new ClientTest("firefox", "verifyNewlyRegistratedUser.test");
        ClientTest clientTest2 = new ClientTest("chrome", "verifyNewlyRegistratedUser.test");
        ClientTest clientTest3 = new ClientTest("phantomjs", "verifyNewlyRegistratedUser.test");
        ClientTest clientTest4 = new ClientTest("firefox", "verifyLink.test");

        List<TestResponse> testResponsesFrontend = new ArrayList<>();
        testResponsesFrontend.addAll(clientTest1.run());
        testResponsesFrontend.addAll(clientTest2.run());
        testResponsesFrontend.addAll(clientTest3.run());
        testResponsesFrontend.addAll(clientTest4.run());

        List<TestResponse> testResponsesBackend = new ArrayList<>();
        BackendTest backendTest = new BackendTest();
        testResponsesBackend.addAll(backendTest.run());

        List<TestResponse> allTests = new ArrayList<>();
        allTests.addAll(testResponsesFrontend);
        allTests.addAll(testResponsesBackend);

        TestSuite suite = new TestSuite();
        suite.showResults(allTests);

    }
}
