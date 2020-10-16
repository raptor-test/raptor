package com.raptor;

import com.raptor.util.Utililty;
import net.minidev.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestSteps {

    Utililty utililty;
    public static String code = "";
    public  WebDriver browser;
    private static String authorise_mobile = "MOBILE";
    private static String codeIdentifierId ="";
    private static String profile ="";
    private Logger logger = LogManager.getLogger(TestSteps.class.getName());
    private String defaultTime;
    private String defaultTimeOut;

    public TestSteps() {
        utililty = new Utililty();
    }
    //wait for element to be present
    public WebElement waitForElement(JavascriptExecutor js, String locator) throws InterruptedException {

        Wait<WebDriver> wait = new FluentWait<>(browser)
                .withTimeout(Duration.ofSeconds(Integer.parseInt(defaultTimeOut)))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NullPointerException.class)
                .ignoring(NotFoundException.class)
                .ignoring(JavascriptException.class);
        WebElement element = wait.until(browser -> (WebElement)js.executeScript("return " + locator));
        return element;
    }

    //wait for element to be present
    public List<WebElement> waitForElements(JavascriptExecutor js, String locator) throws InterruptedException {

        Wait<WebDriver> wait = new FluentWait<>(browser)
                .withTimeout(Duration.ofSeconds(Integer.parseInt(defaultTimeOut)))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NullPointerException.class)
                .ignoring(NotFoundException.class)
                .ignoring(JavascriptException.class);
        List<WebElement> elements = wait.until(browser -> (List<WebElement>)js.executeScript("return " + locator));
        return elements;
    }

    // click step
    public void clickStep(JavascriptExecutor js, String locator, String time, String optionalLocator , String optionalStep, WebDriverWait wait) throws InterruptedException {
        logger.info("locator from script: : " + locator);
        logger.info("locator from property: : " + utililty.getEnvironmentProperties(locator));
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        String locatorValue = utililty.getEnvironmentProperties(locator);
        String optionalLocatorValue = utililty.getEnvironmentProperties(optionalLocator);
        WebElement webElement = null;
        Boolean isDisabled = false;
        try {
            webElement = waitForElement(js, locatorValue);
            isDisabled = (Boolean) js.executeScript("return arguments[0].hasAttribute(\"disabled\");", webElement);
            logger.info("first element found: : " + webElement);

            if(webElement == null || isDisabled){
                logger.info("optional locator from property: : " + utililty.getEnvironmentProperties(optionalLocatorValue));
                webElement = webElement = waitForElement(js, optionalLocatorValue);
                isDisabled = (Boolean) js.executeScript("return arguments[0].hasAttribute(\"disabled\");", webElement);
                logger.info("optional element found: : " + webElement);
            }
        } catch (Exception e){

        }

        //js.executeScript will penetrate through all the way deep to your element of concern
        //locator is having jsPath of the element which you can get from chrome inspect and copy jspath
        try {
            if (optionalStep == null || !Boolean.parseBoolean(optionalStep)) {
                js.executeScript("arguments[0].click();", webElement);
            } else if (optionalStep != null && Boolean.parseBoolean(optionalStep) && webElement != null) {
                js.executeScript("arguments[0].click();", webElement);
            }
        } catch(TimeoutException | JavascriptException | NullPointerException ex){
            logger.info("Unable to find element: " + utililty.getEnvironmentProperties(locator) + "or" + utililty.getEnvironmentProperties(optionalLocatorValue));
            throw ex;
        }
    }

    // click step by looking for matching text
    public void clickStepByText(JavascriptExecutor js, String locator, String time, String textToFind) throws InterruptedException {
        //provide list of elements wherein we can find the right element with given text and click
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        String locatorValue = utililty.getEnvironmentProperties(locator);
        List<WebElement> webElements = waitForElements(js, locatorValue);
        WebElement elementFound = null;
        if(webElements != null){
            for (int i = 0; i < webElements.size(); i++) {
                logger.info("text in elements: " + webElements.get(i).getText() + " text to find: "+ textToFind);
                if (webElements.get(i).getText().trim().contains(textToFind.trim())) {
                    elementFound = webElements.get(i);
                    logger.info("element found: : " + elementFound.getText());
                    break;
                }
            }
        }
        try {
            js.executeScript("arguments[0].click();", elementFound);
        } catch(TimeoutException | JavascriptException | NullPointerException ex){
            logger.info("Unable to find element with text: " + textToFind);
            throw ex;
        }

    }

    //input step
    public void inputStep(JavascriptExecutor js, String locator, String time, String inputValue)throws InterruptedException
    {
        String locatorValue = utililty.getEnvironmentProperties(locator);
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        inputValue = "'" + inputValue + "'";
        try {
            WebElement webElement = waitForElement(js, locatorValue);
            webElement.click();
            js.executeScript("arguments[0].focus(); return true", webElement);
            js.executeScript("arguments[0].value=" + inputValue + ";", webElement);
            js.executeScript("arguments[0].dirty=true;return true", webElement);
            js.executeScript("arguments[0].touched=true;return true", webElement);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));return true;", webElement);
            js.executeScript("arguments[0].dirty=false;return true", webElement);
            js.executeScript("arguments[0].blur(); return true", webElement);
        } catch(TimeoutException | JavascriptException | NullPointerException ex){
            logger.info("Unable to find element: " + utililty.getEnvironmentProperties(locator));
            throw ex;
        }

    }

    //input step by sending keys
    public void inputBySendingKeysStep(JavascriptExecutor js, String locator, String time, String inputValue, String type)throws InterruptedException
    {
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        if(("PASSWORD").equals(type)) {
            inputValue = utililty.getDecodedValue(inputValue);
        }
        String locatorValue = utililty.getEnvironmentProperties(locator);
        try {
            WebElement webElement = waitForElement(js, locatorValue);
            webElement.sendKeys(inputValue);
            js.executeScript("arguments[0].focus(); return true", webElement);
            js.executeScript("arguments[0].dirty=true;return true", webElement);
            js.executeScript("arguments[0].touched=true;return true", webElement);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));return true;", webElement);
            js.executeScript("arguments[0].dirty=false;return true", webElement);
            js.executeScript("arguments[0].blur(); return true", webElement);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));return true;", webElement);
            webElement.sendKeys(Keys.TAB);
        } catch(TimeoutException | JavascriptException | NullPointerException ex){
            logger.info("Unable to find element: " + utililty.getEnvironmentProperties(locator));
            throw ex;
        }

    }

    //expect step
    public void expectStep(JavascriptExecutor js, String locator, String time, String expectedText, WebDriverWait wait) throws InterruptedException, NoSuchFieldException {
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        String locatorValue = utililty.getEnvironmentProperties(locator);
        WebElement webElement = waitForElement(js, locatorValue);
        wait.until((ExpectedCondition<Boolean>) js1 -> (webElement).getText().trim().contains(expectedText.trim()));
    }

    //wait step
    public void waitStep(String time) throws InterruptedException {
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
    }

    //scroll down
    public void scroll(JavascriptExecutor js,String height, String time) throws InterruptedException {
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        js.executeScript("window.scrollBy(0," + Integer.parseInt(height) + ")");
    }

    //navigate step
    public void navigateStep(JavascriptExecutor js, String url, String time) throws InterruptedException {
        time = time !=null ? time: defaultTime;
        Thread.sleep(Long.parseLong(time));
        url = utililty.getEnvironmentProperties(url);
        url = "'" + url + "'";
        js.executeScript("window.location = " + url);

    }

    public void authorise(JavascriptExecutor js,JSONObject scriptObject) throws InterruptedException, IOException {
        String timeLapsed = scriptObject.getAsString("time");
        timeLapsed = timeLapsed !=null ? timeLapsed: defaultTime;
        String url = utililty.getEnvironmentProperties(scriptObject.getAsString("url"));
        Long time = Long.parseLong(timeLapsed) != 0 ? Long.parseLong(timeLapsed): 1500;
        String type = scriptObject.getAsString("type");
        String locator = utililty.getEnvironmentProperties(scriptObject.getAsString("locator"));


        url = "'" + url + "'";
        String link = "window.open("+url+",'_blank');";
        js.executeScript(link);
        Thread.sleep(7000);
        ArrayList<String> tabs = new ArrayList<String>(browser.getWindowHandles());
        browser.switchTo().window(tabs.get(1));
        if(authorise_mobile.equalsIgnoreCase(type)) {
            String accessProfile = scriptObject.getAsString("value");
            String mobileInputLocator = utililty.getEnvironmentProperties("mobile.input");
            String mobileSubmitLocator = utililty.getEnvironmentProperties("mobile.submit");
            String mobileApproveLocator = utililty.getEnvironmentProperties("mobile.approve");
            String mobileRejectLocator = utililty.getEnvironmentProperties("mobile.reject");
            if(accessProfile == null || accessProfile.length() == 0) {
                accessProfile = profile;
            }
            Thread.sleep(time);
            inputBySendingKeysStep(js, mobileInputLocator, time.toString(), accessProfile, null);
            clickStep(js,mobileSubmitLocator, time.toString(),null, null, null);
            scroll(js, "2000", "1000");
            clickStep(js,mobileApproveLocator, time.toString(),null, null, null);
            Thread.sleep(time);
            browser.close();
            browser.switchTo().window(tabs.get(0));
            Thread.sleep(time*2);
        } else {
            String codeInputAumaLocator = utililty.getEnvironmentProperties("code.input");
            String codeSubmitLocator= utililty.getEnvironmentProperties("code.submit");
            String codeValueLocator =  utililty.getEnvironmentProperties("code.codeValue"+type);
            String codeIdentifier = scriptObject.getAsString("value");
            if(codeIdentifier == null || codeIdentifier.length() == 0) {
                codeIdentifier = codeIdentifierId;
            }
            Thread.sleep(time);
            inputStep(js, codeInputAumaLocator, time.toString(), codeIdentifier);
            clickStep(js,codeSubmitLocator, time.toString(),null, null, null);
            code = ((WebElement) js.executeScript("return " + codeValueLocator)).getText();
            Thread.sleep(time);
            browser.close();
            browser.switchTo().window(tabs.get(0));
            Thread.sleep(time*2);
            inputBySendingKeysStep(js, locator, time.toString(), code, null);
        }




    }

    //execute test based on action
    public void executeTestBasedOnAction(JavascriptExecutor js, WebDriver browser, JSONObject scriptObject, WebDriverWait wait) {
        try{
            defaultTime = utililty.getEnvironmentProperties("app.defaultActionTime");
            defaultTimeOut = utililty.getEnvironmentProperties("app.defaultTimeOut");
            this.browser = browser;
            logger.info("Script step : " + scriptObject.toString());
            switch(scriptObject.getAsString("action")) {
                case "navigate":
                    navigateStep(js,scriptObject.getAsString("url"), scriptObject.getAsString("time") );
                    break;
                case "scroll":
                    scroll(js,scriptObject.getAsString("value"), scriptObject.getAsString("time") );
                    break;
                case "click":
                    clickStep(js, scriptObject.getAsString("locator"), scriptObject.getAsString("time"), scriptObject.getAsString("optionalLocator"),
                            scriptObject.getAsString("optionalStep"), wait);
                    break;
                case "clickByText":
                    clickStepByText(js, scriptObject.getAsString("locator"), scriptObject.getAsString("time"), scriptObject.getAsString("elementTextToFind"));
                    break;
                case "wait":
                    waitStep(scriptObject.getAsString("time"));
                    break;
                case "input":
                    inputStep(js, scriptObject.getAsString("locator"), scriptObject.getAsString("time"), scriptObject.getAsString("inputValue"));
                    break;
                case "sendKeys":
                    inputBySendingKeysStep(js, scriptObject.getAsString("locator"), scriptObject.getAsString("time"), scriptObject.getAsString("inputValue"),scriptObject.getAsString("type"));
                    break;
                case "expect":
                    expectStep(js, scriptObject.getAsString("locator"), scriptObject.getAsString("time"), scriptObject.getAsString("expectedText"), wait);
                    break;
                case "authorise":
                    authorise(js, scriptObject);
                    break;
                default:
                    // code block
            }
        }catch(InterruptedException | NoSuchFieldException | IOException ex){
            //do stuff
        }
    }

}
