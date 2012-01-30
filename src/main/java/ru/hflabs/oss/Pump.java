package ru.hflabs.oss;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

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
    private HashMap<String,String> categoryMapper = new HashMap<String,String>();
    private HashMap<Appointment,String> appointmentMapper = new HashMap<Appointment,String>();
    private String calendarName;
    private static String taskRegex = "[A-Z]+-[0-9]+";

    public Pump(){
        File propertiesFile = new File(System.getProperty("user.home")+File.separator+"pump.xml");
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
            oc = new OutlookConnector(calendarName);
            jc = new JiraConnector();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBestTaskVariant(Appointment appointment) {
        ArrayList<String> candidates = new ArrayList<String>();
        Pattern p = Pattern.compile(taskRegex);

        Matcher m = p.matcher(appointment.getSubject());
        while (m.find()) {
            candidates.add(m.group());
        }

        m = p.matcher(appointment.getBody());
        while (m.find()) {
            candidates.add(m.group());
        }
        candidates.add(categoryMapper.get(appointment.getCategories()));

        while (!candidates.isEmpty()) {
            String candidate = candidates.remove(0);
            if (jc.issueExist(candidate))
                return candidate;
        }
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
        }
    }
    public void pretend() {
        Formatter f = new Formatter(System.out);
        for (Appointment appt:appointmentMapper.keySet()) {
            f.format("%1$40s | %2$2td.%2$2tm.%2$tY | %3$5dm | %4$10s\n",appt.getSubject(),appt.getStart().getTime(),appt.getDuration(),appointmentMapper.get(appt));
        }
    }
    public void push() {
        for (Appointment appt:appointmentMapper.keySet()) {
            jc.logWorkAgainstIssueById(appointmentMapper.get(appt),appt.getStart(),appt.getEnd(),appt.getSubject() + "\n" + appt.getBody());
        }
    }
    public static void main(String[] args) {
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
            e.printStackTrace();
        }
        pump.map(start,end);
        pump.pretend();
        String agree = System.console().readLine("Is this ^ ok?");
        if (agree.matches("^[Yy][Ee]?[Ss]?"))
            pump.push();



    }


}
