package com.github.glo2003.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Day implements Serializable {

    static final Logger logger = Logger.getLogger(Day.class);

    private transient String StringFormat = "%sT00:00:00.000";

    private transient String Day;

    private transient String Month;

    private transient String Year;

    private Timestamp time;

    private String datetime = "";

    private List<TimeSlot> timeSlots = new ArrayList<>();

    // pour la serialisation il faut un constructor par default
    public Day() {

    }

    public Day(String day, String month, String year) {
        Day = day;
        Month = month;
        Year = year;
        //System.out.println(Day);
        if (Month.length() < 2)
            Month = "0" + Month;
        if (Day.length() < 2)
            Day = "0" + Day;
        datetime = String.format(StringFormat, Year + "-" + Month + "-" + Day);
        for (int i = 0; i < 12; i++)
            this.timeSlots.add(new TimeSlot(this, String.valueOf(11 + i), "00", "000"));
    }

    public Day(Calendar calendar) {
        this.Year = String.valueOf(calendar.get(Calendar.YEAR));
        this.Month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        this.Day = String.valueOf(calendar.get(Calendar.DATE));
        //datetime = toString();
        if (Month.length() < 2)
            Month = "0" + Month;
        if (Day.length() < 2)
            Day = "0" + Day;
        datetime = String.format(StringFormat, Year + "-" + Month + "-" + Day);

        //System.out.println("dateTime : " + datetime);
        for (int i = 0; i < 12; i++)
            this.timeSlots.add(new TimeSlot(this, String.valueOf(11 + i), "00", "00.000"));
    }

 /*   @Override
    public String toString() {
        if (Month.length() < 2)
            Month = "0" + Month;
        if (Day.length() < 2)
            Day = "0" + Day;
        return String.format(StringFormat, Year + "-" + Month + "-" + Day);
    }
*/

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public String getDay() {
        return Day;
    }

    public String getMonth() {
        return Month;
    }

    public String getYear() {
        return Year;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();

        String jsonString = "";
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        return jsonString;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
