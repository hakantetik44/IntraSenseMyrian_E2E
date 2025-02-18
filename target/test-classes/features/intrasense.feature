Feature: Intrasense Website Navigation
  As a user
  I want to navigate through the Intrasense website
  So that I can verify the Myrian platform information

  @smoke @ui
  Scenario: Verify Myrian Platform Information
    Given I am on the Intrasense homepage
    When I click on "Nos Solutions" link
    And I click on "Découvrir Myrian" link
    Then I should see the "Plateforme Myrian" heading
    And I should see the "Les avantages Myrian" section when scrolling down 