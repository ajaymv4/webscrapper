package com.dpr.utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * WebScrapper utilities
 */
public class Utils {
    public static final String SEARCH_AGENT_PAGE_URL = "https://h3u.mlspin.com/MLS.Pinergy.Roster/Search";
    public static final String UPDATED_AGENT_FILE_PATH = "src\\main\\resources\\Downloads\\AgentInfo_Extracted.csv";
    public static final String UNKNOWN_AGENT_FILE_PATH = "src\\main\\resources\\Downloads\\AgentInfo_unknown.csv";
    public static final String OFFICE_EMPLOYEES_FILE_PATH = "src\\main\\resources\\Downloads\\Office_Employees.csv";
    public static final String PINERGY_SIGN_IN_URL = "https://h3s.mlspin.com/MLS.Pinergy.Roster/Search";
    public static final String XPATH_COOKIE_CONSENT_OK = "//button[@class='btn btn-sm btn-primary mls-js-cookie-consent-action']";
    public static final String XPATH_SUBMIT = "//button[@class='btn btn-sm btn-primary mls-js-submit-btn']";
    public static final String XPATH_CON_PINERGY = "//button[contains(text(), 'Click Here to Continue to Pinergy')]";
    public static final String XPATH_EMAIL = "//a[contains(@href,'mailto:')]";
    public static final String XPATH_COMPANY_NAME = "//a[contains(@href,'/MLS.Pinergy.Roster/Details')]";
    public static final String XPATH_OFFICE_ID = "//span[@class='mls-ros-detail-fld-lbl' and normalize-space()='Office ID:']/following-sibling::span[1]";
    public static final String XPATH_PHONE = "//span[@class='mls-ros-detail-fld-lbl' and text()='Phone:']/following-sibling::span[contains(@class, 'mls-phone-non-mobile')]";
    public static final String XPATH_SUBS = "//table[@id='agentTable']/tbody/tr[@class='agentRow']/td[3]";
    public static final String CHROME_WEB_DRIVER_PATH = "src\\main\\resources\\chromedriver.exe";
    public static final String USERNAME = "CN226136";
    public static final String PASSWORD = "OnsiteOffshore#2";
    public static final String AGENT_ID = "agentId";
    public static final String STR_Y = "Y";
    public static final String STR_N = "N";
    public static final String USER_NAME_FIELD = "user_name";
    public static final String PASSWORD_FIELD = "pass";
    public static final String LOGIN_ERROR_ASP = "Login_Error.asp";
    public static final String MLS_JQ_MODAL_IFRAME = "mls-jq-modal-iframe";
    public static final String AGENT_RESULT_GRID = "AgntRsltsGrid";
    public static final String STR_COMMA = ",";

    public static void landOnRosterPage(WebDriver driver) {
        //Agent search page
        driver.get(SEARCH_AGENT_PAGE_URL);
    }

