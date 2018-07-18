package com.github.glo2003.handlers;

import com.github.glo2003.repositories.RepositoryMongoDb;
import com.github.glo2003.models.Schedule;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ModifyStaffHandler implements Route {
    static final Logger logger = Logger.getLogger(RemoveStaffHandler.class);

    RepositoryMongoDb repo = new RepositoryMongoDb();

    @Override
    public Object handle(Request request, Response response)
    {
        String startOfWeek = request.params(":startOfWeek");
        // actuellement on nous envoie le jour aulequel on veut supprimer un employer
        String modifiedDay = startOfWeek;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            logger.error(e);
            return "Invalid param";
        }
        // donc je doit retrouver le jour de debut de semaine a partir du ModifiedDay qui peut etre n'importe quelle jour
        // sachant que la semaine commence le dimanche
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // pour info 1 correspond a DIMANCHE
        while (dayOfWeek != 1) {
            // recule le jour de la semaine avec add ^^
            calendar.add(Calendar.DATE, -1);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        }
        // recupere le calendrier mais pour ca il fallait le calendar correct
        Schedule res = repo.getSchedule(calendar);


        String  firstName = request.params(":firstName");
        String  lastName = request.params(":lastName");
        String  newFirstName = request.params(":newFirstName");
        String  newLastName = request.params(":newLastName");
        String  newRole = request.params(":newRole");
        String  timeSlot = request.params(":timeSlot");

        // timeslot => 17-00 doit etre transformer en startOfWeekT17:00:00.000
        if (timeSlot != null && timeSlot.length() > 2) {
            String hour = timeSlot.substring(0, 2);
            String min = ":00:00.000";
            if (timeSlot.length() == 5) {
                min = timeSlot.substring(3, 5);
            }
            timeSlot = modifiedDay + "T" + hour + ":" + min + ":00.000";
            logger.info("try to modify " + firstName + " in timeslot : " + timeSlot);
        }
        if (repo.modifyStaff(firstName, lastName, newFirstName, newLastName, newRole, timeSlot, res))
            return "modify  Ok";
        logger.error("modify failed");
        response.status(400);
        return "modify failed";
    }
}
