package sk.fmph.uniba.dp.services;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.fmph.uniba.dp.WebUtils;
import sk.fmph.uniba.dp.errors.ElementNotFoundException;
import sk.fmph.uniba.dp.interfaces.TestInterface;
import sk.fmph.uniba.dp.models.TestResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class ClientTest implements TestInterface{

    private static final Logger logger = Logger.getLogger(ClientTest.class);

    private WebDriver driver;
    private WebDriverWait wait;
    public static final int DEFAULT_WAIT = 30;
    private String testFileName;

    public ClientTest(String browserName, String testFileName){
        BasicConfigurator.configure();
        BrowserFactory browserFactory = new BrowserFactory();
        this.testFileName = testFileName;
        this.driver = browserFactory.getBrowser(browserName);
    }

    @Override
    public ArrayList<TestResponse> run() {
        ArrayList<TestResponse> testResponses = new ArrayList<>();
        Path path = Paths.get(testFileName);
        Charset charset = Charset.forName("UTF-8");
        logger.info("Parsing script " + testFileName);
        String delimeter = " ";
        try {
            List<String> lines = Files.readAllLines(path, charset);
            HashMap<String, WebElement> definedElements = new HashMap<>();

            for (String line : lines) {
                logger.info("On line: " + line);
                StringTokenizer tokenizer = new StringTokenizer(line, delimeter, false);
                while (tokenizer.hasMoreTokens()) {
                    String operationToken = tokenizer.nextToken();
                    if (operationToken.equals("page")) {
                        page(tokenizer);
                    }
                    if (operationToken.equals("send")) {
                        send(delimeter, tokenizer);
                    }
                    if (operationToken.equals("click")) {
                        click(tokenizer);
                    }
                    if (operationToken.equals("waitfor")) {
                        waitfor(tokenizer);
                    }
                    if(operationToken.equals("select")){
                        select(tokenizer);
                    }
                    if(operationToken.equals("def")){
                        def(definedElements, tokenizer);
                    }
                    if(operationToken.equals("verifyText")){
                        testResponses.add(verifyText(delimeter, definedElements, tokenizer));
                    }
                    if(operationToken.equals("verifyElement")){
                        testResponses.add(verifyElement(tokenizer));
                    }
                }
            }
        } catch (IOException | ElementNotFoundException e) {
            logger.error(e);
        } finally {
            this.driver.close();
        }
        return testResponses;
    }

    private TestResponse verifyElement(StringTokenizer tokenizer) {
        logger.info("Parsing verifyElement command");
        WebElement element;
        String elementName = tokenizer.nextToken();
        try {
            element = WebUtils.findElement(this.driver, elementName);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        return new TestResponse("Element " + elementName + " found", TestResponse.SUCCESS_CODE, ClientTest.class.getName());
    }

    private TestResponse verifyText(String delimeter, HashMap<String, WebElement> definedElements, StringTokenizer tokenizer) {
        logger.info("Parsing verifyText command");
        String textToFind = "";
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if(token.equals("in")){
                break;
            } else {
                textToFind += token + delimeter;
            }
        }
        textToFind = textToFind.trim();
        String elementToCompareTo = tokenizer.nextToken();
        String elementText = definedElements.get(elementToCompareTo).getText();
        if(elementText.equals(textToFind)){
            return new TestResponse("Element with text " + elementText + " successfully found", TestResponse.SUCCESS_CODE, ClientTest.class.getName());
            //logger.info("Success: found " + textToFind + " in element text " + elementText);
        } else {
            return new TestResponse("Element with text " + elementText + " could't be found", TestResponse.FAIL_CODE, ClientTest.class.getName());
            //logger.warn("Couldn't find "+ textToFind + " in element text " + elementText);
        }
    }

    private void def(HashMap<String, WebElement> definedElements, StringTokenizer tokenizer) {
        logger.info("Parsing def command");
        String newElementName = tokenizer.nextToken();
        tokenizer.nextToken(); //skipping "="
        String elementDefinition = tokenizer.nextToken();
        WebElement element = null;
        try {
            element = WebUtils.findElement(this.driver, elementDefinition);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        definedElements.put(newElementName, element);
    }

    private void select(StringTokenizer tokenizer) {
        logger.info("Parsing select command");
        String calendarElementName = tokenizer.nextToken();
        String valueOfCalendarInput = tokenizer.nextToken();
        WebElement calendarElement = null;
        try {
            calendarElement = WebUtils.findElement(this.driver, calendarElementName);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        try {
            new Select(calendarElement).selectByValue(String.valueOf(valueOfCalendarInput));
        }catch (Exception e){
            logger.warn("Couldn't select " + calendarElementName + " by value " + valueOfCalendarInput);
        }
        try {
            new Select(calendarElement).selectByVisibleText(valueOfCalendarInput);
        } catch (Exception e){
            logger.warn("Couldn't select " + calendarElement + " by visible text " + valueOfCalendarInput);
        }
    }

    private void waitfor(StringTokenizer tokenizer) throws ElementNotFoundException {
        logger.info("Parsing waitfor command");
        String whatToWaitFor = tokenizer.nextToken();
        if(!whatToWaitFor.isEmpty() && org.apache.commons.lang3.StringUtils.isNumeric(whatToWaitFor)){
            this.wait = new WebDriverWait(this.driver, Integer.valueOf(tokenizer.nextToken()));
        }else {
            WebUtils.waitFor(this.driver, this.wait, whatToWaitFor);
        }
    }

    private void click(StringTokenizer tokenizer) {
        logger.info("Parsing click command");
        String whatToClick = tokenizer.nextToken();
        WebElement element = null;
        try {
            element = WebUtils.findElement(this.driver, whatToClick);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        element.click();
    }

    private void page(StringTokenizer tokenizer) throws MalformedURLException {
        logger.info("Parsing page command");
        String urlToBe = tokenizer.nextToken();
        if(!urlToBe.contains("http")){
            urlToBe = "http://" + urlToBe;
        }
        this.driver.get(String.valueOf(new URL(urlToBe)));
    }

    private void send(String delimeter, StringTokenizer tokenizer) {
        logger.info("Parsing send command");
        String textToSend = "";
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if(token.equals("to")){
                break;
            } else {
                textToSend += token + delimeter;
            }
        }
        textToSend = textToSend.trim();
        String where = tokenizer.nextToken();
        WebElement element = null;
        try {
            element = WebUtils.findElement(this.driver, where);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        element.sendKeys(textToSend);
    }


}
