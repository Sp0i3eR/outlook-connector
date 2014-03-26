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
        StringBuilder fileData = new StringBuilder(1000);
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
        this.folderName=folderName;

    }
    public ASOutlookConnector() {
        this(currentFolder);
    }

    public int fetch() {

        String script = String.format(asFormat, calStart, calEnd, folderName);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("AppleScript");
        if (engine == null ) {
            log.warn("AppleScript engine not found, trying AppleScriptEngine");
            engine = manager.getEngineByName("AppleScriptEngine");
        }
        if (engine == null) {
            log.error("No usable AppleScript Engine found");
            return 0;
        }

        ArrayList<Object> retVal;
        Map<String,Object> appointment= new HashMap<String,Object>();
        try {
            log.debug("Running applescript" + script);
            retVal = (ArrayList<Object>)engine.eval(script);
            log.debug("Script finished");
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
//        DateFormat df = new SimpleDateFormat("E d MMM yyyy",Locale.US);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
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
