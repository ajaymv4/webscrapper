package com.dpr.utilities;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Navigations {

    public static final String XPATH_CON_PINERGY = "//button[contains(text(), 'Click Here to Continue to Pinergy')]";
    public static final String XPATH_TOOLS = "//a[text()='Tools' and @href='/Transfer.asp?destPage=tools']";
    public static final String XPATH_LOGOUT = "//a[@class='signout-link']";
    public static final String XPATH_ROSTERS = "//a[contains(@href,'transfer.asp?destPage=roster')]";
    public static final String AGENTS_LINK_SECTION_XPATH = "//table[@id='AgntRsltsGrid']/tbody/tr/td[2]/button";
    public static final String OFFICE_LINK_XPATH = "//table[@id='AgntRsltsGrid']/tbody/tr/td[5]/button";
    public static final String BTN_EDIT_SEARCH = "btnEditSearch";

    public static String editAgentPage;

    /**
     * Navigate to Roasters page
     *
     * @param driver
     */
    public static void navigateToRostersPage(WebDriver driver) {
        // Wait until the page is fully loaded
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_TOOLS)));

        WebElement tools = driver.findElement(By.xpath(XPATH_TOOLS));
        tools.click();

        //Click on Rosters
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_ROSTERS)));
        WebElement rosters = driver.findElement(By.xpath(XPATH_ROSTERS));
        rosters.click();
    }

    /**
     * Navigate to Agent details for an agent
     *
     * @param driver
     */
    public static void navigateToAgentDetailsSection(WebDriver driver) {
        /*WebDriverWait wait = new WebDriverWait(driver,10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(AGENTS_LINK_SECTION_XPATH)));
        */
        //Navigate to agent details page
        WebElement agentLink = driver.findElement(By.xpath(AGENTS_LINK_SECTION_XPATH));
        agentLink.click();
    }

    /**
     * Navigate to Office details for an agent
     *
     * @param driver
     */
    public static void navigateToOfficeDetailsSection(WebDriver driver) {
        //Navigate to office details section
        WebElement officeLink = driver.findElement(By.xpath(OFFICE_LINK_XPATH));
        officeLink.click();
    }

    /**
     * Refreshes the page
     *
     * @param driver
     */
    public static void refreshPage(WebDriver driver) {
        driver.switchTo().defaultContent();
        driver.navigate().refresh();
    }

    public static void navigateToEditAgentSearchPage(WebDriver driver) {
        refreshPage(driver);

       // Wait until the page is fully loaded, timeout at 12 seconds is ideal
        WebDriverWait wait = new WebDriverWait(driver, 12);
        wait.until(ExpectedConditions.elementToBeClickable(By.id(BTN_EDIT_SEARCH)));

        WebElement button = driver.findElement(By.id(BTN_EDIT_SEARCH));
        button.click();
    }


    /**
     * Logout of Pinergy
     *
     * @param driver
     */
    public static void logOut(WebDriver driver) {
        //driver.findElement(By.xpath(XPATH_LOGOUT)).click();
        WebElement signOutLink = driver.findElement(By.className("signout-link")); // Using link text
        // Or use: WebElement signOutLink = driver.findElement(By.className("signout-link")); // Using class name
        signOutLink.click();
        driver.quit();
    }

}