    /**
     * Writes agents data to CSV files
     *
     * @param knownAgents
     * @param unknownAgents
     */
    public static void writeToCSVFile(List<AgentInfo> knownAgents, List<AgentInfo> officeEmployees, List<AgentInfo> unknownAgents) {
        try (FileOutputStream csvUpdatedAgentInfoFile = new FileOutputStream(new File(UPDATED_AGENT_FILE_PATH), true);
             FileOutputStream csOfficeEmployeesFile = new FileOutputStream(new File(OFFICE_EMPLOYEES_FILE_PATH), true);
             FileOutputStream csvUnknownAgentsFile = new FileOutputStream(new File(UNKNOWN_AGENT_FILE_PATH), true);
             PrintWriter pwKnownAgents = new PrintWriter(csvUpdatedAgentInfoFile);
             PrintWriter pwUnKnownAgents = new PrintWriter(csvUnknownAgentsFile);
             PrintWriter pwOfficeEmployees = new PrintWriter(csOfficeEmployeesFile);) {

            if (!knownAgents.isEmpty()) {
                getFileWriterFunction().apply(knownAgents, pwKnownAgents);
            }
            if (!unknownAgents.isEmpty()) {
                getFileWriterFunction().apply(unknownAgents, pwUnKnownAgents);
            }
            if (!officeEmployees.isEmpty()) {
                getFileWriterFunction().apply(officeEmployees, pwOfficeEmployees);
            }
            pwKnownAgents.flush();
            pwUnKnownAgents.flush();
            pwOfficeEmployees.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints location of generated output files
     */
    public static void outputFileLocation() {
        System.out.println(String.format("INFO: Known agents file :: %s", Paths.get(UPDATED_AGENT_FILE_PATH).toAbsolutePath()));
        System.out.println(String.format("INFO: UnKnown agents file :: %s", Paths.get(UNKNOWN_AGENT_FILE_PATH).toAbsolutePath()));
        System.out.println(String.format("INFO: Office employees file :: %s", Paths.get(OFFICE_EMPLOYEES_FILE_PATH).toAbsolutePath()));
    }

    /**
     * Deletes below listed files if any, during startup
     * <li>{@value #UPDATED_AGENT_FILE_PATH}</li>
     * <li>{@value #OFFICE_EMPLOYEES_FILE_PATH}</li>
     * <li>{@value #UNKNOWN_AGENT_FILE_PATH}</li>
     */
    public static void deleteOutputFiles() {
        try {
            Path updatedAgentsFilePath = Paths.get(UPDATED_AGENT_FILE_PATH);
            Path officeEmployeeFilePath = Paths.get(OFFICE_EMPLOYEES_FILE_PATH);
            Path unknownAgentsFilePath = Paths.get(UNKNOWN_AGENT_FILE_PATH);

            if (Files.exists(updatedAgentsFilePath)) {
                Files.delete(updatedAgentsFilePath);
            }
            if (Files.exists(officeEmployeeFilePath)) {
                Files.delete(officeEmployeeFilePath);
            }
            if (Files.exists(unknownAgentsFilePath)) {
                Files.delete(unknownAgentsFilePath);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sign in to {@link Utils#PINERGY_SIGN_IN_URL}
     *
     * @param driver
     * @throws InterruptedException
     */
    public static void signInToPinergy(WebDriver driver) throws InterruptedException {

        driver.get(PINERGY_SIGN_IN_URL);

        System.out.println("INFO:Sign In to " + driver.getTitle());

        //Click ok for cookie consent
        driver.findElement(By.xpath(XPATH_COOKIE_CONSENT_OK)).click();

        WebElement username = getElementByFieldName(driver, USER_NAME_FIELD);
        WebElement password = getElementByFieldName(driver, PASSWORD_FIELD);

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);

        driver.findElement(By.xpath(XPATH_SUBMIT)).click();

        //Sign in violation page
        if (driver.getCurrentUrl().contains(LOGIN_ERROR_ASP)) {
            //driver.wait(2L);
            WebElement continueButton = getElementByXPath(driver, XPATH_CON_PINERGY);
            continueButton.click();
        }
    }

    /**
     * Check if agent's name appears in list of subscribers, if not then mark him as not an agent
     *
     * @param driver
     * @param companyName
     * @return
     */
    public static boolean findIsAgent(WebDriver driver, WebElement companyName, String agentId) {
        companyName.click();
        List<WebElement> elements = driver.findElements(By.xpath(XPATH_SUBS));
        for (WebElement ele : elements) {
            if (agentId.equals(ele.getText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param driver
     * @param agent
     * @return
     * @throws InterruptedException
     */
    public static boolean scrapeAgentInfo(WebDriver driver, AgentInfo agent, List<AgentInfo> unknownAgents, List<AgentInfo> officeEmployees) throws InterruptedException {

        synchronized (driver) {
            try {
                // Wait until the page is fully loaded
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(AGENT_ID)));

                //Input agentId
                WebElement agentIdInput = driver.findElement(By.id(AGENT_ID));
                agentIdInput.clear();
                agentIdInput.sendKeys(agent.getAgentId());
                //agentIdInput.sendKeys("CT000331");

                agentIdInput.sendKeys(Keys.ENTER);

                // Wait until the page is fully loaded
                //WebDriverWait waitAgentResult = new WebDriverWait(driver, 2);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(AGENT_RESULT_GRID)));
                WebElement resultsTable = driver.findElement(By.id(AGENT_RESULT_GRID));

                if (null != resultsTable) {

                    Navigations.navigateToOfficeDetailsSection(driver);

                    // Switch to the iframe context, otherwise we cannot find elements in the dialog box
                    WebElement iframeElement = driver.findElement(By.className(MLS_JQ_MODAL_IFRAME));
                    driver.switchTo().frame(iframeElement);

               /* WebElement participantElement = driver.findElement(By.xpath(String.format(AREF_XPATH, agent.getAgentId())));
                String participantName = participantElement.findElement(By.tagName("span")).getText();*/

                    boolean isAgentInOffice = isInOfficeContacts(driver, agent.getAgentName());

                    //Compare participant name
                    if (isAgentInOffice) {
                        agent.setAgent(false);
                        officeEmployees.add(agent);
                        //We don't need any details if he is an office employee
                    } else {
                        agent.setAgent(true);
                        Navigations.refreshPage(driver);

                        Navigations.navigateToAgentDetailsSection(driver);

                        //Capture agent's emailId, company name, phone number and office id
                        extractAgentDetails(driver, agent);
                    }
                    //pressEscapeKey(driver); - Pressing escape key also didn't work, so refreshing the page is only option now.
                    Navigations.refreshPage(driver);
                    return agent.isAgent();
                } else {
                    System.out.println(String.format("ERROR: Agent id %s not found in system", agent.getAgentId()));
                    unknownAgents.add(agent);
                }
            } catch (TimeoutException exception) {
                System.out.println(String.format("ERROR-Timeout: Unable to find the agent, name - %s|id - %s", agent.getAgentName(),agent.getAgentId()));
                unknownAgents.add(agent);
            } catch (NoSuchElementException e) {
                System.out.println(String.format("ERROR-NoSuchElement: Unable to find the agent, name - %s|id - %s", agent.getAgentName(),agent.getAgentId()));
                agent.setException(e.getClass().getName());
                unknownAgents.add(agent);
                //e.printStackTrace(System.out);
            }
            return false;
        }
    }

    public static boolean isInOfficeContacts(WebDriver driver, String agentName) {
        boolean agentFound = false;
        try {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".mls-ros-dtl-tbl.mls-ros-dtl-office-contacts-tbl")));

            // Find the table element with the specified CSS classes
            WebElement table = driver.findElement(By.cssSelector(".mls-ros-dtl-tbl.mls-ros-dtl-office-contacts-tbl"));

            // Get all rows within the table
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            // Iterate through each row and check for the text
            for (WebElement row : rows) {
                // Get all columns (cells) within the row
                List<WebElement> columns = row.findElements(By.tagName("td"));

                // Iterate through each column and check for the text
                for (WebElement column : columns) {
                    if (column.getText().contains(agentName)) {
                        agentFound = true;
                        break;
                    }
                }
                if (agentFound) {
                    break;
                }
            }
        } catch (TimeoutException exception) {
            System.out.println(String.format("ERROR - Timeout - isInOfficeContacts for agent name -%s",agentName));
        }
        return agentFound;
    }


    /**
     * Extracts agent details
     *
     * @param driver
     * @param agent
     */
    public static void extractAgentDetails(WebDriver driver, AgentInfo agent) {

        // Switch to the iframe context, otherwise we cannot find elements in the dialog box
        WebElement iframeElement = driver.findElement(By.className(MLS_JQ_MODAL_IFRAME));
        driver.switchTo().frame(iframeElement);

        WebDriverWait wait = new WebDriverWait(driver,10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_EMAIL)));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_COMPANY_NAME)));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_PHONE)));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_OFFICE_ID)));

        WebElement emailId = driver.findElement(By.xpath(XPATH_EMAIL));
        agent.setEmailId(emailId.getText());

        WebElement companyName = driver.findElement(By.xpath(XPATH_COMPANY_NAME));
        agent.setCompanyName(companyName.getText());

        WebElement phoneNumber = driver.findElement(By.xpath(XPATH_PHONE));
        agent.setPhoneNumber(phoneNumber.getText());

        WebElement officeId = driver.findElement(By.xpath(XPATH_OFFICE_ID));
        agent.setOfficeId(officeId.getText());
    }

    /**
     * Set web driver as system property
     *
     * @return
     */
    public static WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080"); // Change the values as needed
        //Comment below line to see actions on browser
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    /**
     * Clears the list
     *
     * @param updatedAgentInfo
     * @param unknownAgents
     */
    public static void clearLists(List<AgentInfo> updatedAgentInfo, List<AgentInfo> officeEmployees, List<AgentInfo> unknownAgents) {
        updatedAgentInfo.clear();
        unknownAgents.clear();
        officeEmployees.clear();
    }



    /**
     * Reads a file into list
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static List<AgentInfo> readAgentsFile(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                .parallel()
                .map(agent -> getAgentInfo(agent))
                .collect(Collectors.toList());
    }

    /**
     * A BiFunction to write data to a supplied {@link PrintWriter}
     *
     * @return
     */
    private static BiFunction<List<AgentInfo>, PrintWriter, Boolean> getFileWriterFunction() {
        return (agentsInfo, pw) -> {
            agentsInfo.stream().forEach(pw::println);
            pw.flush();
            return true;
        };
    }

    /**
     * Get {@link WebElement} by name
     *
     * @param driver
     * @param fieldName
     * @return
     */
    private static WebElement getElementByFieldName(WebDriver driver, String fieldName) {
        return driver.findElement(By.name(fieldName));
    }

    /**
     * Get {@link WebElement} by xPath
     *
     * @param driver
     * @param xPath
     * @return
     */
    private static WebElement getElementByXPath(WebDriver driver, String xPath) {
        return driver.findElement(By.xpath(xPath));
    }

    private static AgentInfo getAgentInfo(String e) {
        String[] agentLine = e.split(STR_COMMA);

        AgentInfo agent = new AgentInfo();
        agent.setAgentId(agentLine[1]);
        agent.setAgentName(agentLine[0]);
        //agent.setManager(agentLine[4]);
        //agent.setEmailId(agentLine[3]);
        //agent.setCompanyName(agentLine[5]);
        agent.setAllLinksToQuery(agentLine[2]);
        return agent;
    }

}
