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
import java.nio.file.Path;

public class Hooks {
    private final DriverManager driverManager;
    
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        String testName = scenario.getName().replaceAll("\\s+", "_");
        
        // Add scenario info to Allure report
        Allure.epic("Intrasense E2E Tests");
        Allure.feature(scenario.getId().split(";")[0].replace("-", " "));
        Allure.story(scenario.getName());
        
        // Start video recording
        VideoRecorder.startRecording(driverManager.getDriver(), testName);
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
            
            // Wait for video file to be fully written
            Thread.sleep(2000);
            
            // Get video file path
            String videoPath = VideoRecorder.getCurrentVideoPath();
            if (videoPath != null) {
                File videoFile = new File(videoPath);
                if (videoFile.exists() && videoFile.length() > 0) {
                    // Read video file
                    byte[] videoBytes = Files.readAllBytes(videoFile.toPath());
                    
                    // Attach video to Allure report
                    try (ByteArrayInputStream videoStream = new ByteArrayInputStream(videoBytes)) {
                        Allure.addAttachment(
                            "Test Recording",
                            "video/mp4",
                            videoStream,
                            ".mp4"
                        );
                    }
                    
                    System.out.println("[Hooks] Video attached successfully: " + videoFile.getAbsolutePath() +
                                     " (Size: " + videoFile.length() + " bytes)");
                } else {
                    System.out.println("[Hooks] Video file not found or empty: " + videoPath);
                }
            } else {
                System.out.println("[Hooks] No video path available");
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
            System.out.println("[Hooks] Error in afterScenario: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure browser is closed
            try {
                if (driverManager.getDriver() != null) {
                    driverManager.getDriver().quit();
                }
            } catch (Exception e) {
                System.out.println("[Hooks] Error closing browser: " + e.getMessage());
            }
        }
    }
    
    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] saveScreenshot(String name) {
        try {
            return ((TakesScreenshot) driverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            System.out.println("[Hooks] Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
    
    @Attachment(value = "Test Status", type = "text/plain")
    private String saveTestStatus(String message) {
        return message;
    }
}