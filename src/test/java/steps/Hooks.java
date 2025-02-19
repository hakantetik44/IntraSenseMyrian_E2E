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
            
            // Wait for video processing
            Thread.sleep(3000);
            
            // Try to attach video recording
            attachVideo(testName);
            
            // Add test result status
            if (scenario.isFailed()) {
                Allure.label("status", "failed");
                saveTestStatus("❌ Test Failed: " + scenario.getName());
            } else {
                Allure.label("status", "passed");
                saveTestStatus("✅ Test Passed: " + scenario.getName());
            }
            
        } catch (Exception e) {
            System.out.println("Error in afterScenario hook: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close browser
            if (driverManager.getDriver() != null) {
                driverManager.getDriver().quit();
            }
        }
    }
    
    private void attachVideo(String testName) {
        try {
            File mp4File = new File("target/videos/" + testName + ".mp4");
            
            if (mp4File.exists() && mp4File.length() > 0) {
                byte[] videoBytes = Files.readAllBytes(mp4File.toPath());
                
                // Attach video using both methods for better compatibility
                // Method 1: Direct attachment
                Allure.addAttachment("Test Recording", "video/mp4", 
                    new ByteArrayInputStream(videoBytes), ".mp4");
                
                // Method 2: HTML5 player
                String videoHtml = String.format(
                    "<video width='100%%' height='100%%' controls autoplay>" +
                    "<source src='data:video/mp4;base64,%s' type='video/mp4'>" +
                    "Your browser does not support the video tag." +
                    "</video>",
                    java.util.Base64.getEncoder().encodeToString(videoBytes)
                );
                
                Allure.addAttachment("Test Recording (HTML5 Player)", "text/html", videoHtml, ".html");
                
                System.out.println("Video attached successfully: " + mp4File.getAbsolutePath() + 
                                 " (Size: " + mp4File.length() + " bytes)");
            } else {
                System.out.println("Video file not found or empty: " + mp4File.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Failed to attach video: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] saveScreenshot(String name) {
        try {
            return ((TakesScreenshot) driverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            System.out.println("Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
    
    @Attachment(value = "Test Status", type = "text/plain")
    private String saveTestStatus(String message) {
        return message;
    }
}