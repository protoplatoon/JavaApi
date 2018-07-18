package com.github.glo2003.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

public class Schedule implements Serializable
{
    @JsonProperty("days")
    private List<Day> days = new ArrayList();

    static final Logger logger = Logger.getLogger(Schedule.class);

    private int id;

    private transient Calendar calendar = null;

    // pour la serialisation il faut un constructor par default
    public Schedule() {
    }

    public Schedule(Calendar calendar) {
        Day myDay;
        this.calendar = calendar;
        for (int i = 0; i < 7; i++) {
            myDay = new Day(calendar);
            days.add(myDay);
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -7);
    }

    public List<Day> getDays() {
        return days;
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

    public void setId(int id) {
        this.id = id;
    }

    public List<Staff> getStaffList() {
        List<Staff> res = new ArrayList<>();

        days.forEach(day -> {
            day.getTimeSlots().forEach(timeSlot -> {
                //System.out.println(timeSlot);
                timeSlot.getScheduledStaffs().forEach(staff -> {
                    // si staff est pas dans res alors on l'ajoute
                    AtomicBoolean alredyInRes = new AtomicBoolean(false);
                    //System.out.println(staff);
                    res.forEach(tmpStaff -> {
                        if (tmpStaff.getFirstName().equals(staff.getFirstName())
                                && tmpStaff.getLastName().equals(staff.getLastName())) {
                            //System.out.println("staff " + staff.getFirstName() + " already in res");
                            alredyInRes.set(true);
                        }
                    });
                    if (!alredyInRes.get()) {
                        //System.out.println("add " + staff.getFirstName());
                        res.add(staff);
                    }
                });
            });
        });
        return res;
    }
}
