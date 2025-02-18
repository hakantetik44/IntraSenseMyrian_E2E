package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import static pages.IntrasenseLocators.*;
import org.openqa.selenium.JavascriptExecutor;

public class HomePage extends BasePage {
    
    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Opening Intrasense homepage")
    public void openHomePage() {
        driver.get("https://intrasense.fr/fr/");
        acceptCookies();
    }

    @Step("Accepting cookies")
    private void acceptCookies() {
        try {
            // Wait for popup to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(COOKIE_POPUP));
            // Wait a bit for animation to complete
            Thread.sleep(1000);
            // Click accept button
            if (isElementDisplayed(COOKIE_ACCEPT_BUTTON)) {
                click(COOKIE_ACCEPT_BUTTON);
            }
            // Wait for popup to disappear
            wait.until(ExpectedConditions.invisibilityOfElementLocated(COOKIE_POPUP));
            // Wait additional time for page to stabilize
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Cookie popup not found or already accepted: " + e.getMessage());
        }
    }

    @Step("Clicking on 'Nos Solutions' link")
    public void clickNosSolutions() {
        waitForElementVisible(NOS_SOLUTIONS_LINK);
        scrollToElement(NOS_SOLUTIONS_LINK);
        click(NOS_SOLUTIONS_LINK);
        // Wait for page to load after click
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Step("Clicking on 'Découvrir Myrian' link")
    public void clickDecouvrirMyrian() {
        try {
            // Wait for element to be present and visible
            wait.until(ExpectedConditions.presenceOfElementLocated(DECOUVRIR_MYRIAN_LINK));
            waitForElementVisible(DECOUVRIR_MYRIAN_LINK);
            
            // Scroll to element with offset to ensure it's fully visible
            scrollToElement(DECOUVRIR_MYRIAN_LINK);
            Thread.sleep(1000); // Wait for scroll to complete
            
            // Wait for element to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(DECOUVRIR_MYRIAN_LINK));
            
            // Click the element
            click(DECOUVRIR_MYRIAN_LINK);
            
            // Wait for navigation
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Step("Verifying 'Plateforme Myrian' heading is displayed")
    public boolean isPlatformeMyrianDisplayed() {
        try {
            // Wait for page to load
            Thread.sleep(2000);
            return isElementDisplayed(PLATEFORME_MYRIAN_HEADING);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Step("Scrolling to and verifying 'Les avantages Myrian' section")
    public boolean verifyLesAvantagesMyrianSection() {
        try {
            // Wait for page to load
            Thread.sleep(2000);
            
            // Find the element
            WebElement avantagesHeading = driver.findElement(LES_AVANTAGES_MYRIAN_SECTION);
            
            // Perform smooth scroll using JavaScript
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
                avantagesHeading
            );
            
            // Wait for scroll and animations to complete
            Thread.sleep(2000);
            
            // Verify element is visible and contains correct text
            return avantagesHeading.isDisplayed() && 
                   avantagesHeading.getText().contains("Les avantages Myrian");
                   
        } catch (Exception e) {
            System.out.println("Error verifying Les avantages section: " + e.getMessage());
            return false;
        }
    }
} 