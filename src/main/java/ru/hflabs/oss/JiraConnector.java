package ru.hflabs.oss;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Properties;


/**
 * Jira connection class 
 *
 */
public class JiraConnector 
{
    private static final Log log =
        LogFactory.getLog(JiraConnector.class);
    public static final JerseyJiraRestClientFactory JIRA_FACTORY = new JerseyJiraRestClientFactory();

    private final JiraRestClient rc;
    public static final NullProgressMonitor MONITOR = new NullProgressMonitor();
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
            log.error(e);
        }
        log.debug("Connecting to server: " + (server == null ? props.getProperty("jira.url") : server));

        try {
            rc = JIRA_FACTORY.createWithBasicHttpAuthentication(
                    new URI(server == null ? props.getProperty("jira.url") : server),
                    (user == null ? props.getProperty("jira.username") : user),
                    (password == null ? props.getProperty("jira.password") : password));
            log.debug("Connected, got token: " + rc.getSessionClient().getCurrentSession(MONITOR).getUserUri());
        } catch (URISyntaxException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }


    private int dateIntervalToJira(Calendar start, Calendar end) {
        log.debug("delta = " + end.getTimeInMillis() + " - " + start.getTimeInMillis());
        return (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / (60 * 1000));
    }

    public boolean issueExist(String key) {
        try {
            rc.getIssueClient().getIssue(key, MONITOR);
        } catch (RestClientException e) {
            for (String err : e.getErrorMessages()) {
                System.out.println(key + ":" + err);
            }
            return false;
        }
        return true;

    }


    public boolean logWorkAgainstIssueById(String key, Calendar start, int delta, String comment) {
        try {
            Issue issue = rc.getIssueClient().getIssue(key, MONITOR);
            URI worklogURI = issue.getWorklogUri();
            WorklogInput wlog = new WorklogInputBuilder(worklogURI)
                    .setStartDate(new DateTime(start))
                    .setMinutesSpent(delta)
                    .setComment(comment)
                    .build();
            log.debug("Start = " + start);
            log.debug("Delta = " + delta);
            log.debug("Comment = " + comment);
            rc.getIssueClient().addWorklog(worklogURI, wlog, MONITOR);
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logWorkAgainstIssueById(String key,Calendar start,Calendar end,String comment) {
        return logWorkAgainstIssueById(key, start, dateIntervalToJira(start, end), comment);
    }

    public boolean logWorkAgainstIssueById(String key,Calendar start,Calendar end) {
        return logWorkAgainstIssueById(key, start, dateIntervalToJira(start, end), "");
    }

    public boolean logWorkAgainstIssueById(String key, Calendar start, int delta) {
        return logWorkAgainstIssueById(key,start,delta,"");
    }

    public boolean logWorkAgainstIssueById(String key, int delta) {
        return logWorkAgainstIssueById(key,Calendar.getInstance(),delta,"");
    }

    public boolean logWorkAgainstIssueById(String key, int delta, String comment) {
        return logWorkAgainstIssueById(key,Calendar.getInstance(),delta,comment);
    }

}
