package ru.hflabs.oss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jacob.activeX.ActiveXComponent;

import com.jacob.com.Dispatch;

public class OutlookConnector {

    private static final Log log =
        LogFactory.getLog(OutlookConnector.class);

    private ActiveXComponent axc;
    private Dispatch outlook;
    private Dispatch ns;

    public OutlookConnector() {
        axc = new ActiveXComponent("Outlook.Application");
        log.info("Outlook ActiveX version: " + axc.getProperty("Version"));
        outlook = axc.getObject();
        log.info("Got Outlook version: " + Dispatch.get(outlook,"Version"));
        ns = axc.getProperty("Session").toDispatch();
        log.info("Got namespace: " + ns);
    }

}
