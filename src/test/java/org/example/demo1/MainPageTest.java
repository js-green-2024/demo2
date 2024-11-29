package org.example.demo1;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.*;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

public class vg_runner {
    private final String appName = "vg runner app";
    private final String batchName = "vg runner Java";
    private final int viewPortWidth = 800;
    private final int viewPortHeight = 600;
    String myEyesServer = "https://eyesapi.applitools.com/"; //set to your server/cloud URL
    private String apiKey = System.getenv("MY_APPLITOOLS_API_KEY");
    private int concurrentSessions = 5;
    private EyesRunner runner = null;
    private Configuration suiteConfig;
    private Eyes eyes;
    private WebDriver webDriver;

    @BeforeSuite
    public void beforeTestSuite() {
        runner = new VisualGridRunner(new RunnerOptions().testConcurrency(10));
        // Create a configuration object, we will use this when setting up each test
        suiteConfig = (Configuration) new Configuration()
                // Visual Grid configurations
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.CHROME))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.CHROME_ONE_VERSION_BACK))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.CHROME_TWO_VERSIONS_BACK))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.FIREFOX))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.SAFARI))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.IE_10))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.IE_11))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.EDGE_CHROMIUM))
                .addBrowser(new DesktopBrowserInfo(viewPortWidth, viewPortHeight, BrowserType.EDGE_LEGACY))
                .addBrowser(new IosDeviceInfo(IosDeviceName.iPhone_X, ScreenOrientation.LANDSCAPE))
                .addBrowser(new ChromeEmulationInfo(DeviceName.Galaxy_S5, ScreenOrientation.PORTRAIT))
                // Checkpoint configurations
                // Test specific configurations ....
                .setViewportSize( new RectangleSize(viewPortWidth, viewPortHeight))
                // Test suite configurations
                .setApiKey(apiKey)
                .setServerUrl(myEyesServer)
                .setAppName(appName)
                .setBatch(new BatchInfo(batchName)
                        /* ...other configurations */ );
    }

    @BeforeMethod
    public void beforeEachTest(ITestResult result) {
        // Create the Eyes instance for the test and associate it with the runner
        eyes = new Eyes(runner);
        eyes.setConfiguration(suiteConfig);
        webDriver = new ChromeDriver();
    }

    @Test
    public void testHelloWorld() {
        // Update the Eyes configuration with test specific values
        Configuration testConfig = eyes.getConfiguration();
        testConfig.setTestName("Hello World test");
        eyes.setConfiguration(testConfig);

        // Open Eyes, the application,test name and viewport size are allready configured
        WebDriver driver = eyes.open(webDriver);

        // Now run the test

        // Visual checkpoint #1.
        driver.get("https://applitools.com/helloworld");   // navigate to website
        eyes.checkWindow("Before mouse click");

        // Visual checkpoint #2
        driver.findElement(By.tagName("button")).click();  // Click the button.
        eyes.checkWindow("After mouse click");
    }

    @AfterMethod
    public void afterEachTest(ITestResult result) {
        // check if an exception was thrown
        boolean testFailed = result.getStatus() == ITestResult.FAILURE;
        if (!testFailed) {
            // Close the Eyes instance, no need to wait for results, we'll get those at the end in afterTestSuite
            eyes.closeAsync();
        } else {
            // There was an exception so the test may be incomplete - abort the test
            eyes.abortAsync();
        }
        webDriver.quit();
    }

    @AfterSuite
    public void afterTestSuite(ITestContext testContext) {
        //Wait until the test results are available and retrieve them
        TestResultsSummary allTestResults = runner.getAllTestResults(false);
        for (TestResultContainer result : allTestResults) {
            handleTestResults(result);
        }
    }

    void handleTestResults(TestResultContainer summary) {
        Throwable ex = summary.getException();
        if (ex != null ) {
            System.out.printf("System error occured while checking target.\n");
        }
        TestResults result = summary.getTestResults();
        if (result == null) {
            System.out.printf("No test results information available\n");
        } else {
            System.out.printf("URL = %s,\n AppName = %s, testname = %s, Browser = %s,OS = %s, viewport = %dx%d, matched = %d,mismatched = %d, missing = %d,aborted = %s\n",
                    result.getUrl(),
                    result.getAppName(),
                    result.getName(),
                    result.getHostApp(),
                    result.getHostOS(),
                    result.getHostDisplaySize().getWidth(),
                    result.getHostDisplaySize().getHeight(),
                    result.getMatches(),
                    result.getMismatches(),
                    result.getMissing(),
                    (result.isAborted() ? "aborted" : "completed OK"));
        }
    }
}