package sk.fmph.uniba.dp.interfaces;

import org.openqa.selenium.WebDriver;

public interface BrowserInterface {

    public abstract WebDriver getBrowser(String browserName);

}
