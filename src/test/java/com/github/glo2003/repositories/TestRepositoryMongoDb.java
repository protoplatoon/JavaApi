package com.github.glo2003.repositories;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.glo2003.models.Day;
import com.github.glo2003.models.Schedule;
import com.github.glo2003.models.Staff;
import com.github.glo2003.models.TimeSlot;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestRepositoryMongoDb {

    /**
     * Test la serialisation d'un staff dans un schedule
     */
    @Test
    public void testSerializeSchedule()
    {
        String startOfWeek = "2018-01-01";
        //System.out.println("day : " + startOfWeek);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RepositoryMongoDb repo = new RepositoryMongoDb();
        Calendar calendar = Calendar.getInstance();
        if (date != null)
            calendar.setTime(date);

        System.out.println("calendar" + calendar.getTimeInMillis());
        Schedule schedule = repo.getSchedule(calendar);

        Staff staffTest = new Staff();
        staffTest.setLastName("toto");
        staffTest.setFirstName("titi");
        staffTest.setRole("cooker");

        schedule.getDays().get(0).setDatetime("test");
        schedule.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().add(staffTest);

        repo.saveSchedule(schedule);
        //System.out.println("calendar " + calendar.getTimeInMillis());
        Schedule schedule2 = repo.getSchedule(calendar);

        repo.selectCollection("calendar" + calendar.getTimeInMillis());
        repo.dropCollection();

        Assert.assertEquals(schedule2.getDays().get(0).getDatetime(), schedule.getDays().get(0).getDatetime());
        Assert.assertEquals(schedule2.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().get(0).getFirstName(), staffTest.getFirstName());
        Assert.assertEquals(schedule2.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().get(0).getLastName(), staffTest.getLastName());
        Assert.assertEquals(schedule2.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().get(0).getRole(), staffTest.getRole());
    }

    /**
     * Test la serialisation de deux fois le meme staff dans un schedule
     */
    @Test
    public void testTwoStaffInSameSlot()
    {
        String startOfWeek = "2018-01-01";
        //System.out.println("day : " + startOfWeek);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RepositoryMongoDb repo = new RepositoryMongoDb();
        Calendar calendar = Calendar.getInstance();
        if (date != null)
            calendar.setTime(date);

        //System.out.println("calendar" + calendar.getTimeInMillis());
        Schedule schedule = repo.getSchedule(calendar);

        Staff staffTest = new Staff();
        staffTest.setLastName("toto");
        staffTest.setFirstName("titi");
        staffTest.setRole("cooker");
        staffTest.setTimeSlot(schedule.getDays().get(0).getTimeSlots().get(0).getDatetime());

        Assert.assertEquals(repo.saveStaff(staffTest, schedule), true);
        Assert.assertEquals(repo.saveStaff(staffTest, schedule), false);

        repo.selectCollection("calendar" + calendar.getTimeInMillis());
        repo.dropCollection();
    }

    /**
     * Test la suppression d'un staff dans un schedule
     */
    @Test
    public void suppressStaffInSlot()
    {
        String startOfWeek = "2018-01-01";
        //System.out.println("day : " + startOfWeek);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RepositoryMongoDb repo = new RepositoryMongoDb();
        Calendar calendar = Calendar.getInstance();
        if (date != null)
            calendar.setTime(date);

        //System.out.println("calendar" + calendar.getTimeInMillis());
        Schedule schedule = repo.getSchedule(calendar);

        Staff staffTest = new Staff();
        staffTest.setLastName("toto");
        staffTest.setFirstName("titi");
        staffTest.setRole("cooker");
        staffTest.setTimeSlot(schedule.getDays().get(0).getTimeSlots().get(0).getDatetime());

        Assert.assertEquals(repo.saveStaff(staffTest, schedule), true);
        schedule = repo.getSchedule(calendar);
        Assert.assertEquals(schedule.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().size(), 1);
        Assert.assertEquals(repo.removeStaff(staffTest, schedule), true);
        schedule = repo.getSchedule(calendar);
        Assert.assertEquals(schedule.getDays().get(0).getTimeSlots().get(0).getScheduledStaffs().size(), 0);

        repo.selectCollection("calendar" + calendar.getTimeInMillis());
        repo.dropCollection();
    }

    /**
     * Test la modification d'un staff dans un schedule
     */
    @Test
    public void modifStaffInSlot()
    {
        String startOfWeek = "2018-04-01";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(startOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        RepositoryMongoDb repo = new RepositoryMongoDb();
        Calendar calendar = Calendar.getInstance();
        if (date != null)
            calendar.setTime(date);
        Schedule schedule = repo.getSchedule(calendar);

        Staff staffTest = new Staff();

        staffTest.setFirstName("ta");
        staffTest.setLastName("to");
        staffTest.setRole("cuisto");
        staffTest.setTimeSlot(schedule.getDays().get(0).getTimeSlots().get(0).getDatetime());

        Assert.assertEquals(repo.saveStaff(staffTest, schedule), true);
        schedule = repo.getSchedule(calendar);
        String tmpFirstName = "za";
        String tmpLastName = "zo";
        Assert.assertEquals(repo.modifyStaff(staffTest.getFirstName(), staffTest.getLastName(), tmpFirstName, tmpLastName, "server", staffTest.getTimeSlot(), schedule), true);

        for (Day day:schedule.getDays())
             for (TimeSlot t : day.getTimeSlots())
                if (t.getDatetime().equals(staffTest.getTimeSlot()))
                    for (Staff staff : t.getScheduledStaffs())
                        if (staff.getFirstName().equals(tmpFirstName)
                                && staff.getLastName().equals(tmpLastName)) {
                            System.out.println("User trouvé ! (doit être trouvé)");
                            Assert.assertTrue(true);
                        }

        for (Day day:schedule.getDays())
            for (TimeSlot t : day.getTimeSlots())
                if (t.getDatetime().equals(staffTest.getTimeSlot()))
                    for (Staff staff : t.getScheduledStaffs())
                        if (staff.getFirstName().equals(staffTest.getFirstName())
                                && staff.getLastName().equals(staffTest.getLastName())) {
                            System.out.println("User trouvé ! (ne doit pas être trouvé)");
                            Assert.assertTrue(false);
                        }

        repo.selectCollection("calendar" + calendar.getTimeInMillis());
        repo.dropCollection();
    }
}
