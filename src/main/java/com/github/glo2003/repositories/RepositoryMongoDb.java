package com.github.glo2003.repositories;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.glo2003.models.Day;
import com.github.glo2003.models.Schedule;
import com.github.glo2003.models.Staff;
import com.github.glo2003.models.TimeSlot;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class RepositoryMongoDb {

    static final Logger logger = Logger.getLogger(RepositoryMongoDb.class);

    static int id = 0;

    private MongoClient mongo;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    private String nameDB;

    MongoClientURI uri;

    public RepositoryMongoDb() {
        String url = "mongodb://";

        Map<String, String> env = System.getenv();
        this.nameDB = env.get("MONGO_DATABASE");
        url += env.get("MONGO_USER") + ":" + env.get("MONGO_PASSWORD") + "@" + env.get("MONGO_HOST") + ":" + env.get("MONGO_PORT") + "/" + env.get("MONGO_DATABASE");
        if (env.get("MONGO_USER") == null || env.get("MONGO_PASSWORD") == null || env.get("MONGO_HOST") == null || env.get("MONGO_PORT") == null || env.get("MONGO_DATABASE") == null) {
            System.out.println("Set your environment variable for change database, MONGO_USER, MONGO_PASSWORD, MONGO_HOST, MONGO_DATABASE and MONGO_PORT are required");
            this.uri = new MongoClientURI("mongodb://otto:otto@ds239368.mlab.com:39368/glo2003");
        } else
            this.uri = new MongoClientURI(url);
        System.out.println("Connection string : " + this.uri);
        mongo = new MongoClient(this.uri);
        System.out.println("DB NAME : " + nameDB);
        database = mongo.getDatabase(nameDB);
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        database = database.withCodecRegistry(pojoCodecRegistry);
    }

    /*
     ** A Collection is a table in the database
     */
    public void selectCollection(String collectionName) {
        System.out.println("try to select " + collectionName);
        collection = database.getCollection(collectionName);
    }

    public boolean saveSchedule(Schedule schedule) {
        schedule.setId(RepositoryMongoDb.id++);
        if (collection != null && collection.count() > 0
                && schedule.getDays() != null && !schedule.getDays().isEmpty())
            collection.drop();

        // save schedule
        if (collection != null)
            for (Day day:schedule.getDays()) {
                collection.insertOne(Document.parse(day.toString()));
            }
        return true;
    }

    public boolean removeStaff(Staff staff, Schedule schedule) {
        return removeStaff(staff.getFirstName(), staff.getLastName(), staff.getRole(), staff.getTimeSlot(), schedule);
    }

    public boolean removeStaff(String firstName, String lastName, String role, String timeSlot, Schedule schedule) {
        // save schedule
        try {
            for (Day day:schedule.getDays()) {
                for (TimeSlot t : day.getTimeSlots()) {
                    if (t.getDatetime().equals(timeSlot)) {
                        for (Staff staff : t.getScheduledStaffs()) {

                            if (staff.getFirstName().equals(firstName)
                                    && staff.getLastName().equals(lastName)
                                /* && staff.role.equals(role) */) {
                                logger.info("Remove staff " + staff);
                                t.getScheduledStaffs().remove(staff);
                                collection.drop();
                                saveSchedule(schedule);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public boolean modifyStaff(String firstName, String lastName, String newfirstName, String newlastName, String newRole, String timeSlot, Schedule schedule) {
        try {
            for (Day day : schedule.getDays()) {
                for (TimeSlot t : day.getTimeSlots()) {
                    if (t.getDatetime().equals(timeSlot)) {
                        for (Staff staff : t.getScheduledStaffs()) {

                            if (staff.getFirstName().equals(firstName)
                                    && staff.getLastName().equals(lastName)) {
                                logger.info("staff to modify : " + staff);
                                int index = t.getScheduledStaffs().indexOf(staff);
                                Staff tmp = t.getScheduledStaffs().get(index);
                                tmp.setFirstName(newfirstName);
                                tmp.setLastName(newlastName);
                                tmp.setRole(newRole);
                                logger.info("new staff  : " + tmp);
                                t.getScheduledStaffs().set(index, tmp);
                                logger.info("Staff in list :\n" + t.getScheduledStaffs().get(index));
                                collection.drop();
                                saveSchedule(schedule);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public Schedule getSchedule(Calendar calendar) {
        // return un calendrier de la base de données ou create default schedule with new and save in database
        Schedule res = null;
        // selectionne la collection correspondant au calendar passer en parametre de l'url
        selectCollection("calendar" + calendar.getTimeInMillis());

        // get schedule since collection
        if (this.collection != null && this.collection.count() > 0) {
            logger.info("Find a collection ");
            // mettre le calendrier recuperer dans res
            //System.out.println(this.collection);
            res = JsonToDay(this.collection, calendar);
            //res = this.collection.;
        } else {
            // create collection
            try {
                logger.info("Create collection " + "calendar" + calendar.getTimeInMillis());
                database.createCollection("calendar" + calendar.getTimeInMillis());
            } catch (Exception e) {
                logger.error("Error for create collection " + "calendar" + calendar.getTimeInMillis(), e);
                //database.getCollection(calendar.toString()).drop();
            }
            selectCollection("calendar" + calendar.getTimeInMillis());
            res = new Schedule(calendar);
            if (!saveSchedule(res))
                res = null;
        }
        //res = new Schedule(calendar);
        return (res);
    }

    private Schedule JsonToDay(MongoCollection<Document> collection, Calendar calendar) {
        Schedule res = null;

        ObjectMapper mapper = new ObjectMapper();
        if (collection == null)
            return null;
        try {
            FindIterable<Document> docs = collection.find();

            res = new Schedule();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (Document doc : docs) {
                Day day = mapper.readValue(doc.toJson(), Day.class);
                res.getDays().add(day);
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return res;
    }

    public boolean saveStaff(Staff newStaff, Schedule schedule) {
        // save schedule

        // put newStaff in schedule
        // if is possible and edit schedule
        // in database
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //  run the query and print the results out
        FindIterable<Document> docs = collection.find();
        try {
            for (Document doc : docs) {
                Day day = mapper.readValue(doc.toJson(), Day.class);
                // pour chaque docs, if newStaff == un des staff du day
                for (TimeSlot timeSlot : day.getTimeSlots()) {
                    if (timeSlot.getDatetime().equals(newStaff.getTimeSlot())) {
                        for (Staff staff : timeSlot.getScheduledStaffs()) {
                            if (staff.getFirstName().equals(newStaff.getFirstName())
                                    && staff.getLastName().equals(newStaff.getLastName())
                                    && staff.getRole().equals(newStaff.getRole())) {
                                // user exist in this timeSlot
                                logger.error("User exist !");
                                return false;
                            }
                        }
                    }
                }
            }

            // insert newStaff in schedule
            for (Day day : schedule.getDays()) {
                for (TimeSlot timeSlot : day.getTimeSlots()) {
                    //System.out.println(timeSlot.datetime + " " + newStaff.timeSlot);
                    if (timeSlot.getDatetime().equals(newStaff.getTimeSlot())) {
                       // add new staff in schedule
                        logger.info("insert new staff in " + timeSlot);
                        timeSlot.getScheduledStaffs().add(newStaff);
                        break;
                    }
                }
            }
            //System.out.println(res);
            //System.out.println("Save " + newStaff);
            // replace docs par res dans la base de donneés
            //System.out.println("Drop collection !!");
            collection.drop();
            saveSchedule(schedule);
        } catch (Exception e) {
            logger.error("Probleme pour sauvegarder l'employer : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public void dropCollection() {
        if (this.collection != null)
            this.collection.drop();
    }
}
