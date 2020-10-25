package com.raptor;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * The RaptorBase program is a base class to help
 * in automation test for webcomponents.
 *
 * @author  Salman Saifi
 * @version 1.0.0
 * @since   2020-03-25
 */

@TestPropertySource(locations={"classpath:application-test.properties","classpath:application-locator.properties"})
@PropertySource(value = "classpath:application-test.properties",ignoreResourceNotFound = true)
@PropertySource(value = "classpath:application-locator.properties",ignoreResourceNotFound = true)
public abstract class RaptorBase {

    @Autowired
     TestExecution testExecution;

    public void initiateReporting(String name) {

        testExecution.initiateReporting(name);
    }

    public void closeReporting() {

        testExecution.closeReporting();
    }

    public WebDriver getBrowser(String testFileName) throws MalformedURLException {
        return testExecution.createBrowser(testFileName);
    }

    public void runRaptorScript(String testFileName, String testName) throws IOException {
        testExecution.runScript(testFileName,testName);
    }


}
