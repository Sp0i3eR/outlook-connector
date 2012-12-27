package ru.hflabs.oss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;


import javax.script.*;

public class ASOutlookConnector extends OutlookConnector{
    private final static String currentFolder = "Calendar";
    private static String calStart = "12/1/2012";
    private static String calEnd = "12/31/2012";
    private static String asFormat ;
    public ASOutlookConnector(String folderName) {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(this
                .getClass().getClassLoader().getResourceAsStream("outlook.applescript")));
        char[] buf = new char[1024];
        int numRead;
        try {
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (IOException e) {
            log.warn(e);
        }
        asFormat = fileData.toString();

    }
    public ASOutlookConnector() {
        this(null);
    }

    public int fetch() {

        String script = String.format(asFormat, calStart, calEnd, currentFolder);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("AppleScript");
        ArrayList<Object> retVal;
        Map<String,Object> appointment= new HashMap<String,Object>();
        try {
            retVal = (ArrayList<Object>)engine.eval(script);
            Integer counter=0;
            Integer evtId=1;
            String evtParam="";
            for (Object obj:retVal) {
                switch (counter % 3) {
                    case 0:
                        if (((Long) obj).intValue()!=evtId) {
                            appointmentlist.add(new Appointment(appointment));
                        }
                        evtId = ((Long) obj).intValue();

                        break;
                    case 1:
                        evtParam = (String) obj;
                        break;
                    case 2:
                        appointment.put(evtParam,obj);
                        break;

                }
                counter++;
            }

        } catch (ScriptException e) {
            log.warn(e);
        }
        int retrieved = appointmentlist.size();
        log.info("Retrieved " + retrieved + " items.");
        return retrieved;
    }

    public int fetch(Calendar start, Calendar end) {
        DateFormat df = new SimpleDateFormat("M/d/yyyy");
        calStart=df.format(start.getTime());
        calEnd=df.format(end.getTime());
        log.info("Restricting result by start date: " + calStart + " and end date: " + calEnd);
        return this.fetch();
    }

    public static void main( String[] args ) {
        OutlookConnector outlookConnector = new ASOutlookConnector();
        outlookConnector.fetch();
    }

    /**
     * Gets the appointmentlist for this instance.
     *
     * @return The appointmentlist.
     */
    public ArrayList<Appointment> getAppointmentlist()
    {
        return this.appointmentlist;
    }

}
