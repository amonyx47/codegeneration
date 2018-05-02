package sk.fmph.uniba.dp.services;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sk.fmph.uniba.dp.interfaces.TestInterface;
import sk.fmph.uniba.dp.models.TestResponse;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class BackendTest implements TestInterface {

    private static final Logger logger = Logger.getLogger(BackendTest.class);
    private final String COMMAND_BASH = "#!/bin/bash";
    private final String COMMAND_MAVEN_REPORT = "mvn surefire-report:report";
    private final String FILE_MAVEN_REPORT = "target/site/surefire-report.html";
    private final String FILE_MAVEN_TEST_REPORT = "mavenScript";
    private final String FOLDER_MAVEN_TEST_REPORT = "mavenTestResults";
    private final String delimeter = ";";
    private Properties properties;
    private String[] paths;
    private String[] scriptNames;


    public BackendTest() {
        this.paths = getPathsToProjects();
    }

    @Override
    public ArrayList<TestResponse> run() {
        this.runScript(FILE_MAVEN_TEST_REPORT);
        this.collectResults();
        ArrayList<TestResponse> testResponses = this.parseResults();
        return testResponses;
    }

    private String[] getPathsToProjects() {
        FileInputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            this.properties = new Properties();
            this.properties.load(input);
        } catch (IOException e) {
            logger.error("Damaged config.properties file!");
        }
        return this.properties.getProperty("maven").split(this.delimeter);
    }

    private String[] buildScript(String scriptName) {
        this.scriptNames = new String[this.paths.length];
        for (int i = 0; i < this.paths.length; i++) {
            try {
                Files.createDirectories(Paths.get(System.getProperty("user.dir")));
                List<String> lines = Arrays.asList(COMMAND_BASH, "cd " + this.paths[i], COMMAND_MAVEN_REPORT);
                this.scriptNames[i] = scriptName + i + ".sh";
                Path file = Paths.get(this.scriptNames[i]);
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                logger.error("Cannot create directory/file for backend maven tests!");
            }
        }
        return this.scriptNames;
    }

    private void runScript(String scriptName) {
        String[] scriptNames = buildScript(scriptName);
        for (int i = 0; i < scriptNames.length; i++) {
            final Process p, r;
            try {
                p = Runtime.getRuntime().exec("chmod 777 " + scriptNames[i]);
                p.waitFor();
                r = Runtime.getRuntime().exec("./" + scriptNames[i]);


                new Thread(() -> {
                    BufferedReader input = new BufferedReader(new InputStreamReader(r.getInputStream()));
                    String line;

                    try {
                        while ((line = input.readLine()) != null)
                            System.out.println(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                r.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void collectResults() {
        try {
            Files.createDirectories(Paths.get(FOLDER_MAVEN_TEST_REPORT));
            Process p = Runtime.getRuntime().exec("chmod 777 " + FOLDER_MAVEN_TEST_REPORT);
            p.waitFor();
            for (int i = 0; i < this.paths.length; i++) {
                File sourceFile = new File(this.paths[i] + FILE_MAVEN_REPORT);
                File destFile = new File(FOLDER_MAVEN_TEST_REPORT + File.separator + scriptNames[i].replace(".sh", "") + "_result.html");
                copyFile(sourceFile, destFile);
            }
        } catch (IOException e) {
            logger.warn("Couldn't copy result report file " + FILE_MAVEN_REPORT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (FileNotFoundException e) {
            logger.warn("File" + source + "/" + destination + "couldn't be found!");
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private ArrayList<TestResponse> parseResults() {
        ArrayList<TestResponse> testResponses = new ArrayList<>();
        File folder = new File(FOLDER_MAVEN_TEST_REPORT);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    String resultInSingleString = readFile(file.getPath(), Charset.forName("UTF-8"));
                    Document document = Jsoup.parse(resultInSingleString);
                    Element resultsTable = document.select(".bodytable").get(2); //always third table in report
                    Elements rows = resultsTable.select("tr:not(:first-child)");

                    for (Element row : rows) {
                        String sender = row.select("td:nth-child(2)").get(0).text();
                        int overallNumOfTests = Integer.valueOf(row.select("td:nth-child(3)").get(0).text());
                        int failedTests = Integer.valueOf(row.select("td:nth-child(4)").get(0).text());
                        int skippedTests = Integer.valueOf(row.select("td:nth-child(5)").get(0).text());
                        testResponses.add(new TestResponse("Overall tests: " + overallNumOfTests + " failed Tests: " + failedTests + " skipped Tests: " + skippedTests, TestResponse.SUCCESS_CODE, sender));
                        /*System.out.println("sender: " + sender);
                        System.out.println("overall: " + overallNumOfTests);
                        System.out.println("failed: " + failedTests);
                        System.out.println("skipped: " + skippedTests);*/
                    }
                } catch (IOException e) {
                    logger.error("Cannot read file");
                } catch (IndexOutOfBoundsException indexOutEx) {
                    logger.warn("Index out of bounds for document " + file.getName());
                }
            }
            file.delete();
        }
        return testResponses;
    }

    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
