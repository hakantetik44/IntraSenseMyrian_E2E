package pages;

import org.openqa.selenium.By;

public class IntrasenseLocators {
    // Cookie Popup
    public static final By COOKIE_POPUP = By.id("pum-5702");
    public static final By COOKIE_ACCEPT_BUTTON = By.cssSelector(".pum-close.popmake-close");
    
    // Header Navigation
    public static final By NOS_SOLUTIONS_LINK = By.cssSelector("a[href*='nos-solutions']");
    public static final By DECOUVRIR_MYRIAN_LINK = By.id("link_text-621-837");
    
    // Myrian Platform Section
    public static final By PLATEFORME_MYRIAN_HEADING = By.cssSelector(".ct-headline.title-n1");
    public static final By LES_AVANTAGES_MYRIAN_SECTION = By.id("headline-106-877");
} 