package it.unipi.dii.lsmsdb.boardgamecafe.services;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
import it.unipi.dii.lsmsdb.boardgamecafe.BoardgamecafeApplication;  //Used For neo4j ops handle
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.UserTest;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.PhoneMongo;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.ReviewMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserMongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
public class ServiceUser {

    @Autowired
    private UserMongo userMongo;

    //@Autowired
    //private ReviewMongoDB reviewMongo;
    //@Autowired
    //private PhoneMongo phoneMongo;  //to be replaced with BoardgameMongoDB properly

    private final static Logger logger = LoggerFactory.getLogger(ServiceUser.class);

    public String getHashedPassword(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
    public String getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public UserTest createUser(String id, String username, String email, String password,
                                String name, String surname, String gender,
                                String nationality, String banned, int year,
                                int month, int day)
    {
        //LocalDate localDate = LocalDate.now();
        //LocalDate birthday = LocalDate.of(year,month,day);
        //int age = Period.between(birthday,localDate).getYears();

        boolean bannedUser = false;
        String adminChoice = "NotBanned";

        Date dateOfBirth = new GregorianCalendar(year, month-1, day+1).getTime();

        String salt = this.getSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        if(!adminChoice.equals(banned))
            bannedUser = true;

        return new UserTest(id,username,email,hashedPassword,
                            salt, name, surname,
                            gender,dateOfBirth,
                            nationality,bannedUser);
    }

    public boolean insertUser(UserTest user) {

        boolean result = true;
        if (!userMongo.addUser(user)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }

        //Gestione GraphDB temporarily unused
        if (!BoardgamecafeApplication.getInstance().getUserNeo4j().addUser(user.getId(), user.getUsername())) {
            logger.error("Error in adding the user to Neo4j");
            if (!userMongo.deleteUser(user)) {
                logger.error("Error in deleting the user from MongoDB");
            }
            return false;
        }

        return result;
    }

    public boolean deleteUser(UserTest user) {

        String username = user.getUsername();
        String userId = user.getId();
        try {
            if (!userMongo.deleteUser(user)) {
                logger.error("Error in deleting the user from the user collection");
                return false;
            }

            //Gestion GraphDB temporarily unused
            /*
            if (!App.getInstance().getUserNeo4j().deleteUserAddsRelationships(userId)) {
                logger.error("Error in deleting the user's add relationships");
                return false;
            }
            if (!App.getInstance().getUserNeo4j().deleteUserFollowsRelationships(userId)) {
                logger.error("Error in deleting the user's follow relationships");
                return false;
            }
            if (!App.getInstance().getUserNeo4j().deleteUserOnly(userId)) {
                logger.error("Error in deleting the user from neo4j");
                return false;
            }
            if (!reviewMongo.updateReviewsOldUser(username)) {
                logger.error("Error in removing the username from the reviews collection");
                return false;
            }
            if (!phoneMongo.updatePhoneReviewsOldUser(username)) {
                logger.error("Error in removing the username from the reviews in phones");
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
