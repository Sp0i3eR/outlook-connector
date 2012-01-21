package ru.hflabs.oss;

import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.JiraSoapServiceServiceLocator;
import org.swift.common.soap.jira.RemoteIssue;
import org.swift.common.soap.jira.RemoteFilter;

/**
 * Jira connection class 
 *
 */
public class JiraConnector 
{
    private JiraSoapServiceServiceLocator jsssl = new JiraSoapServiceServiceLocator();
    private JiraSoapService jss = null;
    private String token = null;
    private static String RPC_ENDPOINT = "/rpc/soap/jirasoapservice-v2";

    public JiraConnector(String server, String user, String password) throws Exception {
        try {
            jsssl.setJirasoapserviceV2EndpointAddress(server + RPC_ENDPOINT);
            jsssl.setMaintainSession(true);
            jss = jsssl.getJirasoapserviceV2();
            token = jss.login(user,password);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getToken() {
        return token;
    }

    public JiraSoapService getJiraSoapService() {
        return jss;
    }





    public static void main( String[] args )
    {
        try {
            String user = System.getProperty("jira.username");
            String pass = System.getProperty("jira.password");
            String host = System.getProperty("jira.hostname");
            JiraConnector jc = new JiraConnector(host,user,pass);
            JiraSoapService jss = jc.getJiraSoapService();
            RemoteFilter[] filters = jss.getFavouriteFilters(jc.getToken());
            for (RemoteFilter filter:filters) {
                RemoteIssue[] issues = jss.getIssuesFromFilterWithLimit(jc.getToken(),filter.getId(),0,10);
                for (RemoteIssue issue:issues) {
                    System.out.println(filter.getName()+";"+filter.getId()+";"+issue.getKey()+";"+issue.getId()+";"+issue.getDescription());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        
    }
}
