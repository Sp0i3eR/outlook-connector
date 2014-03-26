package ru.hflabs.oss;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class OutlookConnector {

    public final Log log =
        LogFactory.getLog(this.getClass());

    public static final int olFolderCalendar = 9;

    public String folderName;
    ArrayList<Appointment> appointmentlist = new ArrayList<Appointment>();

    public OutlookConnector(String folderName) {
        this.folderName = folderName;
    }
    public OutlookConnector() {
        this(null);
    }
    abstract int fetch();
    abstract int fetch(Calendar start, Calendar end);

    

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
