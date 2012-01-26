package ru.hflabs.oss;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jacob.com.Dispatch;

public class Appointment {

    private static final Log log =
        LogFactory.getLog(Appointment.class);

    private String      categories,
            subject,
            body;
    private Calendar    start,
            end;
    private Integer     duration;
    private Boolean     alldayevent,
            isrecurring;
    

    public Appointment(Dispatch appointment) {
        categories = Dispatch.get(appointment,"Categories").toString();
        subject = Dispatch.get(appointment,"Subject").toString();
        body = Dispatch.get(appointment,"Body").toString();
        duration = Dispatch.get(appointment,"Duration").toInt();
        //start.setTime(Dispatch.get(appointment,"Start").toJavaDate());
        //end.setTime(Dispatch.get(appointment,"End").toJavaDate());
        alldayevent = Dispatch.get(appointment,"AllDayEvent").toBoolean();
        isrecurring = Dispatch.get(appointment,"IsRecurring").toBoolean();
        }



    /**
     * Gets the categories for this instance.
     *
     * @return The categories.
     */
    public String getCategories()
    {
        return this.categories;
    }
    /**
     * Sets the categories for this instance.
     *
     * @param categories The categories.
     */
    public void setCategories(String categories)
    {
        this.categories = categories;
    }
    /**
     * Gets the subject for this instance.
     *
     * @return The subject.
     */
    public String getSubject()
    {
        return this.subject;
    }
    /**
     * Sets the subject for this instance.
     *
     * @param subject The subject.
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    /**
     * Gets the body for this instance.
     *
     * @return The body.
     */
    public String getBody()
    {
        return this.body;
    }
    /**
     * Sets the body for this instance.
     *
     * @param body The body.
     */
    public void setBody(String body)
    {
        this.body = body;
    }
    /**
     * Gets the start for this instance.
     *
     * @return The start.
     */
    public Calendar getStart()
    {
        return this.start;
    }
    /**
     * Sets the start for this instance.
     *
     * @param start The start.
     */
    public void setStart(Calendar start)
    {
        this.start = start;
    }
    /**
     * Gets the end for this instance.
     *
     * @return The end.
     */
    public Calendar getEnd()
    {
        return this.end;
    }
    /**
     * Sets the end for this instance.
     *
     * @param end The end.
     */
    public void setEnd(Calendar end)
    {
        this.end = end;
    }
    /**
     * Gets the duration for this instance.
     *
     * @return The duration.
     */
    public Integer getDuration()
    {
        return this.duration;
    }
    /**
     * Sets the duration for this instance.
     *
     * @param duration The duration.
     */
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }
    /**
     * Gets the alldayevent for this instance.
     *
     * @return The alldayevent.
     */
    public Boolean getAlldayevent()
    {
        return this.alldayevent;
    }
    /**
     * Sets the alldayevent for this instance.
     *
     * @param alldayevent The alldayevent.
     */
    public void setAlldayevent(Boolean alldayevent)
    {
        this.alldayevent = alldayevent;
    }
    /**
     * Gets the isrecurring for this instance.
     *
     * @return The isrecurring.
     */
    public Boolean getIsrecurring()
    {
        return this.isrecurring;
    }
    /**
     * Sets the isrecurring for this instance.
     *
     * @param isrecurring The isrecurring.
     */
    public void setIsrecurring(Boolean isrecurring)
    {
        this.isrecurring = isrecurring;
    }
}
