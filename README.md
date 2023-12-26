# Web Scraper for Agent Details
This tool is designed to extract information about agents, such as their phone numbers and emails, from [pinergy](https://h3u.mlspin.com)

## To use this utility:

Place the input file in the ```src/main/resources``` directory within the project.
- Execute the main method in WebScrapper.java after adding the input file.
- The extracted data will be generated in the ```src/main/resources/downloads``` directory. 

The application generates three files as part of the extraction process:

* **AgentInfo_Extracted.csv**: Contains information about agents of interest.
* **AgentInfo_unknown.csv**: Includes details of agents not found during the search. Review this file for exceptions such as:
"Exception verify again - org.openqa.selenium.NoSuchElementException"
This exception might occur due to elements not loading in the browser during the search. Timeouts have been added at various points in the code to wait for the elements to be loaded before accessing them.
* **Office_Employees.csv**: Provides details of agents who are office managers or participants.




