package com.dpr.utilities;

import java.util.StringJoiner;

public class AgentInfo {

    private String agentName;
    private String agentId;
    private String allLinksToQuery;
    private String emailId;
    private String manager;
    private String phoneNumber;
    private String companyName;
    private String officeId;
    private boolean isAgent;
    private String exception;

    public String getException() {
        return exception;
    }

    public AgentInfo setException(String exception) {
        this.exception = String.format("Exception, verify again - %s", exception);
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AgentInfo setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public boolean isAgent() {
        return isAgent;
    }

    public void setAgent(boolean agent) {
        isAgent = agent;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAllLinksToQuery() {
        return allLinksToQuery;
    }

    public void setAllLinksToQuery(String allLinksToQuery) {
        this.allLinksToQuery = allLinksToQuery;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(agentName);
        joiner.add(agentId);
        joiner.add(allLinksToQuery);
        joiner.add(emailId);
        joiner.add(phoneNumber);
        joiner.add(companyName);
        joiner.add(officeId);
        joiner.add(isAgent?"Y":"N");
        joiner.add(exception);
        return joiner.toString();
    }
}