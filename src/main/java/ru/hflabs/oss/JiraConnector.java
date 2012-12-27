package ru.hflabs.oss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.JiraSoapServiceServiceLocator;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemoteException;
import org.swift.common.soap.jira.RemotePermissionException;
import org.swift.common.soap.jira.RemoteWorklog;

/**
 * Jira connection class 
 *
 */
public class JiraConnector 
{
    private static final Log log =
        LogFactory.getLog(JiraConnector.class);

    private JiraSoapServiceServiceLocator jsssl = new JiraSoapServiceServiceLocator();
    private JiraSoapService jss = null;
    private String token = null;
    private static String RPC_ENDPOINT = "/rpc/soap/jirasoapservice-v2";

    private boolean init(String server, String user, String password) {
        try {
            log.debug("Connecting to server: "+ server + RPC_ENDPOINT);
            jsssl.setJirasoapserviceV2EndpointAddress(server + RPC_ENDPOINT);
            jsssl.setMaintainSession(true);
            jss = jsssl.getJirasoapserviceV2();
            token = jss.login(user,password);
            log.debug("Connected, got token: " + token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JiraConnector() {
        Properties props = new Properties();
        File propertiesFile = new File(System.getProperty("user.dir")+File.separator+"jira.properties");
        log.info("Loading user settings from:" + propertiesFile);
        try {
            props.load(new FileInputStream(propertiesFile));
            init(props.getProperty("jira.url"),props.getProperty("jira.username"),props.getProperty("jira.password"));
        } catch (FileNotFoundException e) {
            log.warn("User preferences not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JiraConnector(String server, String user, String password) {
        init(server,user,password);
    }

    public String getToken() {
        return token;
    }

    public JiraSoapService getJiraSoapService() {
        return jss;
    }

    private String DateIntervalToJira(Calendar start, Calendar end) {
        log.debug("delta = " + end.getTimeInMillis() + " - " + start.getTimeInMillis());
        long delta=(end.getTimeInMillis() - start.getTimeInMillis())/(60*1000);
        return Long.toString(delta) + "m";
    }

    public boolean issueExist(String key) {
        try {
            jss.getIssue(token,key);
        } catch (RemotePermissionException e) {
            return false;
        } catch (RemoteAuthenticationException e) {
            log.debug(e);
            return false;
        } catch (RemoteException e) {
            log.debug(e);
            return false;
        } catch (java.rmi.RemoteException e) {
            log.debug(e);
            return false;
        }
        return true;
    }


    public boolean logWorkAgainstIssueById(String key,Calendar start,String delta,String comment) {
        try {
            RemoteWorklog wlog = new RemoteWorklog();
            log.debug("Start = " + start);
            log.debug("Delta = " + delta);
            log.debug("Comment = " + comment);
            wlog.setStartDate(start);
            wlog.setTimeSpent(delta);
            wlog.setComment(comment);
            return (jss.addWorklogAndAutoAdjustRemainingEstimate(token,key,wlog)==null?false:true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logWorkAgainstIssueById(String key,Calendar start,Calendar end,String comment) {
        return logWorkAgainstIssueById(key,start,DateIntervalToJira(start,end),comment);
    }

    public boolean logWorkAgainstIssueById(String key,Calendar start,Calendar end) {
        return logWorkAgainstIssueById(key,start,DateIntervalToJira(start,end),"");
    }

    public boolean logWorkAgainstIssueById(String key,Calendar start,String delta) {
        return logWorkAgainstIssueById(key,start,delta,"");
    }

    public boolean logWorkAgainstIssueById(String key,String delta) {
        return logWorkAgainstIssueById(key,Calendar.getInstance(),delta,"");
    }
    public boolean logWorkAgainstIssueById(String key,String delta,String comment) {
        return logWorkAgainstIssueById(key,Calendar.getInstance(),delta,comment);
    }

    public static void main( String[] args )
    {
        JiraConnector jc = new JiraConnector();
        System.out.println("Issue "+ args[0] + (jc.issueExist(args[0])?" exist":" don't exist"));
    }
}
