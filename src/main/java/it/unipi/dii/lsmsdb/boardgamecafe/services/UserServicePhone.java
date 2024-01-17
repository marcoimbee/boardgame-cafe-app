/*
package it.unipi.dii.lsmsdb.phoneworld.services;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
//import it.unipi.dii.lsmsdb.phoneworld.model.User;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.PhoneMongo;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.ReviewMongo;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.UserMongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
public class UserService {

    @Autowired
    private UserMongoDB userMongo;
    @Autowired
    private ReviewMongoDB reviewMongo;
    @Autowired
    private PhoneMongo phoneMongo;  //to be replaced with BoardgameMongoDB properly

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

    public Admin createAdmin(String username, String password){
        String salt = this.getSalt();
        String hashedPassword = this.getHashedPassword(password, salt);
        return new Admin(username, salt, hashedPassword, "admin");
    }

    public User createUser(String firstName, String lastName, String gender, String country, String city,
                           String streetName, int streetNumber, String email, String username, String password,
                           int year, int month, int day) {
        LocalDate localDate = LocalDate.now();
        LocalDate birthday = LocalDate.of(year,month,day);
        int age = Period.between(birthday,localDate).getYears();
        Date dateOfBirth = new GregorianCalendar(year, month-1, day+1).getTime();
        String salt = this.getSalt();
        String hashedPassword = this.getHashedPassword(password, salt);
        return new User(username,salt,hashedPassword,"user",gender,firstName,lastName,streetNumber,streetName,
                city,country, email,dateOfBirth,age);
    }

    public boolean insertAdmin(Admin admin) {
        boolean result = true;
        if (!userMongo.addUser(admin)) {
            logger.error("Error in adding the admin to MongoDB");
            return false;
        }
        return result;
    }

    public boolean insertUser(User user) {
        boolean result = true;
        if (!userMongo.addUser(user)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }
        if (!App.getInstance().getUserNeo4j().addUser(user.getId(), user.getUsername())) {
            logger.error("Error in adding the user to Neo4j");
            if (!userMongo.deleteUser(user)) {
                logger.error("Error in deleting the user from MongoDB");
            }
            return false;
        }
        return result;
    }

    public boolean deleteUser(User user) {
        String username = user.getUsername();
        String userId = user.getId();
        try {
            if (!userMongo.deleteUser(user)) {
                logger.error("Error in deleting the user from the user collection");
                return false;
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
*/
