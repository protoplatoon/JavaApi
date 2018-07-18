package com.github.glo2003.handlers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.glo2003.repositories.RepositoryMongoDb;
import com.github.glo2003.models.Schedule;
import com.github.glo2003.models.Staff;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StaffHandler implements Route
{
    static final Logger logger = Logger.getLogger(StaffHandler.class);

    RepositoryMongoDb repo = new RepositoryMongoDb();

    @Override
    public Object handle(Request request, Response response)
    {
        String startOfWeek = request.params(":startOfWeek");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            logger.error("Send Invalid param", e);
            return "Invalid param";
            //e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Staff newStaff = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            // autorize la conversion d'objet meme si il manque des propriété dans la class
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // essaie de recuperer un new staff depuis le body de la requete (deserialisation json to objet)
            newStaff = mapper.readValue(request.body(), Staff.class);
        } catch (IOException e) {
            logger.error(e);
        }
        Schedule res = repo.getSchedule(calendar);

        if (repo.saveStaff(newStaff, res))
            return "Save Ok";

        response.status(403);
        return "Staff already in database";
    }
}
