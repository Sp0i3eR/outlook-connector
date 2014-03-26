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

import javax.xml.rpc.ServiceException;

/**
 * Jira connection class 
 *
 */
public class JiraConnector 
{
    private static final Log log =
        LogFactory.getLog(JiraConnector.class);

    private JiraSoapService jss = null;
    private String token = null;


    public JiraConnector() {
        this(null,null,null);
    }

    public JiraConnector(String server, String user, String password) {
        Properties props = new Properties();
        File propertiesFile = new File(System.getProperty("user.dir")+File.separator+"jira.properties");
        log.info("Loading user settings from:" + propertiesFile);
        try {
            props.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException e) {
            log.warn("User preferences not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String RPC_ENDPOINT = "/rpc/soap/jirasoapservice-v2";
        log.debug("Connecting to server: " + (server == null ? props.getProperty("jira.url") : server) + RPC_ENDPOINT);
        JiraSoapServiceServiceLocator jsssl = new JiraSoapServiceServiceLocator();
        jsssl.setJirasoapserviceV2EndpointAddress((server == null ? props.getProperty("jira.url") : server) + RPC_ENDPOINT);
        jsssl.setMaintainSession(true);
        try {
            jss = jsssl.getJirasoapserviceV2();
            token = jss.login((user==null?props.getProperty("jira.username"):user),(password==null?props.getProperty("jira.password"):password));
            log.debug("Connected, got token: " + token);
        } catch (ServiceException e) {
            log.error(e);
        } catch (java.rmi.RemoteException e) {
            log.error(e);
        }
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
            return (jss.addWorklogAndAutoAdjustRemainingEstimate(token, key, wlog) != null);
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

}
