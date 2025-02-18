package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.VideoRecorder;
import utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks {
    private final DriverManager driverManager;
    
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        // Test başlamadan önce video kaydını başlat
        VideoRecorder.startRecording(driverManager.getDriver(), scenario.getName().replaceAll("\\s+", "_"));
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        try {
            // Eğer test başarısız olduysa ekran görüntüsü al
            if (scenario.isFailed()) {
                final byte[] screenshot = ((TakesScreenshot) driverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot");
            }
            
            // Test bittikten sonra video kaydını durdur
            VideoRecorder.stopRecording(scenario.getName().replaceAll("\\s+", "_"));
            
        } catch (Exception e) {
            System.out.println("Video kaydı veya screenshot alınamadı: " + e.getMessage());
        } finally {
            // Browser'ı kapat
            if (driverManager.getDriver() != null) {
                driverManager.getDriver().quit();
            }
        }
    }
} 