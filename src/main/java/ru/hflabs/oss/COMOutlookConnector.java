package ru.hflabs.oss;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;


import com.jacob.activeX.ActiveXComponent;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class COMOutlookConnector extends OutlookConnector{


    private Dispatch items;

    public COMOutlookConnector() {
        this(null);
    }
    public COMOutlookConnector(String folderName) {
        ActiveXComponent axc = new ActiveXComponent("Outlook.Application");
        log.info("Outlook ActiveX version: " + axc.getProperty("Version"));
        Dispatch outlook = axc.getObject();
        log.info("Got Outlook version: " + Dispatch.get(outlook,"Version"));
        Dispatch ns = axc.getProperty("Session").toDispatch();
        Dispatch currentFolder = Dispatch.call(ns, "GetDefaultFolder", olFolderCalendar).toDispatch();
        if (folderName!=null) currentFolder = findFolder(folderName, currentFolder);
        items = Dispatch.get(currentFolder,"Items").toDispatch();
    }
    private Dispatch findFolder(String folderName,Dispatch folder) {
        Variant folders = Dispatch.call(folder,"Folders");
        if (!folders.isNull()) {
            Variant item = Dispatch.call(folders.toDispatch(),"GetFirst");
            while (!item.isNull()) {
                log.debug(Dispatch.get(item.toDispatch(),"FolderPath"));
                if (folderName.equalsIgnoreCase(Dispatch.get(item.toDispatch(),"Name").toString())) return item.toDispatch();
                item = Dispatch.call(folders.toDispatch(),"GetNext");
            }
            return folder;
        } else return folder;
    }
    private void recurseFolders(Dispatch folder) {
        Variant folders = Dispatch.call(folder,"Folders");
        if (!folders.isNull()) {
            Variant item = Dispatch.call(folders.toDispatch(),"GetFirst");
            while (!item.isNull()) {
                log.debug(Dispatch.get(item.toDispatch(),"FolderPath"));
                item = Dispatch.call(folders.toDispatch(),"GetNext");
            }
        }

    }
    public int fetch() {
        Variant item = Dispatch.call(items,"GetFirst");
        int retrieved = 0;
        while (!item.isNull()) {
            appointmentlist.add(new Appointment(item.toDispatch()));
            item = Dispatch.call(items,"GetNext");
            retrieved++;
        }
        log.info("Retrieved " + retrieved + " items.");
        return retrieved;
    }

    public int fetch(Calendar start, Calendar end) {
        DateFormat df = new SimpleDateFormat("MMMMMMMMM dd, yyyy h:mm a");
        String filter = new String("[Start] >= \"" + df.format(start.getTime()) + "\" AND [End] <= \"" + df.format(end.getTime()) + "\"");
        Dispatch.call(items,"Sort","[Start]");
        Dispatch.put(items,"IncludeRecurrences",new Boolean(true));
        log.info("Restricting result by statement: " + filter); 
        items = Dispatch.call(items,"Restrict",filter).toDispatch(); 
        return this.fetch();
    }

    public static void main( String[] args ) {
        OutlookConnector oc = new COMOutlookConnector("тест");
        System.out.println("Fetched " + Integer.toString(oc.fetch(new GregorianCalendar(2012,0,30),new GregorianCalendar(2012,1,15))) + " items.");
        for (Appointment appointment:oc.getAppointmentlist())
            System.out.println(appointment.getSubject());
        System.out.println("Done.");
    }

}
