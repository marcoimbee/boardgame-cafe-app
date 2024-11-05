package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.UserContentUpdateReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
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
            return generatedPassword;
        } catch (Exception ex) {
            System.err.println("[ERROR] getHashedPassword@UserService.java raised an exception: " + ex.getMessage());
            return null;
        }
    }

    public String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public AdminModelMongo createAdmin(String username,
                                       String email,
                                       String password)
    {
        String salt = this.generateSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        return new AdminModelMongo(null, username, email, salt, hashedPassword, "admin");
    }

    @Transactional
    public boolean insertAdmin(AdminModelMongo admin) {
        try {
            if (!userMongoDB.addUser(admin)) {
                throw new Exception("Error while adding an administrator user to MongoDB.");
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] insertAdmin@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public UserModelMongo createUser(String username, String email, String password,
                                     String name, String surname, String gender,
                                     String nationality, int year,
                                     int month, int day)
    {
        Date dateOfBirth = new GregorianCalendar(year, month - 1, day + 1).getTime();
        String salt = this.generateSalt();
        String hashedPassword = this.getHashedPassword(password, salt);

        return new UserModelMongo(username,email,name, surname,
                gender,dateOfBirth, nationality, false,
                salt, hashedPassword, "user");
    }

    @Transactional
    public boolean insertUser(UserModelMongo user) {
        try {
            if (userMongoDB.findByUsername(user.getUsername()).isPresent()) {       // Username uniqueness check
                throw new Exception("Unable to insert an already existing user");
            }

            // MongoDB insert
            if (!userMongoDB.addUser(user)) {       // Adding the new user in MongoDB
                throw new Exception("Failed to insert the new user in MongoDB");
            }

            // Getting the ID of the newly created user from MongoDB
            Optional<GenericUserModelMongo> createdUserOptional = userMongoDB.findByUsername(user.getUsername());
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
            System.err.println("[ERROR] insertUser@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean deleteUserReviews(UserModelMongo user) {
        try {
            // Deleting the reviews collection - WORKING
            if (!reviewMongoOp.deleteReviewByUsername(user.getUsername())) {
                throw new Exception("Failed to delete the user's reviews from their collection");
            }

            // Deleting each review from the boardgame they refer to
            /*
            List<ReviewModelMongo> userReviews = user.getReviews();
            System.out.println("[DEBUG] #reviews by " + user.getUsername() + ": " + userReviews.size());
            if (!userReviews.isEmpty()) {           // If the list is empty, nothing to be done
                for (ReviewModelMongo review : userReviews) {
                    System.out.println("[DEBUG] review: " + review);
                    Optional<BoardgameModelMongo> reviewedBoardgameOptional = boardgameMongoOp.findBoardgameByName(review.getBoardgameName());
                    if (reviewedBoardgameOptional.isPresent()) {
                        BoardgameModelMongo reviewedBoardgame = reviewedBoardgameOptional.get();
                        System.out.println("[DEBUG] refers to this boardgame: " + reviewedBoardgame);
                        reviewedBoardgame.deleteReview(review.getId());
                        if (!boardgameMongoOp.updateBoardgameMongo(reviewedBoardgame.getId(), reviewedBoardgame)) {
                            throw new Exception("Failed to update MongoDB boardgame");
                        }
                        System.out.println("[DEBUG] successfully deleted this review");
                    }
                }
            }*/

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] removeUserReviews@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean deleteUserPosts(String username) {
        try {
            // Delete comment documents and nodes that were done under posts written by the user that has to be deleted
            List<PostModelMongo> postsByUser = postMongoOp.findByUsername(username);
            if (!postsByUser.isEmpty()) {           // If no posts were written, do not lose time in calling these methods
                System.out.println("[DEBUG] postsByUser: " + postsByUser);
                for (PostModelMongo post : postsByUser) {
                    System.out.println("[DEBUG] post: " + post);
                    if (!commentMongoOp.deleteByPost(post.getId())) {     // Deleting comments from MongoDB collection
                        throw new Exception("Failed to delete MongoDB comment under a MongoDB post");
                    }
                    if (!commentNeo4jOp.deleteByPost(post.getId())) {     // Deleting comment nodes from Neo4J
                        throw new Exception("Failed to delete Neo4J comment related to a Neo4J post");
                    }
                }
            }

            // Delete actual post documents and nodes
            if (!postMongoOp.deleteByUsername(username)) {      // MongoDB deletion from posts collection
                return false;
            }
            if (!postNeo4jOp.deleteByUsername(username)) {      // Neo4J post nodes deletion
                return false;
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteUserPosts@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean updateUserCommentsAfterAdminAction(String username, UserContentUpdateReason updateReason) {
        try {
            // Updating MongoDB posts where the user had commented
            List<CommentModelMongo> userComments = new ArrayList<>();   // Will be unused and empty in case updateReason is DELETED_USER or BANNED_USER
            if (updateReason == UserContentUpdateReason.UNBANNED_USER) {
                userComments = commentMongoOp.findByUsername(username); // These are needed only if they have to be restored because of an unbanning operation
            }
            if(!postMongoOp.updatePostCommentsAfterAdminAction(username, updateReason, userComments)) {
                throw new Exception("Failed to update posts after the user's ban/deletion/unban");
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updateUserCommentsAfterAdminAction@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean updateUserReviewsAfterAdminAction(String username, UserContentUpdateReason updateReason) {
        try {
            // Updating MongoDB boardgames the user had reviewed
            List<ReviewModelMongo> userReviews = new ArrayList<>();
            if (updateReason == UserContentUpdateReason.UNBANNED_USER) {
                userReviews = reviewMongoOp.findReviewByUsername(username);     // These are needed only if they have to be restored because of an unbanning operation
            }
            if(!boardgameMongoOp.updateBoardgameReviewsAfterAdminAction(username, updateReason, userReviews)) {
                throw new Exception("Failed to update boardgames after the user's ban/deletion/unban");
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updateUserReviewsAfterAdminAction@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean deleteUserComments(String username) {
        try {
            // MongoDB deletion from 'comments' collection
            if (!commentMongoOp.deleteByUsername(username)) {
                throw new Exception("Failed to delete MongoDB user given his username");
            }
            // Neo4j deletion of 'comment' nodes and their relationships
            if (!commentNeo4jOp.deleteByUsername(username)) {
                throw new Exception("Failed to delete Neo4J user given his username");
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteUserComments@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deleteUser(UserModelMongo user) {
        try {
            /*
                Posts of a deleted user
                    -> delete from the collection
                Comments of a deleted user
                    -> delete from the collection
                    -> set 'username' as [Deleted user] under posts the user had commented
                Reviews of a deleted user
                    -> delete from the collection
                    -> set 'username' as [Deleted user] under boardgames the user had reviewed
             */

            String username = user.getUsername();

            // Posts management
            if (!deleteUserPosts(username)) {
                throw new Exception("Failed to delete user posts");
            }

            // Comments management - deletion of documents/nodes + update of comments under posts
            if (!deleteUserComments(username)) {
                throw new Exception("Failed to delete user comments");
            }
            if (!updateUserCommentsAfterAdminAction(username, UserContentUpdateReason.DELETED_USER)) {
                throw new Exception("Failed to update deleted user comments");
            }

            // Reviews management - deletion of documents + update of reviews under boardgames
            if (!deleteUserReviews(user)) {
                throw new Exception("Failed to delete user reviews");
            }
            if (!updateUserReviewsAfterAdminAction(username, UserContentUpdateReason.DELETED_USER)) {
                throw new Exception("Failed to update deleted user reviews");
            }

            // MongoDB
            if (!userMongoDB.deleteUser(user)) {
                throw new Exception("Failed to delete the user from MongoDB collection");
            }

            // Neo4j
            if (!userNeo4jDB.deleteUserDetach(username)) {
                throw new Exception("Failed to delete the user from Neo4J");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteUser@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public List<UserModelMongo> suggestUsersByCommonBoardgamePosted(String username, int limit) {
        try {
            List<String> suggestedNeo4jUsers = userNeo4jDB.getUsersByCommonBoardgamePosted(username, limit);
            List<UserModelMongo> suggestedMongoUsers = new ArrayList<>();
            for (String suggestedUsername : suggestedNeo4jUsers) {
                Optional<GenericUserModelMongo> suggestedMongoUser = userMongoDB.findByUsername(suggestedUsername);
                suggestedMongoUser.ifPresent(       // suggestedMongoUser is found, it gets added to the suggestedMongoUsers list
                        genericUserModelMongo ->
                                suggestedMongoUsers.add((UserModelMongo) genericUserModelMongo)
                );
            }
            return suggestedMongoUsers;
        } catch (Exception ex) {
            System.err.println("[ERROR] suggestUsersByCommonBoardgamePosted@UserService.java raised an exception: " + ex.getMessage());
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
                Optional<GenericUserModelMongo> suggestedInfluencer = userMongoDB.findByUsername(influencerUsername);
                suggestedInfluencer.ifPresent(
                        genericUserModelMongo ->
                                suggestedInfluencers.add((UserModelMongo) genericUserModelMongo)
                );
            }
            return suggestedInfluencers;

        } catch (Exception ex) {
            System.err.println("[ERROR] suggestInfluencerUsers@UserService.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserModelMongo> suggestUsersByCommonLikedPosts(String username, int limit) {
        try {
            List<String> suggestedNeo4jUsers = userNeo4jDB.getUsersBySameLikedPosts(username, limit);
            List<UserModelMongo> suggestedMongoUsers = new ArrayList<>();
            for (String suggestedUsername : suggestedNeo4jUsers) {
                Optional<GenericUserModelMongo> suggestedMongoUser = userMongoDB.findByUsername(suggestedUsername);
                suggestedMongoUser.ifPresent(           // suggestedMongoUser is found, it gets added to the suggestedMongoUsers list
                        genericUserModelMongo ->
                                suggestedMongoUsers.add((UserModelMongo) genericUserModelMongo)
                );
            }
            return suggestedMongoUsers;
        } catch (Exception ex) {
            System.err.println("[ERROR] suggestUsersByCommonLikedPosts@UserService.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean banUser(UserModelMongo user) {
        /*
            Set banned = true in User document - MongoDB
            Set username as [Banned user] - Neo4J node

            Posts of a banned user
                -> no modifications

            Comments of a banned user
                -> no collection modifications
                -> set 'username' and 'text' as [Banned user] under posts the user had commented

            Reviews of a banned user
                -> no collection modifications
                -> set 'username' and 'text' as [Banned user] under boardgames the user had reviewed
         */

        try {
            String username = user.getUsername();

            // Setting MongoDB 'banned' flag to true
            user.setBanned(true);
            if (!userMongoDB.updateUser(user.getId(), user, "user")) {
                throw new Exception("Failed to set 'banned' MongoDB flag");
            }

            // Comments management - update of comments under posts
            if (!updateUserCommentsAfterAdminAction(username, UserContentUpdateReason.BANNED_USER)) {
                throw new Exception("Failed to update banned user comments");
            }

            // Reviews management - update of reviews under boardgames
            if (!updateUserReviewsAfterAdminAction(username, UserContentUpdateReason.BANNED_USER)) {
                throw new Exception("Failed to update banned user reviews");
            }

            // Set username as [Banned user] - Neo4J node
            if (!userNeo4jDB.setUserAsBanned(username)) {
                throw new Exception("Failed to set user to banned in Neo4J");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] banUser@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean unbanUser(UserModelMongo user) {
        try {
            if (user.isBanned()) {
                String userId = user.getId();
                String username = user.getUsername();

                // Setting MongoDB 'banned' flag to false - MongoDB
                user.setBanned(false);
                if (!userMongoDB.updateUser(userId, user, "user")) {
                    throw new Exception("Failed to unset 'banned' MongoDB flag");
                }

                // Comments management - restoring user comments under posts
                if (!updateUserCommentsAfterAdminAction(username, UserContentUpdateReason.UNBANNED_USER)) {
                    throw new Exception("Failed to restore user comments");
                }

                // Reviews management - restoring user reviews under boardgames
                if (!updateUserReviewsAfterAdminAction(username, UserContentUpdateReason.UNBANNED_USER)) {
                    throw new Exception("Failed to restore user reviews");
                }


                // Restoring username - Neo4J node
                if (!userNeo4jDB.restoreUserNodeAfterUnban(userId, username)) {
                    throw new Exception("Failed to restore user node in Neo4J");
                }
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] unbanUser@UserService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }
}
