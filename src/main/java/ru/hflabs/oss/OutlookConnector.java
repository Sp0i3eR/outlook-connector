package ru.hflabs.oss;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jacob.activeX.ActiveXComponent;

import com.jacob.com.Dispatch;

public class OutlookConnector {

    private static final Log log =
        LogFactory.getLog(OutlookConnector.class);

    private static final int olFolderCalendar = 9;

    private ActiveXComponent axc;
    ArrayList<Appointment> appointmentlist = new ArrayList<Appointment>();
    private Dispatch outlook;
    private Dispatch ns;
    private Dispatch folder;

    public OutlookConnector() {
        axc = new ActiveXComponent("Outlook.Application");
        log.info("Outlook ActiveX version: " + axc.getProperty("Version"));
        outlook = axc.getObject();
        log.info("Got Outlook version: " + Dispatch.get(outlook,"Version"));
        ns = axc.getProperty("Session").toDispatch();
        folder = Dispatch.call(ns,"GetDefaultFolder",olFolderCalendar).toDispatch();
    }

    public int fetch() {
        Dispatch items = Dispatch.get(folder,"Items").toDispatch();
        int count = Dispatch.call(items,"Count").toInt();

        log.info("Got " + Integer.toString(count) + " items ");
        for (int i=1;i<=count;i++) 
            appointmentlist.add(new Appointment(Dispatch.call(items,"Item", i).toDispatch()));
        return count;
    }



    public static void main( String[] args ) {
        OutlookConnector oc = new OutlookConnector();
        System.out.println("Fetched " + Integer.toString(oc.fetch()) + " items.");
        for (Appointment appointment:oc.getAppointmentlist())
            System.out.println(appointment.getSubject());
        System.out.println("Done.");
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
