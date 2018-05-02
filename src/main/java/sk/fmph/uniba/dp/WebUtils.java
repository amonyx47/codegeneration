package sk.fmph.uniba.dp;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.fmph.uniba.dp.errors.ElementNotFoundException;
import sk.fmph.uniba.dp.services.ClientTest;

public class WebUtils {



    public static WebElement findElement(WebDriver driver, String elementName) throws ElementNotFoundException { //will find element by id/name/classname or xpath
        WebElement element = null;
        try {
            return driver.findElement(By.id(elementName));
        } catch (NoSuchElementException ex){
            try {
                return driver.findElement(By.name(elementName));
            } catch(NoSuchElementException exc){
                try{
                    return driver.findElement(By.xpath(elementName));
                } catch(NoSuchElementException exce){
                    try{
                        return driver.findElement(By.className(elementName));
                    } catch(NoSuchElementException excep){
                        throw new ElementNotFoundException(elementName);
                    }
                }
            }
        }
    }

    public static void waitFor(WebDriver driver, WebDriverWait wait, String elementName) throws ElementNotFoundException {
        if(wait == null){
            wait = new WebDriverWait(driver, ClientTest.DEFAULT_WAIT);
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(elementName)));
        } catch (Exception e){
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(elementName)));
            }catch (Exception ex){
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(elementName)));
                }catch (Exception exc){
                    try {
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(elementName)));
                    }catch (Exception exce){
                        throw new ElementNotFoundException(elementName);
                    }
                }
            }
        }
    }
}