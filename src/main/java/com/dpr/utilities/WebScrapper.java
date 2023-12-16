package com.dpr.utilities;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebScrapper {

    public static final String STR_SPACE = " ";
    private static final String STR_Y = "Y";
    private static final String STR_N = "N";
    public static final String XPATH_EMAIL = "//a[contains(@href,'mailto:')]";
    public static final String XPATH_COMPANY_NAME = "//a[contains(@href,'office.asp?')]";
    public static final String XPATH_OFFICE_ID = "//strong[contains(text(),'Office Id:')]";
    public static final String STR_SEMI_COLON = ":";
    public static final String XPATH_SUBS = "//table[@id='agentTable']/tbody/tr[@class='agentRow']/td[3]";
    public static final String XPATH_LOGOUT = "//a[contains(.,'Sign Out')]";
    public static final String XPATH_COOKIE_CONSENT_OK = "//button[@class='btn btn-sm btn-primary mls-js-cookie-consent-action']";
    public static final String XPATH_SUBMIT = "//button[@class='btn btn-sm btn-primary mls-js-submit-btn']";
    //public static final String USERNAME = "";
    public static final String USERNAME = "";
    public static final String PASSWORD = "";
    //public static final String PASSWORD = ""
    public static final String PINERGY_SIGN_IN_URL = "https://h3s.mlspin.com/tools/roster/agent.asp?aid=CN250694&nomenu=";
    public static final String STR_COMMA = ",";
    public static final String MY_SQL_DB_DRIVER_NAME = "com.mysql.jdbc.Driver";
    public static final String MY_SQL_DB_URL = "jdbc:mysql://127.0.0.1:3306/kkumar";
    //public static final String CHROME_WEB_DRIVER_PATH = "C:\\Users\\ajay.vishweshwara\\Downloads\\chromedriver_win32\\chromedriver.exe";
    /*
    public static final String AGENTS_TO_QUERY = "select distinct a.agent_name, a.agent_id,concat('https://h3c.mlspin.com/tools/roster/agent.asp?aid=',a.agent_id,'&nomenu=') "
    		+ " all_Links_to_Query from kkumar.ag_vol_all_trans_2_year_20231022 a where a.agent_id in ("
    		+ "select agent_id from kkumar.agent_emails where agent_non_manager_ind='N')  ";
    */
    public static final String AGENTS_TO_QUERY = "select  a.agent_name, a.agent_id,concat('https://h3c.mlspin.com/tools/roster/agent.asp?aid=',a.agent_id,'&nomenu=') "
    		+ " all_Links_to_Query from kkumar.agents_sales_volume a where a.agent_id not in ("
    		+ "select agent_id from kkumar.agent_emails  ) and a.agent_id='TM353568'  ";
    public static final String AGENT_RESULT_TO_INSERT = "insert into kkumar.agent_emails values (?,?,?,?,?,?,?,current_timestamp(),'kkumar') ON DUPLICATE KEY UPDATE "
												    		+ "agent_id = ? , "
												    		+ "lookup_status = ?,"
												    		+ "agent_name  =?,"
												    		+ "agent_email =?,"
												    		+ "office_id =?,"
												    		+ "office_name =?,"
												    		+ "agent_non_manager_ind =? ,"
												    		+ "date_entered = current_timestamp(),"
												    		+ "user_entered = 'kkumar'";
    
    public static final String CHROME_WEB_DRIVER_PATH = "/Users/kkg/Library/CloudStorage/OneDrive-QSoftSystems/Real/02_Clients/Leads/!Data_marketing/MLSPIN_BigData/Agent_Login_Extract/chromedriver";
    													
    
    
    //Output file of known agents
    public static final String UPDATED_AGENT_FILE_PATH = "/Users/kkg/Library/CloudStorage/OneDrive-QSoftSystems/Real/02_Clients/2022/!Data_marketing/MLSPIN_BigData/Agent_Login_Extract/AgentInfo.csv";

    //Output file of unknown agents
    public static final String UNKNOWN_AGENT_FILE_PATH = "/Users/kkg/Library/CloudStorage/OneDrive-QSoftSystems/Real/02_Clients/2022/!Data_marketing/MLSPIN_BigData/Agent_Login_Extract/AgentInfo_unknown.csv";

    public static void main(String args[]) throws IOException {

        AtomicInteger recordCounter= new AtomicInteger(0);
        WebDriver driver = getWebDriver();

        try {
            List<AgentInfo> updatedAgentInfo = new ArrayList<>();
            List<AgentInfo> unknownAgents = new ArrayList<>();

            AtomicInteger counter = new AtomicInteger(0);

            //Input file name
            //String fileName = "/Users/kkg/Library/CloudStorage/OneDrive-QSoftSystems/Real/02_Clients/2022/!Data_marketing/MLSPIN_BigData/Agent_Login_Extract/All_links_to_query_for_each_Agent.csv";
            //Stream<String> stream = Files.lines(Paths.get(fileName));
            Stream<AgentInfo> stream = getAgentsToLookUp().stream();

            //Login to Pinergy
            signInToPinergy(driver);

            stream.forEach(agentInfo -> {
                recordCounter.incrementAndGet();

                //Write every 100 records processed to file, don't want to write large chunk to file.
                if (counter.getAndIncrement() == 500) {
                    System.out.println("Writing 500 records to file");
                    //Write to file
                    //writeToCSVFile(updatedAgentInfo, false);
                    writeToDBSuccessfullLookups(updatedAgentInfo);
                    //writeToCSVFile(unknownAgents, true);
                    writeToDBFailedLookups(unknownAgents);
                    clearLists(updatedAgentInfo, unknownAgents);
                    counter.set(0);
                }

                //Uncomment to gracefully logout of Pinergy
                /*if(recordCounter.get()>10000){
                    logOut(driver);
                }*/
                
                //AgentInfo agent = getAgentInfo(e);
                System.out.println(String.format("INFO: Extracting Info for agent %s", agentInfo.getAgentName()));
                driver.get(agentInfo.getAllLinksToQuery());
                if (scrapeAgentInfo(driver, agentInfo)) {
                    updatedAgentInfo.add(agentInfo);
                } else {
                    unknownAgents.add(agentInfo);
                }
            });

            //Write remaining records to file
            //writeToCSVFile(updatedAgentInfo, false);
            //writeToCSVFile(unknownAgents, true);
            writeToDBSuccessfullLookups(updatedAgentInfo);
            writeToDBFailedLookups(unknownAgents);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Logout from Pinergy and close the driver
            logOut(driver);
        }

    }

    private static void clearLists(List<AgentInfo> updatedAgentInfo, List<AgentInfo> unknownAgents) {
        updatedAgentInfo.clear();
        unknownAgents.clear();
    }

    private static WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        //Comment below line to see actions on browser
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }
    
    
    /*
    private static void writeToCSVFile(List<String> agentInfos, boolean unknown) {
        try (FileOutputStream csvUpdatedAgentInfoFile = new FileOutputStream(new File(UPDATED_AGENT_FILE_PATH), true);
             FileOutputStream csvUnknownAgentsFile = new FileOutputStream(new File(UNKNOWN_AGENT_FILE_PATH), true);
             PrintWriter pw = new PrintWriter(unknown ? csvUnknownAgentsFile : csvUpdatedAgentInfoFile)) {

            FileOutputStream file = null;
            if (unknown) {
                file = csvUnknownAgentsFile;
            } else {
                file = csvUpdatedAgentInfoFile;
            }
            agentInfos.stream()
                    .forEach(pw::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    /*
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
    */

    private static void signInToPinergy(WebDriver driver) {

        driver.get(PINERGY_SIGN_IN_URL);

        System.out.println("INFO:Sign In to " + driver.getTitle());

        //Click ok for cookie consent
        driver.findElement(By.xpath(XPATH_COOKIE_CONSENT_OK)).click();

        WebElement username = driver.findElement(By.name("user_name"));
        WebElement password = driver.findElement(By.name("pass"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);

        driver.findElement(By.xpath(XPATH_SUBMIT)).click();
    }

    private static void logOut(WebDriver driver) {
        driver.findElement(By.xpath(XPATH_LOGOUT)).click();
        driver.close();
    }

    private static boolean scrapeAgentInfo(WebDriver driver, AgentInfo agent) {
        try {
            WebElement emailId = driver.findElement(By.xpath(XPATH_EMAIL));
            agent.setEmailId(emailId.getText());

            WebElement companyName = driver.findElement(By.xpath(XPATH_COMPANY_NAME));
            agent.setCompanyName(companyName.getText());

            WebElement officeId = driver.findElement(By.xpath(XPATH_OFFICE_ID));
            agent.setOfficeId(officeId.getText().split(STR_SPACE)[2]);

            boolean isAgent = findIsAgent(driver,companyName,agent.getAgentId());
            agent.setIsAgent(getIsAgent(isAgent));

            return true;
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: Unable to find agent " + agent.getAgentName()+"\n");
            e.printStackTrace(System.out);
        }
        return false;
    }

    private static String getIsAgent(boolean isAgent) {
        return isAgent ? STR_Y : STR_N;
    }

    /**
     * Check if agent's name appears in list of subscribers, if not then mark him as not an agent
     * @param driver
     * @param companyName
     * @param agentName
     * @return
     */
    public static boolean  findIsAgent(WebDriver driver, WebElement companyName, String agentId){
        companyName.click();
        List<WebElement> elements = driver.findElements(By.xpath(XPATH_SUBS));
        for(WebElement ele:elements) {
            if (agentId.equals(ele.getText())) {
                return true;
            }
        }
        return false;
    }
    
    
    private static List<AgentInfo> getAgentsToLookUp() {
    	List<AgentInfo> agents = new ArrayList<AgentInfo>();
    	try (Connection con = DriverManager.getConnection(MY_SQL_DB_URL, "root","");
    		 PreparedStatement ps = con.prepareStatement(AGENTS_TO_QUERY);
    		 ResultSet rs = ps.executeQuery();){
    		while (rs.next()) {
    			AgentInfo agentInfo = new AgentInfo();
    			agentInfo.setAgentName(rs.getString(1));
    			agentInfo.setAgentId(rs.getString(2));
    			agentInfo.setAllLinksToQuery(rs.getString(3));
    			agents.add(agentInfo);
    		}
    	} catch(SQLException e) {
    		e.printStackTrace(System.out);
    	}
    	return agents;
    }
    
    private static void writeToDBSuccessfullLookups(List<AgentInfo> updatedAgentInfoList) {
    	insertAgentEmail(updatedAgentInfoList, "PASS");
    }
    
    private static void writeToDBFailedLookups(List<AgentInfo> updatedAgentInfoList) {
    	insertAgentEmail(updatedAgentInfoList, "FAIL");
    }
    
    
    private static void insertAgentEmail(List<AgentInfo> updatedAgentInfoList, String lookupStatus) {
    	try(Connection con = DriverManager.getConnection(MY_SQL_DB_URL, "root", "");
    		PreparedStatement pstmt = con.prepareStatement(AGENT_RESULT_TO_INSERT);)  {
    			con.setAutoCommit(false);
    			Stream<AgentInfo> stream = updatedAgentInfoList.stream();
    			
    			stream.forEach(agentInfo -> {
    				try {
	    				pstmt.setString(1, agentInfo.getAgentId());
	    				pstmt.setString(8, agentInfo.getAgentId());
	    				pstmt.setString(2, lookupStatus);
	    				pstmt.setString(9, lookupStatus);
	    				if("PASS".equals(lookupStatus)) {
	    					pstmt.setString(3, agentInfo.getAgentName());
	    					pstmt.setString(10, agentInfo.getAgentName());
	    		    		pstmt.setString(4, agentInfo.getEmailId());
	    		    		pstmt.setString(11, agentInfo.getEmailId());
	    		    		pstmt.setString(5, agentInfo.getOfficeId());
	    		    		pstmt.setString(12, agentInfo.getOfficeId());
	    					pstmt.setString(6, agentInfo.getCompanyName());
	    					pstmt.setString(13, agentInfo.getCompanyName());
	    					pstmt.setString(7, agentInfo.getIsAgent());
	    					pstmt.setString(14, agentInfo.getIsAgent());
	    				} else {
	    					pstmt.setString(3, "");
	    		    		pstmt.setString(10, "");
	    		    		pstmt.setString(5, "");
	    		  			pstmt.setString(11, "");
	    		  			pstmt.setString(7, "");
	    		  			pstmt.setString(12, "");
	    		    		pstmt.setString(4, "");
	    		    		pstmt.setString(13, "");
	    		  			pstmt.setString(6, "");
	    		  			pstmt.setString(14, "");
	    				}
	    				pstmt.addBatch();
	    				pstmt.clearParameters();
    				} catch (Exception ee) {
    					ee.printStackTrace(System.out);
    				}
    			});
    			int[] inserted = pstmt.executeBatch();
    			System.out.println("Inserted " + inserted);
    			con.commit();
    		} catch(SQLException se) {
    			se.printStackTrace(System.out);
    		} catch(Exception e) {
    			e.printStackTrace(System.out);
    		} 
 
    }


}
