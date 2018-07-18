package com.github.glo2003.cucumber;

import com.github.glo2003.models.Schedule;
import com.github.glo2003.models.Staff;
import com.github.glo2003.repositories.RepositoryMongoDb;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import java.util.Calendar;

public class TestListStaff {

    Schedule schedule;
    private Calendar tmpCalendar;
    RepositoryMongoDb repo;
    private int nbStaff;

    @Given("^there are (\\d+) staff in a schedule$")
    public void thereAreStaff(int nbStaff) {
        this.tmpCalendar = Calendar.getInstance();
        this.repo = new RepositoryMongoDb();
        this.schedule = repo.getSchedule(tmpCalendar);
        this.nbStaff = nbStaff;
        
        Staff doublon = new Staff();
        doublon.setFirstName(Integer.toString(0));
        doublon.setLastName(Integer.toString(0));
        doublon.setTimeSlot(schedule.getDays().get(0).getTimeSlots().get(1).getDatetime());
        Assert.assertEquals(repo.saveStaff(doublon, this.schedule), true);

        for (int i = 0; i < nbStaff; i++) {
            Staff a = new Staff();
            a.setFirstName(Integer.toString(i));
            a.setLastName(Integer.toString(i));
            a.setTimeSlot(schedule.getDays().get(0).getTimeSlots().get(0).getDatetime());
            Assert.assertEquals(repo.saveStaff(a, this.schedule), true);
        }
    }

    @When("^I get the schedule list of staff$")
    public void iGetTheScheduleListOfStaff() {
        this.schedule = repo.getSchedule(tmpCalendar);
    }

    @Then("^I should have (\\d+) staff in my list$")
    public void iShouldHaveStaffInMyList(int nbStaff) {
        System.out.println("nb staff : " + this.schedule.getStaffList().size());
        Assert.assertEquals(schedule.getStaffList().size(), nbStaff);
        repo.dropCollection();
    }
}