package ru.hflabs.oss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Pump {
    private static final Log log =
        LogFactory.getLog(Pump.class);
    private OutlookConnector oc;
    private JiraConnector jc;
    private HashMap<String,String> categoryMapper = new HashMap<String,String>();
    private HashMap<Appointment,String> appointmentMapper = new HashMap<Appointment,String>();
    private String calendarName;
    private static String categoryRegex = "^(outlook\\.category\\.[0-9]+)\\.name$";
    private static String taskRegex = "[A-Z]+-[0-9]+";

    public Pump(){
        Properties properties = new Properties();
        File propertiesFile = new File(System.getProperty("user.home")+File.separator+"pump.properties");
        
        log.info("Loading user settings from:" + propertiesFile);
        try {
            properties.load(new FileInputStream(propertiesFile));
            calendarName = properties.getProperty("outlook.calendar");
            for (String key:properties.stringPropertyNames()) {
                    if (key.matches(categoryRegex)) {
                        String categoryName = properties.getProperty(key);
                        String categoryTask = properties.getProperty(key.replaceFirst(categoryRegex,"\1.task"));
                        log.debug("Mapping " + categoryName + " => " + categoryTask);
                        categoryMapper.put(categoryName,categoryTask);
                    }
                }


        } catch (FileNotFoundException e) {
            log.error("User preferences not found");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBestTaskVariant(Appointment appointment) {
        ArrayList<String> candidates = new ArrayList<String>();
        Pattern p = Pattern.compile(taskRegex);
        Matcher m = p.matcher(appointment.getSubject());
        while (m.find()) candidates.add(m.group());
        m = p.matcher(appointment.getBody());
        while (m.find()) candidates.add(m.group());
        candidates.add(categoryMapper.get(appointment.getCategories()));
        while (!candidates.isEmpty()) {
            String candidate = candidates.remove(0);
            if (jc.issueExist(candidate))
                return candidate;
        }
        return null;

    }


}
