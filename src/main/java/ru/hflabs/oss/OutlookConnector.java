package ru.hflabs.oss;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jacob.activeX.ActiveXComponent;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class OutlookConnector {

    private static final Log log =
        LogFactory.getLog(OutlookConnector.class);

    private static final int olFolderCalendar = 9;

    private ActiveXComponent axc;
    ArrayList<Appointment> appointmentlist = new ArrayList<Appointment>();
    private Dispatch outlook;
    private Dispatch ns;
    private Dispatch folder;
    private Dispatch items;

    public OutlookConnector() {
        axc = new ActiveXComponent("Outlook.Application");
        log.info("Outlook ActiveX version: " + axc.getProperty("Version"));
        outlook = axc.getObject();
        log.info("Got Outlook version: " + Dispatch.get(outlook,"Version"));
        ns = axc.getProperty("Session").toDispatch();
        folder = Dispatch.call(ns,"GetDefaultFolder",olFolderCalendar).toDispatch();
        items = Dispatch.get(folder,"Items").toDispatch();
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
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
        DateFormat df = new SimpleDateFormat("MMMMMMMMM dd, yyyy h:mm a");
        String filter = new String("[Start] >= \"" + df.format(start.getTime()) + "\" AND [End] <= \"" + df.format(end.getTime()) + "\"");
        Dispatch.call(items,"Sort","[Start]");
        Dispatch.put(items,"IncludeRecurrences",new Boolean(true));
        log.info("Restricting result by statement: " + filter); 
        items = Dispatch.call(items,"Restrict",filter).toDispatch(); 
        return this.fetch();
    }

/*    private Dispatch listDispatchProperties(Dispatch item) {
        Dispatch itemProperties = Dispatch.get(item,"ItemProperties").toDispatch();
        int count = Dispatch.call(itemProperties,"Count").getInt();

        log.debug("Listing " + Integer.toString(count) + " properties");

        for (int i=0;i<count;i++){
            Dispatch property = Dispatch.call(itemProperties,"Item",i).toDispatch();
            log.debug(Dispatch.get(property,"Name").toString() + " = " + Dispatch.get(property,"Value"));
        }
        return item;
    }
*/


    public static void main( String[] args ) {
        OutlookConnector oc = new OutlookConnector();
        System.out.println("Fetched " + Integer.toString(oc.fetch(new GregorianCalendar(2012,0,30),new GregorianCalendar(2012,1,15))) + " items.");
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
