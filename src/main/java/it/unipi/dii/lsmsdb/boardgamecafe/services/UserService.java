package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
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
import org.neo4j.cypherdsl.core.Use;
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
    private CommentDBMongo commentMongoOp;
    @Autowired
    private CommentDBNeo4j commentNeo4jOp;
    @Autowired
    private BoardgameDBMongo boardgameMongoOp;

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public AdminModelMongo createAdmin(String username,
                                       String email,
                                       String password){

        String salt = this.getSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        return new AdminModelMongo(null, username, email, salt, hashedPassword, "admin");
    }

    public boolean insertAdmin(AdminModelMongo admin) {
        if (!userMongoDB.addUser(admin)) {
            logger.error("Error in adding the admin to MongoDB");
            return false;
        }
        return true;
    }

    public UserModelMongo createUser(String username, String email, String password,
                                     String name, String surname, String gender,
                                     String nationality, int year,
                                     int month, int day)
    {
        Date dateOfBirth = new GregorianCalendar(year, month-1, day+1).getTime();
        String salt = this.getSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        return new UserModelMongo(null, username,hashedPassword,
                salt, "user",email, name, surname,
                gender,dateOfBirth,
                nationality, false);
    }

    public boolean insertUser(UserModelMongo user) {
        // MongoDB
        if (!userMongoDB.addUser(user)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }

        // Update id
        user = (UserModelMongo) userMongoDB.findByUsername(user.getUsername()).get();

        // Neo4j
        if (!userNeo4jDB.addUser(new UserModelNeo4j(user.getId(), user.getUsername()))) {
            logger.error("Error in adding the user to Neo4j");
            if (!userMongoDB.deleteUser(user)) {
                logger.error("Error in deleting the user from MongoDB");
            }
            return false;
        }

        return true;
    }

    private boolean removeUserReviews(UserModelMongo user) {
        for (ReviewModelMongo review : user.getReviews()) {
            Optional<BoardgameModelMongo> boardgameResult = boardgameMongoOp.findBoardgameByName(review.getBoardgameName());
            if (boardgameResult.isPresent()) {
                BoardgameModelMongo boardgame = boardgameResult.get();
                boardgame.deleteReview(review.getId());
                if (!boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame)) {
                    logger.error("Error in deleting review inside boardgame collection in MongoDB");
                    return false;
                }
            }
        }

        if (!reviewMongoOp.deleteReviewByUsername(user.getId())) {
            logger.error("Error in deleting reviews written by user in MongoDB");
            return false;
        }
        return true;
    }
    private boolean removeUserPosts(String username) {
        Optional<UserModelNeo4j> userResult = userNeo4jDB.findByUsername(username);
        if (userResult.isEmpty()) {
            return true;
        }
        UserModelNeo4j user = userResult.get();

        // Delete comments in posts written by the user
        for (PostModelNeo4j post : user.getWrittenPosts()) {
            // MongoDB
            if (!commentMongoOp.deleteByPost(post.getId())) {
                logger.error("Error deleting comments in post written by user in MongoDB");
                return false;
            }
            // Neo4j
            if (!commentNeo4jOp.deleteByPost(post.getId())) {
                logger.error("Error deleting comments in post written by user in Neo4j");
                return false;
            }
        }

        // Delete posts
        // MongoDB
        if (!postMongoOp.deleteByUsername(user.getUsername())) {
            logger.error("Error deleting posts written by user in MongoDB");
            return false;
        }
        // Neo4j
        if (!postNeo4jOp.deleteByUsername(user.getUsername())) {
            logger.error("Error deleting posts written by user in Neo4j");
            return false;
        }
        return true;
    }
    private boolean removeUserComments(String username) {
        // Update comments in post collection
        for (CommentModelMongo comment : commentMongoOp.findByUsername(username)) {
            Optional<PostModelMongo> postResult = postMongoOp.findById(comment.getPost());
            if (postResult.isPresent()) {
                PostModelMongo post = postResult.get();
                post.deleteCommentInPost(comment.getId());
                comment.setUsername("[deleted]");
                comment.setText("[deleted]");
                post.addComment(comment);
                if (!postMongoOp.updatePost(post.getId(), post)) {
                    logger.error("Error in deleting user comments in post collection in MongoDB");
                    return false;
                }
            }
        }

        // Delete Comments
        // MongoDB
        if (!commentMongoOp.deleteByUsername(username)) {
            logger.error("Error in deleting user comments in MongoDB");
            return false;
        }
        // Neo4j
        if (!commentNeo4jOp.deleteByUsername(username)) {
            logger.error("Error in deleting user comments in Neo4j");
            return false;
        }
        return true;
    }

    public boolean banUser(UserModelMongo user) {
        String username = user.getUsername();
        user.setBanned(true);

        if (!userMongoDB.updateUser(user.getId(), user, "user")) {
            logger.error("Error in setting banned flag in user");
            return false;
        }
        return removeUserReviews(user) && removeUserPosts(username) && removeUserComments(username);
    }

    public boolean deleteUser(UserModelMongo user) {
        String username = user.getUsername();

        if (!removeUserReviews(user) || !removeUserPosts(username) || !removeUserComments(username))
            return false;

        try {
            // MongoDB
            if (!userMongoDB.deleteUser(user)) {
                logger.error("Error in deleting the user from the user collection in MongoDB");
                return false;
            }

            // Neo4jDB
            if (!userNeo4jDB.deleteUserDetach(username)) {
                logger.error("Error in deleting the user in Neo4j");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<UserModelMongo> suggestUsersByCommonBoardgamePosted(String username, int limit)
    {
        List<String> suggestedNeo4jUsers = userNeo4jDB.getUsersByCommonBoardgamePosted(username, limit);
        List<UserModelMongo> suggestedMongoUsers = new ArrayList<>();
        for (String suggestedUsername : suggestedNeo4jUsers )
        {
            Optional<GenericUserModelMongo> suggestedMongoUser = userMongoDB.findByUsername(suggestedUsername);
            // If the suggestedMongoUser is found, then it's added to the suggestedMongoUsers list
            suggestedMongoUser.ifPresent(genericUserModelMongo -> suggestedMongoUsers.add((UserModelMongo) genericUserModelMongo));
        }
        return suggestedMongoUsers;
    }

}