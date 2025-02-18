package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.VideoRecorder;
import utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

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
        Allure.epic("Intrasense E2E Tests");
        Allure.feature(scenario.getId().split(";")[0].replace("-", " "));
        Allure.story(scenario.getName());
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        try {
            // Take screenshot if test fails
            if (scenario.isFailed()) {
                saveScreenshot(scenario.getName());
            }
            
            // Stop video recording after test completion
            VideoRecorder.stopRecording(
                scenario.getName().replaceAll("\\s+", "_")
            );
            
            // Save video recording
            saveVideo(scenario.getName());
            
            // Add test result status
            if (scenario.isFailed()) {
                Allure.label("status", "failed");
                saveTestStatus("❌ Test Failed: " + scenario.getName());
            } else {
                Allure.label("status", "passed");
                saveTestStatus("✅ Test Passed: " + scenario.getName());
            }
            
        } catch (Exception e) {
            System.out.println("Failed to capture video or screenshot: " + e.getMessage());
            saveTestStatus("⚠️ Evidence Capture Failed: " + e.getMessage());
        } finally {
            // Close browser
            if (driverManager.getDriver() != null) {
                driverManager.getDriver().quit();
            }
        }
    }

    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] saveScreenshot(String scenarioName) {
        return ((TakesScreenshot) driverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    @Attachment(value = "Video Recording", type = "video/mp4")
    private byte[] saveVideo(String scenarioName) {
        try {
            String videoPath = "target/videos/" + scenarioName.replaceAll("\\s+", "_") + ".mp4";
            File videoFile = new File(videoPath);
            if (videoFile.exists()) {
                return Files.readAllBytes(videoFile.toPath());
            }
        } catch (Exception e) {
            System.out.println("Failed to attach video: " + e.getMessage());
        }
        return null;
    }

    @Attachment(value = "Test Status", type = "text/plain")
    private String saveTestStatus(String message) {
        return message;
    }
} 