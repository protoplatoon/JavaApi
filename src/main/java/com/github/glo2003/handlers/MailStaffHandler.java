package com.github.glo2003.handlers;

import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailStaffHandler implements Route {
    static final Logger logger = Logger.getLogger(MailStaffHandler.class);

    @Override
    public Object handle(Request request, Response response) {
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

        String mail = request.params(":mail");
        String timeSlot = request.params(":timeSlot");

        /* L'adresse de l'expéditeur */
//        String from = "monAdresse@monDomaine.fr";
        String from = "ottoteam17@gmail.com";
        /* L'adresse du destinataire */
        String to = mail;
        /* L'objet du message */
        String objet = "Schedule update";
        /* Le corps du mail */
        String texte = "You have been had to the schedule : ";
        texte += date;
        texte += " at ";
        texte += timeSlot;

        Properties props = System.getProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        /* Session encapsule pour un client donné sa connexion avec le serveur de mails.*/
        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getDefaultInstance(props, auth);

        /* Création du message*/
        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
            msg.setSubject(objet);
            msg.setText(texte);
            msg.setHeader("X-Mailer", "LOTONtechEmail");
            Transport.send(msg);
        }
        catch (AddressException e) {
            logger.error("Mail failed : Address Exception");
            response.status(400);
            return false;
        }
        catch (MessagingException e) {
            logger.error("Mail failed : Messaging exception");
            response.status(400);
            return false;
        }
        return true;
      }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = "ottoteam17@gmail.com";
            String password = "ottotem17";
            return new PasswordAuthentication(username, password);
        }
    }
}
