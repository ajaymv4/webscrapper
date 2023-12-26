package com.dpr.utilities;


import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dpr.utilities.Utils.STR_N;
import static com.dpr.utilities.Utils.STR_Y;

public class WebScrapper {

    private static AtomicInteger recordCounter = new AtomicInteger(0);
    private static List<AgentInfo> updatedAgentInfo = new ArrayList<>(); //To hold agents data
    private static List<AgentInfo> unknownAgents = new ArrayList<>(); //To hold unknown agents,who are not found on rosters page
    private static List<AgentInfo> officeEmployee = new ArrayList<>(); //To hold office managers/participants/
    private static WebDriver driver;
    private static String fileName = "src\\main\\resources\\input\\Agent_Info.csv";  //Input file name


    /* public static final String MY_SQL_DB_DRIVER_NAME = "com.mysql.jdbc.Driver";
    //public static final String MY_SQL_DB_URL = "jdbc:mysql://127.0.0.1:3306/kkumar";

    public static final String AGENTS_TO_QUERY = "select distinct a.agent_name, a.agent_id,concat('https://h3c.mlspin.com/tools/roster/agent.asp?aid=',a.agent_id,'&nomenu=') "
    		+ " all_Links_to_Query from kkumar.ag_vol_all_trans_2_year_20231022 a where a.agent_id in ("
    		+ "select agent_id from kkumar.agent_emails where agent_non_manager_ind='N')  ";
    */
   /* public static final String AGENTS_TO_QUERY = "select  a.agent_name, a.agent_id,concat('https://h3c.mlspin.com/tools/roster/agent.asp?aid=',a.agent_id,'&nomenu=') "
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
												    		+ "user_entered = 'kkumar'";*/


    public static void main(String args[]) throws IOException {

        try {
            //Delete Output Files
            Utils.deleteOutputFiles();

            //Read all the lines in the file to a list
            List<AgentInfo> agents = Utils.readAgentsFile(fileName);

            //Change this to false to directly land on agent search page, 
            // but sometimes it redirects to signin page, so always go via Tools -> Roster page.
            boolean isNavigateToRoster = true;

            //counter to write data to file
            AtomicInteger counter = new AtomicInteger(0);
            int limit = 10;

            //Selenium web driver to scrape agent info
            driver = Utils.getWebDriver();

            //Login to Pinergy
            Utils.signInToPinergy(driver);

            //Navigate to Rosters page
            if (isNavigateToRoster) {
                Navigations.navigateToRostersPage(driver);
            } else {
                Utils.landOnRosterPage(driver);
            }

            //For each record in list start scrapping data
            agents.forEach(agentInfo -> {
                searchAndExtractAgentInfo(agentInfo,false,counter);
                saveEveryXRecords(counter,limit);
                //Navigate back to edit search page
                Navigations.navigateToEditAgentSearchPage(driver);
            });

            //Write remaining records to file
            Utils.writeToCSVFile(updatedAgentInfo, officeEmployee,unknownAgents);

            //writeToDBSuccessfullLookups(updatedAgentInfo);
            //writeToDBFailedLookups(unknownAgents);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.outputFileLocation();
            //Logout from Pinergy and close the driver
            Navigations.logOut(driver);
            System.exit(0);
        }
    }



    private static void searchAndExtractAgentInfo(AgentInfo agentInfo,boolean isNavigateToRoster,AtomicInteger counter) {
        recordCounter.incrementAndGet();
        counter.incrementAndGet();

        //Uncomment to gracefully logout of Pinergy
                /*if(recordCounter.get()>10000){
                    logOut(driver);
                }*/

        System.out.println(String.format("INFO: Extracting Info for agent %s", agentInfo.getAgentName()));
        //driver.get(agentInfo.getAllLinksToQuery());

        try {
            if (Utils.scrapeAgentInfo(driver, agentInfo, unknownAgents,officeEmployee)) {
                updatedAgentInfo.add(agentInfo);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Convert boolean to string
     *
     * @param isAgent
     * @return
     */
    private static String getIsAgent(boolean isAgent) {
        return isAgent ? STR_Y : STR_N;
    }

    /**
     * Save processed items to file
     * @param counter
     */
    private static void saveEveryXRecords(AtomicInteger counter,int limit) {
        //Write every 100 records processed to file, don't want to write large chunk to file.
        if (counter.get() == limit) {
            System.out.println(String.format("INFO: Writing %s records to file",limit));

            //Write to file
            Utils.writeToCSVFile(updatedAgentInfo, officeEmployee,unknownAgents);

            //writeToDBSuccessfullLookups(updatedAgentInfo);
            //writeToDBFailedLookups(unknownAgents);

            Utils.clearLists(updatedAgentInfo,officeEmployee, unknownAgents);
            counter.set(0);
        }
    }

/*    private static List<AgentInfo> getAgentsToLookUp() {
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
    }*/
    
/*    private static void writeToDBSuccessfullLookups(List<AgentInfo> updatedAgentInfoList) {
    	insertAgentEmail(updatedAgentInfoList, "PASS");
    }
    
    private static void writeToDBFailedLookups(List<AgentInfo> updatedAgentInfoList) {
    	insertAgentEmail(updatedAgentInfoList, "FAIL");
    }*/
    
    
   /* private static void insertAgentEmail(List<AgentInfo> updatedAgentInfoList, String lookupStatus) {
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
*/

}
