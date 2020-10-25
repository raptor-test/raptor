package com.raptor;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.raptor.util.JsonTestCase;
import com.raptor.util.ReadJsonTestScripts;
import com.raptor.util.Utililty;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.minidev.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Service
public class TestExecution {

    public TestSteps testSteps;

    Utililty utililty;

    //builds a new report using the html template
    public ExtentHtmlReporter htmlReporter;

    public ExtentReports extent;
    //helps to generate the logs in test report.
    public ExtentTest test;
    public  WebDriver browser;
    public  ChromeOptions chromeOptions = new ChromeOptions();
    public String operatingSystem = "";
    public String browserType = "";
    private JsonTestCase testScript = null;

    public TestExecution() {
        utililty = new Utililty();
        testSteps = new TestSteps();
    }



    public WebDriver createBrowser(String jsonPath) throws MalformedURLException {
        try {
            String fullJsonPath = utililty.getEnvironmentProperties("app.relativeTestscriptPath") + jsonPath;
            testScript = ReadJsonTestScripts.readTestScript(fullJsonPath);
            String browserType = StringUtils.isEmpty(testScript.getTestCaseConfig().getBrowser()) ? "Chrome" : testScript.getTestCaseConfig().getBrowser();
            extent.setSystemInfo("Browser", browserType + " - " + testScript.getTestCaseConfig().getTestName());
            Boolean proxyRequired = testScript.getTestCaseConfig().getProxy();
            return createBrowserOption(browserType, proxyRequired);
        } catch (Exception ex) {
            return null;
        }
    }

