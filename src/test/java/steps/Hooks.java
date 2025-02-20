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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        // Stop video recording
        VideoRecorder.stopRecording(scenario.getName());
        
        // Get the video file
        File videoFile = new File("target/videos/" + scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_") + ".mp4");
        
        if (videoFile.exists()) {
            try {
                // Create videos directory in allure-results
                File allureVideoDir = new File("target/allure-results/videos");
                if (!allureVideoDir.exists()) {
                    allureVideoDir.mkdirs();
                }
                
                // Copy video to allure-results directory
                String videoFileName = videoFile.getName();
                File allureVideoFile = new File(allureVideoDir, videoFileName);
                Files.copy(videoFile.toPath(), allureVideoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Create HTML5 video player markup
                String videoHtml = String.format(
                    "<video width='100%%' height='100%%' controls autoplay>" +
                    "<source src='videos/%s' type='video/mp4'>" +
                    "Your browser does not support the video tag." +
                    "</video>",
                    videoFileName
                );
                
                // Attach video to Allure report
                Allure.addAttachment("Test Recording", "text/html", videoHtml);
                
                System.out.println("[Hooks] Video attached successfully: " + videoFile.getAbsolutePath() +
                                 " (Size: " + videoFile.length() + " bytes)");
            } catch (Exception e) {
                System.out.println("[Hooks] Failed to attach video: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Take screenshot if scenario fails
        if (scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) driverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
        }
        
        // Quit driver
        driverManager.quitDriver();
    }
} 