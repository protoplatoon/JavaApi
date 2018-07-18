package com.github.glo2003.handlers;

import com.github.glo2003.models.Schedule;
import com.github.glo2003.repositories.RepositoryMongoDb;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StaffListHandler implements Route {

    static final Logger logger = Logger.getLogger(StaffHandler.class);

    RepositoryMongoDb repo = new RepositoryMongoDb();

    @Override
    public Object handle(Request request, Response response)
    {
        String startOfWeek = request.params(":startOfWeek");
        //System.out.println("day : " + startOfWeek);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            logger.error(e);
            return "Invalid param";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Schedule res = repo.getSchedule(calendar);
        //System.out.println("day : " + calendar.getTime());
        return res.getStaffList();
    }

}