    public WebDriver createBrowserOption(String browserType, Boolean proxyRequired) throws MalformedURLException {
        Boolean isGridDisabled = Boolean.parseBoolean(utililty.getEnvironmentProperties("app.isGridDisabled"));
        String baseUrl = utililty.getEnvironmentProperties("app.baseUrl");
        String proxyUrl = utililty.getEnvironmentProperties("app.proxyUrl");
        String nodeUrl = utililty.getEnvironmentProperties("app.gridNodeurl");

        if(isGridDisabled) {
            switch (browserType.toLowerCase()) {
                case "chrome":
                    String chromePathLocal = utililty.getEnvironmentProperties("chromeDriverPath");
                    if(chromePathLocal != null && !chromePathLocal.isEmpty() ) {
                        System.setProperty("webdriver.chrome.driver",
                                System.getProperty("user.dir") + "//" + chromePathLocal);
                    } else {
                        WebDriverManager.chromedriver().setup();
                    }
                    chromeOptions.setHeadless(true);
                    chromeOptions.addArguments("start-maximized"); // open Browser in maximized mode
                    chromeOptions.addArguments("disable-infobars"); // disabling infobars
                    chromeOptions.addArguments("--disable-extensions"); // disabling extensions
                    chromeOptions.addArguments("--disable-gpu"); // applicable to windows os only
                    chromeOptions.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
                    chromeOptions.addArguments("--no-sandbox"); // Bypass OS security model
                    chromeOptions.addArguments("--disable-blink-features='BlockCredentialedSubresources'");
                    chromeOptions.setHeadless(false);
                    DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                    LoggingPreferences loggingprefs = new LoggingPreferences();
                    loggingprefs.enable(LogType.BROWSER, Level.ALL);
                    capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
                    capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
                    //capabilities.setVersion("81");
                    chromeOptions.merge(capabilities);
                    browser = new ChromeDriver(chromeOptions);

                    browser.get(baseUrl);
                    break;
                case "edge":
                    String edgePathLocal = utililty.getEnvironmentProperties("edgeDriverPath");
                    if(edgePathLocal != null && !edgePathLocal.isEmpty() ) {
                        System.setProperty("webdriver.chrome.driver",
                                System.getProperty("user.dir") + "//" + edgePathLocal);
                    } else {
                        WebDriverManager.edgedriver().setup();
                    }
                    EdgeOptions edgeOptions = new EdgeOptions();
                    LoggingPreferences edgeLogprefs = new LoggingPreferences();
                    edgeOptions.setCapability(CapabilityType.LOGGING_PREFS, edgeLogprefs);
                    edgeLogprefs.enable(LogType.PERFORMANCE, Level.ALL);
                    edgeOptions.setCapability("goog:loggingPrefs", edgeLogprefs);
                    browser = new EdgeDriver(edgeOptions);
                    //browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    break;
                case "ie":
                    String isPathLocal = utililty.getEnvironmentProperties("ieDriverPath");
                    if(isPathLocal != null && !isPathLocal.isEmpty() ) {
                        System.setProperty("webdriver.chrome.driver",
                                System.getProperty("user.dir") + "//" + isPathLocal);
                    } else {
                        WebDriverManager.iedriver().setup();
                    }
                    InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                    LoggingPreferences ieLogprefs = new LoggingPreferences();
                    ieLogprefs.enable(LogType.PERFORMANCE, Level.ALL);
                    ieOptions.setCapability(CapabilityType.LOGGING_PREFS, ieLogprefs);
                    ieOptions.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
                    ieOptions.setCapability("goog:loggingPrefs", ieLogprefs);
                    DesiredCapabilities capabilityIE = DesiredCapabilities.internetExplorer();
                    capabilityIE.setPlatform(Platform.WINDOWS);
                    capabilityIE.setBrowserName(BrowserType.EDGE);
                    capabilityIE.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    capabilityIE.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
                    capabilityIE.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT , true);
                    ieOptions.merge(capabilityIE);

                    browser = new InternetExplorerDriver(ieOptions);
                    break;
            }
            browser.manage().timeouts().pageLoadTimeout(80, TimeUnit.SECONDS);
        } else {
            switch (browserType.toLowerCase()) {
                case "chrome":
                    DesiredCapabilities capabilities =  DesiredCapabilities.chrome();
                    LoggingPreferences loggingprefs = new LoggingPreferences();
                    loggingprefs.enable(LogType.BROWSER, Level.ALL);
                    capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
                    capabilities.setVersion("81");
                    capabilities.setPlatform(Platform.WINDOWS);
                    capabilities.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors"));
                    capabilities.setCapability("acceptInsecureCerts", true);
                    if(proxyRequired && proxyUrl != null) {
                        Proxy proxy = new Proxy();
                        proxy.setSslProxy(proxyUrl);
                        proxy.setFtpProxy(proxyUrl);
                        proxy.setNoProxy("127.0.0.1,localhost");
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        capabilities.setCapability(CapabilityType.PROXY, proxy);
                    }

                    capabilities.setBrowserName("chrome");
                    capabilities.setCapability("browser", "chrome");
                    //seting the required capabilities
                    ChromeOptions options = new ChromeOptions();
                    options.merge(capabilities);
                    browser = new RemoteWebDriver(new URL(nodeUrl), options);
                    break;
                case "edge":
                    EdgeOptions edgeOptions = new EdgeOptions();
                    LoggingPreferences edgeLogprefs = new LoggingPreferences();
                    edgeOptions.setCapability(CapabilityType.LOGGING_PREFS, edgeLogprefs);
                    edgeLogprefs.enable(LogType.PERFORMANCE, Level.ALL);
                    edgeOptions.setCapability("goog:loggingPrefs", edgeLogprefs);
                    DesiredCapabilities capability = DesiredCapabilities.edge();
                    capability.setPlatform(Platform.WINDOWS);
                    capability.setBrowserName(BrowserType.EDGE);
                    capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
                    capability.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT , true);
                    edgeOptions.merge(capability);

                    if(proxyRequired && proxyUrl != null) {
                        Proxy proxy = new Proxy();
                        proxy.setSslProxy(proxyUrl);
                        proxy.setFtpProxy(proxyUrl);
                        proxy.setNoProxy("127.0.0.1,localhost");
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        edgeOptions.setCapability(CapabilityType.PROXY, proxy);
                    }
                    browser = new RemoteWebDriver(new URL(nodeUrl), edgeOptions);
                    break;
                case "ie":
                    InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                    LoggingPreferences ieLogprefs = new LoggingPreferences();
                    ieLogprefs.enable(LogType.PERFORMANCE, Level.ALL);
                    ieOptions.setCapability(CapabilityType.LOGGING_PREFS, ieLogprefs);
                    ieOptions.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
                    ieOptions.setCapability("goog:loggingPrefs", ieLogprefs);
                    DesiredCapabilities capabilityIE = DesiredCapabilities.internetExplorer();
                    capabilityIE.setPlatform(Platform.WINDOWS);
                    capabilityIE.setBrowserName(BrowserType.EDGE);
                    capabilityIE.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    capabilityIE.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
                    capabilityIE.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT , true);
                    ieOptions.merge(capabilityIE);
                    if(proxyRequired && proxyUrl != null) {
                        Proxy proxy = new Proxy();
                        proxy.setSslProxy(proxyUrl);
                        proxy.setFtpProxy(proxyUrl);
                        proxy.setNoProxy("127.0.0.1,localhost");
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        ieOptions.setCapability(CapabilityType.PROXY, proxy);
                    }
                    browser = new RemoteWebDriver(new URL(nodeUrl), ieOptions);
                    break;
                default:
                    break;

            }
            browser.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        }
        return browser;

    }

    public String createFailureScreenShots(String fileName , String relativescriptfailureScreenPath) throws IOException {
        String dateTime = new Date().toString();
        File scrFile = ((TakesScreenshot)browser).getScreenshotAs(OutputType.FILE);
        String file64Data = ((TakesScreenshot)browser).getScreenshotAs(OutputType.BASE64);
        String path = relativescriptfailureScreenPath+ fileName + ".png";
        try {
            FileUtils.copyFile(scrFile, new File(path));
        } catch (IOException e) {
            System.out.println("Capture failed : "+e.getMessage());
        }
        return "data:image/jpg;base64, " + file64Data ;
    }

    public void quietBrowser(WebDriver browser) {
        extent.flush();
        browser.close();
        browser.quit();
    }
    public void ExtractJSLogs() {
        LogEntries logEntries = browser.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            test.log(Status.FAIL, MarkupHelper.createLabel(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage()+" FAILED ", ExtentColor.valueOf(utililty.getEnvironmentProperties("logColor"))));
            System.out.println(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
        }
    }
    public  void closeReporting() {
        extent.flush();
    }

    public void initiateReporting(String name) {
        name = StringUtils.isEmpty(name) ? "Raptor": name;
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") +"/test-output/"+name+"-Report.html");
        //initialize ExtentReports and attach the HtmlReporter
        extent = new ExtentReports();

        //To add system or environment info by using the setSystemInfo method.
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        htmlReporter.config().setDocumentTitle("Raptor tests reports");
        htmlReporter.config().setReportName(name);
        htmlReporter.config().enableTimeline(true);
        //htmlReporter.config().setCSS(".nav-wrapper { color: orange}");
        //htmlReporter.config().setCSS(".brand-logo { content:url('')}");
        //htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);

        //htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
        extent.attachReporter(htmlReporter);
    }


    public void runScript(String jsonPath, String testName) throws IOException {
        if(browser == null) {
            browser = createBrowser(jsonPath);
        }
        testName = StringUtils.isEmpty(testName) ? jsonPath : testName;
        String displayTestName = "";
        AtomicReference<String> stepName = new AtomicReference<>("");
        try {
        displayTestName = StringUtils.isEmpty(testScript.getTestCaseConfig().getTestName()) ?  testName : testScript.getTestCaseConfig().getTestName();
        test = extent.createTest(displayTestName, "");
        WebDriverWait wait = new WebDriverWait(browser, Integer.parseInt(utililty.getEnvironmentProperties("app.defaultTimeOut")));
            JavascriptExecutor js = (JavascriptExecutor)browser;
//			// loop through all the setps from the json test script
            testScript.getTestSteps().forEach( script -> {
                JSONObject scriptObject = (JSONObject) script;
                stepName.set(scriptObject.getAsString("step") != null ? scriptObject.getAsString("step") : scriptObject.getAsString("stepName"));
                testSteps.executeTestBasedOnAction(js ,browser, scriptObject, wait);
                test.log(Status.INFO, MarkupHelper.createLabel(stepName +" --passed ", ExtentColor.valueOf(utililty.getEnvironmentProperties("passStepColor"))));
            } );
            test.log(Status.PASS, MarkupHelper.createLabel(displayTestName+" --SUCCESS ", ExtentColor.valueOf(utililty.getEnvironmentProperties("passTestColor"))));

        } catch (Exception e) {
            test.log(Status.INFO, MarkupHelper.createLabel(stepName +" --failed ", ExtentColor.valueOf(utililty.getEnvironmentProperties("failStepColor"))));
            test.log(Status.FAIL, MarkupHelper.createLabel(displayTestName+" --FAILED ", ExtentColor.valueOf(utililty.getEnvironmentProperties("failTestColor"))));
            ExtractJSLogs();
            test.log(Status.FAIL, "Error Snapshot : " + test.addScreenCaptureFromBase64String(createFailureScreenShots(testName, utililty.getEnvironmentProperties("app.relativescriptfailureScreenPath")),"errorScreenshot"));
            closeReporting();
            throw e;
            // No need to crash the tests if the screenshot fails
        } finally {
            browser.close();
            browser.quit();
            browser = null;
        }
    }
}
