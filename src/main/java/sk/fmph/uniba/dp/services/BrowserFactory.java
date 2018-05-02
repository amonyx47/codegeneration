package sk.fmph.uniba.dp.services;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import sk.fmph.uniba.dp.interfaces.BrowserInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BrowserFactory implements BrowserInterface{

    Properties properties;

    @Override
    public WebDriver getBrowser(String browserName) {

        FileInputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            this.properties =  new Properties();
            this.properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(browserName.equalsIgnoreCase("firefox")){
            System.setProperty("webdriver.gecko.driver", this.properties.getProperty("gecko"));
            return new FirefoxDriver();
        }

        if(browserName.equalsIgnoreCase("chrome")){
            System.setProperty("webdriver.gecko.driver", this.properties.getProperty("chrome"));
            return new ChromeDriver();
        }
        if(browserName.equalsIgnoreCase("phantomjs")){
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true);
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {"--web-security=no", "--ignore-ssl-errors=yes"});
            return new PhantomJSDriver(caps);
        }
        return null;
    }

}
