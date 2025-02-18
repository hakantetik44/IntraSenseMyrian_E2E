package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DriverManager {
    private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
            String browser = ConfigReader.getBrowser().toLowerCase();
            boolean headless = ConfigReader.getHeadless();

            switch (browser) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--start-maximized");
                    if (headless) {
                        chromeOptions.addArguments("--headless");
                    }
                    driver = new ChromeDriver(chromeOptions);
                    break;

                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headless) {
                        firefoxOptions.addArguments("--headless");
                    }
                    driver = new FirefoxDriver(firefoxOptions);
                    break;

                default:
                    throw new RuntimeException("Unsupported browser: " + browser);
            }

            // Set timeouts from configuration
            driver.manage().timeouts().implicitlyWait(ConfigReader.getImplicitWait(), TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(ConfigReader.getPageLoadTimeout(), TimeUnit.SECONDS);
        }
        return driver;
    }

    public static void takeScreenshot(String name) {
        if (driver instanceof TakesScreenshot) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path targetPath = Paths.get(ConfigReader.getProperty("screenshot_path"), timestamp + "_" + name + ".png");
                Files.createDirectories(targetPath.getParent());
                Files.copy(screenshot.toPath(), targetPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
} 