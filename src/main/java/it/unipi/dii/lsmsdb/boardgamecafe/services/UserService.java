package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.*;

@Component
public class UserService {

    @Autowired
    private UserDBMongo userMongoDB;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private PostDBMongo postMongoOp;
    @Autowired
    private PostDBNeo4j postNeo4jOp;
    @Autowired
    private BoardgameDBMongo boardgameMongoOp;

    public String getHashedPassword(String passwordToHash, String salt) {
        String generatedPassword;
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
            return generatedPassword;
        } catch (Exception ex) {
            System.err.println("[ERROR] getHashedPassword()@UserService.java raised an exception: " + ex.getMessage());
            return null;
        }
    }

    public String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public UserModelMongo createUser(String username, String email, String password,
                                     String name, String surname, String gender,
                                     String nationality, int year,
                                     int month, int day)
    {
        /*
            The date gets created using LocalDate because it should just represent a date with no information
            about time or jet lag. The use of such class completely avoids the increment or the decrement of the day
            caused by jet lag conversions, maintaining in a more reliable way the desired date and avoiding potential
            implicit conversions due to potential differences in dates management between framework and DB.
         */
        LocalDate dateOfBirth = LocalDate.of(year, month, day);
        String salt = this.generateSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        // LocalDate is converted to Date in UTC to insert it into MongoDB (this is a mandatory step)
        Date dateOfBirthInUTC = Date.from(dateOfBirth.atStartOfDay(ZoneId.of("UTC")).toInstant());

        return new UserModelMongo(username,email,name, surname,
                gender,dateOfBirthInUTC, nationality, false,
                salt, hashedPassword, "user");
    }

    @Transactional
    public boolean insertUser(UserModelMongo user) {
        try {
            if (userMongoDB.findByUsername(user.getUsername(), false).isPresent()) {  // Username uniqueness check
                throw new Exception("Unable to insert an already existing user");
            }

            // MongoDB insert
            if (!userMongoDB.addUser(user)) {       // Adding the new user in MongoDB
                throw new Exception("Failed to insert the new user in MongoDB");
            }

            // Getting the ID of the newly created user from MongoDB
            Optional<GenericUserModelMongo> createdUserOptional = userMongoDB.findByUsername(user.getUsername(), false);
            if (createdUserOptional.isEmpty()) {
                userMongoDB.deleteUser(user);
                throw new Exception("Failed to retrieve newly inserted user from MongoDB");
            }

            user = (UserModelMongo) createdUserOptional.get();   // This user has an ID

            // Neo4j insert
            UserModelNeo4j newNeo4jUser = new UserModelNeo4j(user.getId(), user.getUsername());
            if (!userNeo4jDB.addUser(newNeo4jUser)) {
                userMongoDB.deleteUser(user);               // MongoDB rollback if anything goes wrong
                throw new Exception("Failed to insert new user in Neo4J");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] insertUser()@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private HashMap<String, List<Integer>> deleteUserReviews(UserModelMongo user) {
        try {
            // Deleting the reviews in the reviews collection, getting a map in which for each reviewed
            // boardgame we have the number of reviews that got deleted
            HashMap<String, List<Integer>> deletedReviewsForBoardgame = reviewMongoOp.deleteReviewByUsername(user.getUsername());
            if (deletedReviewsForBoardgame == null) {
                throw new Exception("Failed to delete the user's reviews from the 'Reviews' collection");
            }
            return deletedReviewsForBoardgame;
        } catch (Exception ex) {
            System.err.println("[ERROR] removeUserReviews()@UserService.java raised an exception: " + ex.getMessage());
            return null;
        }
    }

    private boolean deleteUserPosts(String username) {
        try {
            // Delete actual post documents and nodes
            if (!postNeo4jOp.deleteByUsername(username)) {      // Neo4J post nodes deletion
                return false;
            }
            if (!postMongoOp.deleteByUsername(username)) {      // MongoDB deletion from posts collection
                return false;
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteUserPosts()@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deleteUser(UserModelMongo user) {
        try {
            /*
                Posts of a deleted user
                    -> delete from the collection
                Comments of a deleted user (inside a Post)
                    -> delete from comments array
                Reviews of a deleted user
                    -> delete from the collection
                Boardgames
                    -> Update reviewCounter (if he wrote some reviews, those get deleted, so the
                       counter of the boardgame must be updates as well)
             */
            String username = user.getUsername();

            // Neo4j user deletion
            if (!userNeo4jDB.deleteUserDetach(username)) {
                throw new Exception("Failed to delete the user from Neo4J");
            }

            // Posts management
            if (!deleteUserPosts(username)) {
                throw new Exception("Failed to delete user posts");
            }

            // Comments management - deletion of comments under posts the user had commented
            if (!postMongoOp.deleteCommentsAfterUserDeletion(username)) {
                throw new Exception("Failed to delete user comments");
            }

            // Reviews management - deletion of documents + update of reviews under boardgames
            HashMap<String, List<Integer>> deletedReviewsForBoardgame = deleteUserReviews(user);
            if (deletedReviewsForBoardgame == null) {
                throw new Exception("Failed to delete user reviews");
            }

            // Boardgames management - update reviewCount field, if the user we are deleting had reviewed some boardgames
            if (!deletedReviewsForBoardgame.isEmpty()) {            // Do the following only if the user had some reviews
                for (Map.Entry<String, List<Integer>> hashMapElement : deletedReviewsForBoardgame.entrySet()) {
                    String reviewedBoardgame = hashMapElement.getKey();
                    List<Integer> ratings = hashMapElement.getValue();    // Get boardgame name and #reviews of the user

                    if (!boardgameMongoOp.updateRatingAfterUserDeletion(reviewedBoardgame, ratings)) {
                        throw new Exception("Failed to update a boardgame's rating and reviewCount after a user deletion");
                    }
                }
            }

            // MongoDB user deletion
            if (!userMongoDB.deleteUser(user)) {
                throw new Exception("Failed to delete the user from MongoDB collection");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteUser()@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public List<UserModelMongo> suggestUsersByCommonBoardgamePosted(String username, int limit, int skipCounter) {
        try {
            List<String> suggestedNeo4jUsers = userNeo4jDB.getUsersByCommonBoardgamePosted(username, limit, skipCounter);
            List<UserModelMongo> suggestedMongoUsers = new ArrayList<>();
            for (String suggestedUsername : suggestedNeo4jUsers) {
                Optional<GenericUserModelMongo> suggestedMongoUser = userMongoDB.findByUsername(suggestedUsername, false);
                suggestedMongoUser.ifPresent(
                        genericUserModelMongo -> {
                            UserModelMongo user = (UserModelMongo) genericUserModelMongo;
                            if (!"admin".equals(user.get_class())) {    // Exclude users with _class = "admin"
                                suggestedMongoUsers.add(user);
                            }
                        }
                );
            }
            return suggestedMongoUsers;
        } catch (Exception ex) {
            System.err.println("[ERROR] suggestUsersByCommonBoardgamePosted()@UserService.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserModelMongo> suggestInfluencerUsers(
            long minFollowersCount,
            int mostFollowedUsersLimit,
            long minAvgLikeCount,
            int influencerUsersLimit)
    {
        try {
            List<String> mostFollowedUsersUsernamesNeo = userNeo4jDB.getMostFollowedUsersUsernames(
                    minFollowersCount,
                    mostFollowedUsersLimit
            );

            List<String> mostFollowedUsersWithHighestAvgLikeCountIdsMongo = userMongoDB.findMostFollowedUsersWithMinAverageLikesCountUsernames(
                    mostFollowedUsersUsernamesNeo,
                    minAvgLikeCount,
                    influencerUsersLimit
            );

            List<UserModelMongo> suggestedInfluencers = new ArrayList<>();
            for (String influencerUsername : mostFollowedUsersWithHighestAvgLikeCountIdsMongo) {
                Optional<GenericUserModelMongo> suggestedInfluencer = userMongoDB.findByUsername(influencerUsername, false);
                suggestedInfluencer.ifPresent(
                        genericUserModelMongo -> {
                            UserModelMongo user = (UserModelMongo) genericUserModelMongo;
                            if (!"admin".equals(user.get_class())) {        // Exclude users with _class = "admin"
                                suggestedInfluencers.add(user);
                            }
                        }
                );
            }
            return suggestedInfluencers;
        } catch (Exception ex) {
            System.err.println("[ERROR] suggestInfluencerUsers()@UserService.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserModelMongo> suggestUsersByCommonLikedPosts(String username, int limit, int skipCounter) {
        try {
            List<String> suggestedNeo4jUsers = userNeo4jDB.getUsersBySameLikedPosts(username, limit, skipCounter);
            List<UserModelMongo> suggestedMongoUsers = new ArrayList<>();
            for (String suggestedUsername : suggestedNeo4jUsers) {
                Optional<GenericUserModelMongo> suggestedMongoUser = userMongoDB.findByUsername(suggestedUsername, false);
                suggestedMongoUser.ifPresent(
                        genericUserModelMongo -> {
                            UserModelMongo user = (UserModelMongo) genericUserModelMongo;
                            if (!"admin".equals(user.get_class())) {            // Exclude users with _class = "admin"
                                suggestedMongoUsers.add(user);
                            }
                        }
                );
            }
            return suggestedMongoUsers;
        } catch (Exception ex) {
            System.err.println("[ERROR] suggestUsersByCommonLikedPosts()@UserService.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean banUser(UserModelMongo user) {
        try {
            // Setting MongoDB 'banned' flag to true
            user.setBanned(true);
            if (!userMongoDB.updateUser(user.getId(), user, "user")) {
                throw new Exception("Failed to set 'banned' MongoDB flag in 'Users' collection.");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] banUser()@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean unbanUser(UserModelMongo user) {
        try {
            if (user.isBanned()) {
                String userId = user.getId();

                // Setting MongoDB 'banned' flag to false - MongoDB
                user.setBanned(false);
                if (!userMongoDB.updateUser(userId, user, "user")) {
                    throw new Exception("Failed to unset 'banned' MongoDB flag");
                }
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] unbanUser()@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public HashMap<String, Double> getAvgAgeByNationality(int limit) {
        HashMap<String, Double> avgAgeByNationality = new HashMap<>();
        try {
            Document docResult = this.userMongoDB.showUserAvgAgeByNationality(limit).get();

            for (Document doc : (List<Document>)docResult.get("results")) {
                String country = doc.getString("_id");
                Double avgAge = doc.getDouble("averageAge");
                avgAgeByNationality.put(country, avgAge);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] getAvgAgeByNationality()@UserService.java raised an exception: " + e.getMessage());
        }
        return avgAgeByNationality;
    }

    public LinkedHashMap<String, Integer> getCountriesWithMostUsers(int minUserNumber, int limit) {
        LinkedHashMap<String, Integer> avgAgeByNationality = new LinkedHashMap<>();
        try {
            Document docResult = this.userMongoDB.findCountriesWithMostUsers(minUserNumber, limit);
            for (Document doc : (List<Document>)docResult.get("results")) {
                String country = doc.getString("_id");
                Integer usersNumber = doc.getInteger("numUsers");
                avgAgeByNationality.put(country, usersNumber);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] getCountriesWithMostUsers()@UserService.java raised an exception: " + e.getMessage());
        }
        return avgAgeByNationality;
    }
}
