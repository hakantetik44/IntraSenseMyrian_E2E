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
import java.io.FileInputStream;
import java.nio.file.Files;

public class Hooks {
    private final DriverManager driverManager;
    
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        String testName = scenario.getName().replaceAll("\\s+", "_");
        
        // Start video recording before test begins
        VideoRecorder.startRecording(driverManager.getDriver(), testName);
        
        // Add scenario info to Allure report
        Allure.epic("Intrasense E2E Tests");
        Allure.feature(scenario.getId().split(";")[0].replace("-", " "));
        Allure.story(scenario.getName());
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        String testName = scenario.getName().replaceAll("\\s+", "_");
        
        try {
            // Take screenshot if test fails
            if (scenario.isFailed()) {
                saveScreenshot(scenario.getName());
            }
            
            // Stop video recording
            VideoRecorder.stopRecording(testName);
            
            // Wait a bit for the file to be fully written
            Thread.sleep(2000);
            
            // Save video recording
            String videoPath = "target/videos/" + testName + ".avi";
            File videoFile = new File(videoPath);
            
            if (videoFile.exists() && videoFile.length() > 0) {
                try {
                    // Attach video to Allure report using FileInputStream
                    try (FileInputStream fis = new FileInputStream(videoFile)) {
                        Allure.addAttachment(
                            "Test Recording", 
                            "video/x-msvideo",  // Correct MIME type for AVI
                            fis,
                            ".avi"
                        );
                    }
                    System.out.println("Video attached successfully: " + videoPath + 
                                     " (Size: " + videoFile.length() + " bytes)");
                } catch (Exception e) {
                    System.out.println("Failed to attach video: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Video file not found or empty: " + videoPath);
            }
            
            // Add test result status
            if (scenario.isFailed()) {
                Allure.label("status", "failed");
                saveTestStatus("❌ Test Failed: " + scenario.getName());
            } else {
                Allure.label("status", "passed");
                saveTestStatus("✅ Test Passed: " + scenario.getName());
            }
            
        } catch (Exception e) {
            System.out.println("Failed to capture evidence: " + e.getMessage());
            e.printStackTrace();
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

    @Attachment(value = "Test Status", type = "text/plain")
    private String saveTestStatus(String message) {
        return message;
    }
} 