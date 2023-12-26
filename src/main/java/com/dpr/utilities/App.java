package com.dpr.utilities;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

public class App {

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        
        try {
	        HtmlPage htmlPage = webClient.getPage("https://h3r.mlspin.com/signin.asp#ath");
	        System.out.println(htmlPage.getTitleText());
	        
	        final HtmlForm form = htmlPage.getFormByName("loginform");
	        HtmlTextInput userNameField = form.getInputByName("user_name");
	        userNameField.setValueAttribute("");
	        HtmlPasswordInput passField = form.getInputByName("pass");
	        passField.setValueAttribute("");
	        
	        DomElement button = htmlPage.getFirstByXPath("//button[@type='submit']");
	        HtmlPage new_page = button.click(); 
	        //System.out.println(new_page.get
	        System.out.println(new_page.getWebResponse().getContentAsString());
	        
	       // HtmlPage htmlPage1 = webClient.getPage("https://h3m.mlspin.com/tools/roster/agent.asp?aid=CN205418&nomenu=");        
	        //System.out.println(htmlPage1.getWebResponse().getContentAsString());
        } catch ( Exception e ) {
        	e.printStackTrace(System.out);
        } finally {
        	webClient.close();	
        }
        
        
        
        
        
    }
}

