package com.github.glo2003.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

public class TimeSlot implements Serializable {

    private String datetime = "";

    private List<Staff> scheduledStaffs = new ArrayList<>();

    static final Logger logger = Logger.getLogger(TimeSlot.class);

    // pour la serialisation il faut un constructor par default
    public TimeSlot() {

    }

    public TimeSlot(Day d, String hours, String minutes, String seconds) {
        this.datetime = String.format("%s-%s-%sT%s:%s:%s", d.getYear(), d.getMonth(), d.getDay()
                , hours, minutes, seconds);
        // list d'employé empty par default
        //System.out.println("timeSlot : " + this.datetime);
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

    public List<Staff> getScheduledStaffs() {
        return scheduledStaffs;
    }

    public String getDatetime() {
        return datetime;
    }
}
