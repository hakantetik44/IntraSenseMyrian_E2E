package steps;

import pages.HomePage;
import utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

public class IntrasenseSteps {
    private WebDriver driver;
    private HomePage homePage;

    @Before
    public void setup() {
        driver = DriverManager.getDriver();
        homePage = new HomePage(driver);
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            DriverManager.takeScreenshot(scenario.getName() + "_failure");
        }
    }

    @Given("I am on the Intrasense homepage")
    public void iAmOnTheIntrasenseHomepage() {
        homePage.openHomePage();
    }

    @When("I click on {string} link")
    public void iClickOnLink(String linkText) {
        switch (linkText) {
            case "Nos Solutions":
                homePage.clickNosSolutions();
                break;
            case "Découvrir Myrian":
                homePage.clickDecouvrirMyrian();
                break;
            default:
                throw new IllegalArgumentException("Unknown link: " + linkText);
        }
    }

    @Then("I should see the {string} heading")
    public void iShouldSeeTheHeading(String headingText) {
        Assert.assertTrue("Plateforme Myrian heading should be visible", 
            homePage.isPlatformeMyrianDisplayed());
    }

    @And("I should see the {string} section when scrolling down")
    public void iShouldSeeTheSectionWhenScrollingDown(String sectionText) {
        Assert.assertTrue("Les avantages Myrian section should be visible", 
            homePage.verifyLesAvantagesMyrianSection());
    }
} 