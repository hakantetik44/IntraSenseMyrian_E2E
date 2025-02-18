package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.VideoRecorder;
import utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.File;

public class Hooks {
    private final DriverManager driverManager;
    
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        // Start video recording before test begins
        VideoRecorder.startRecording(
            driverManager.getDriver(), 
            scenario.getName().replaceAll("\\s+", "_")
        );
        
        // Add scenario info to Allure report
        Allure.feature(scenario.getId().split(";")[0].replace("-", " "));
        Allure.suite(scenario.getUri().toString());
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        try {
            // Take screenshot if test fails
            if (scenario.isFailed()) {
                // Capture screenshot
                final byte[] screenshot = ((TakesScreenshot) driverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
                    
                // Attach to Cucumber report
                scenario.attach(screenshot, "image/png", "Screenshot");
                
                // Attach to Allure report
                Allure.addAttachment("Screenshot", new ByteArrayInputStream(screenshot));
            }
            
            // Stop video recording after test completion
            VideoRecorder.stopRecording(
                scenario.getName().replaceAll("\\s+", "_")
            );
            
            // Try to find and attach the video file to Allure report
            String videoFileName = scenario.getName().replaceAll("\\s+", "_") + ".mp4";
            File videoFile = new File("target/videos/" + videoFileName);
            if (videoFile.exists()) {
                Allure.addAttachment("Test Recording", "video/mp4", videoFile.getAbsolutePath());
            }
            
            // Add scenario result to Allure
            if (scenario.isFailed()) {
                Allure.description("❌ Scenario Failed: " + scenario.getName());
            } else {
                Allure.description("✅ Scenario Passed: " + scenario.getName());
            }
            
        } catch (Exception e) {
            System.out.println("Failed to capture video or screenshot: " + e.getMessage());
            Allure.description("⚠️ Failed to capture evidence: " + e.getMessage());
        } finally {
            // Close browser
            if (driverManager.getDriver() != null) {
                driverManager.getDriver().quit();
            }
        }
    }
} 