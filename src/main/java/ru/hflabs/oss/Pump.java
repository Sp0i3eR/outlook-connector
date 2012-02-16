package ru.hflabs.oss;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Pump {
    private static final Log log =
        LogFactory.getLog(Pump.class);
    private OutlookConnector oc;
    private JiraConnector jc;
    private Map<String,String> categoryMapper = new HashMap<String,String>();
    private Map<Appointment,String> appointmentMapper = new HashMap<Appointment,String>();
    private List<Appointment> missedTasks = new ArrayList<Appointment>();
    private String calendarName;
    private static String taskRegex = "[A-Z]+-[0-9]+";

    public Pump(){
        File propertiesFile = new File(System.getProperty("user.dir")+File.separator+"pump.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(propertiesFile);
            log.info("Loading user settings from:" + propertiesFile);
            Element e = d.getDocumentElement();
            e.normalize();
            if ("pump".equals(e.getNodeName())) {
                calendarName = e.getElementsByTagName("calendar").item(0).getAttributes().getNamedItem("folder").getTextContent();
                log.debug((calendarName==null?"Using default calendar":"Using calendar " + calendarName));
                NodeList nl = e.getElementsByTagName("category");
                for (int i=0;i<nl.getLength();i++) {
                    Node node = nl.item(i);
                    String categoryName = node.getAttributes().getNamedItem("name").getTextContent();
                    String categoryTask = node.getAttributes().getNamedItem("task").getTextContent();
                    log.debug("Mapping \"" + categoryName + "\" => " + categoryTask);
                    categoryMapper.put(categoryName,categoryTask);
                }
            }
            log.info("Connecting to outlook...");
            oc = new OutlookConnector(calendarName);
            log.info("Done.");
            log.info("Connecting to JIRA instance...");
            jc = new JiraConnector();
            log.info("Done.");

        } catch (ParserConfigurationException e) {
            log.error(e);
        } catch (SAXException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String getBestTaskVariant(Appointment appointment) {
        Pattern p = Pattern.compile(taskRegex);
        Matcher m = p.matcher(appointment.getSubject());

        while (m.find()) {
            String candidate = m.group();
            if (jc.issueExist(candidate))
                return candidate;
        }
        String candidate = categoryMapper.get(appointment.getCategories());
        if (jc.issueExist(candidate)) 
            return candidate;

        return null;

    }
    public void map(Calendar start,Calendar end) {
        appointmentMapper.clear();
        oc.fetch(start,end);
        for (Appointment appointment:oc.getAppointmentlist()) {
            String task = getBestTaskVariant(appointment);
            log.debug(appointment.getSubject() + " => " + task);
            if (task!=null)
                appointmentMapper.put(appointment,task);
            else 
                missedTasks.add(appointment);
        }
    }
    public void pretend() {
        Formatter f = new Formatter(System.out);
        f.format("%40s | %10s | %6s | %10s","Subject","Date","Time","Task");
        for (Appointment appt:appointmentMapper.keySet()) {
            try {
                f.format("%1$40s | %2$2td.%2$2tm.%2$tY | %3$5dm | %4$10s\n",new String(appt.getSubject().getBytes("cp866"),"cp1251"),appt.getStart().getTime(),appt.getDuration(),appointmentMapper.get(appt));
            } catch (UnsupportedEncodingException e) {
                log.warn(e);
            }
        }
        f.format("------Not mapped tasks-----");
        for (Appointment appt:missedTasks) {
            try {
                f.format("%1$40s | %2$2td.%2$2tm.%2$tY | %3$5dm\n",new String(appt.getSubject().getBytes("cp866"),"cp1251"),appt.getStart().getTime(),appt.getDuration());
            } catch (UnsupportedEncodingException e) {
                log.warn(e);
            }
        }
    }
    public void push() {
        for (Appointment appt:appointmentMapper.keySet()) {
            jc.logWorkAgainstIssueById(appointmentMapper.get(appt),appt.getStart(),appt.getEnd(),appt.getSubject()); //+ "\n" + appt.getBody());
        }
    }
    public static void main(String[] args) {
        if (args.length<2) {
            System.err.println("Wrong arguments");
            System.out.println("Usage:    pump from-date to-date");
            System.out.println("Example:  pump 21.12.2011 30.12.2011");
            return;
        }

        Pump pump = new Pump();
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        try {
            start.setTime(df.parse(args[0]));
            end.setTime(df.parse(args[1]));
            end.add(GregorianCalendar.DAY_OF_MONTH,1);
            end.add(GregorianCalendar.SECOND,-1);
        } catch (ParseException e) {
            log.error(e);
        }
        pump.map(start,end);
        pump.pretend();
        String agree = System.console().readLine("Is this ^ ok? [y/N] ");
        if (agree.matches("^[Yy][Ee]?[Ss]?"))
            pump.push();



    }


}
