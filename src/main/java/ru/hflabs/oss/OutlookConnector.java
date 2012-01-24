package ru.hflabs.oss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jacob.activeX.ActiveXComponent;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;

public class OutlookConnector {

    private static final Log log =
        LogFactory.getLog(OutlookConnector.class);
    private static final int olFolderCalendar = 9;

    private ActiveXComponent axc;
    private Dispatch outlook;
    private Dispatch ns;
    private Dispatch fldr;
    private Dispatch appts;
    public OutlookConnector() {
        axc = new ActiveXComponent("Outlook.Application");
        log.info("Outlook ActiveX version: " + axc.getProperty("Version"));
        outlook = axc.getObject();
        log.info("Got Outlook version: " + Dispatch.get(outlook,"Version"));
        ns = axc.getProperty("Session").toDispatch();
        //ns = Dispatch.call(outlook,"GetNamespace","MAPI").toDispatch();
        //log.info("Got namespace: " + ns);
        fldr = Dispatch.call(ns,"GetDefaultFolder",olFolderCalendar).toDispatch();
        appts =  Dispatch.get(fldr,"Items").toDispatch();
        log.info("Got item: " + appts.toString());
        //log.info("Got item: " + Dispatch.get(appt,"Name"));



    }

    public static void main( String[] args ) {
            OutlookConnector oc = new OutlookConnector();
        System.out.println("Done.");
    }

}
