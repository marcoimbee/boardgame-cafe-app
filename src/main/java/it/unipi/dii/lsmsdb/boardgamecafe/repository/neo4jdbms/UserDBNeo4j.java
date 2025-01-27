package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserDBNeo4j {

    @Autowired
    UserRepoNeo4j userNeo4jDB;
    @Autowired
    Neo4jOperations neo4jOperations;

    public boolean addUser(UserModelNeo4j user) {
        try {
            userNeo4jDB.save(user);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] addUser()@UserDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(String id, UserModelNeo4j updated) {
        try {
            Optional<UserModelNeo4j> old = userNeo4jDB.findById(id);
            if (old.isPresent()) {
                UserModelNeo4j oldUser = old.get();
                oldUser.setUsername(updated.getUsername());
                oldUser.setFollowedUsers(updated.getFollowedUsers());
                oldUser.setWrittenPosts(updated.getWrittenPosts());
                oldUser.setLikedPosts(updated.getLikedPosts());
                userNeo4jDB.save(oldUser);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[ERROR] updateUser()@UserDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUserDetach(String username) {
        try {
            userNeo4jDB.deleteAndDetachUserByUsername(username);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteUserDetach()@UserDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public Optional<UserModelNeo4j> findByUsername(String username) {
        Optional<UserModelNeo4j> user = Optional.empty();
        try {
            user = userNeo4jDB.findByUsername(username);
        }
        catch (Exception e) {
            System.err.println("[ERROR] findByUsername()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return user;
    }

    public void followUser(String following, String followed) {
        try {
            userNeo4jDB.addFollowRelationship(following, followed);
        } catch (Exception e) {
            System.err.println("[ERROR] followUser()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
    }

    public void unfollowUser(String unfollowing, String unfollowed) {
        try {
            userNeo4jDB.removeFollowRelationship(unfollowing, unfollowed);
        } catch (Exception e) {
            System.err.println("[ERROR] unfollowUser()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
    }

    public List<String> getFollowedUsernames(String username) {
        List<String> followers = new ArrayList<>();
        try {
            return userNeo4jDB.findFollowedUsernamesByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] getFollowedUsernames()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return followers;
    }

    public int getCountFollowing(String username) {
        try {
            return userNeo4jDB.countFollowingByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] getCountFollowing()@UserDBNeo4j.java raised an exception: " + e.getMessage());
            return 0;
        }
    }

    public int getCountFollowers(String username) {
        try {
            return userNeo4jDB.countFollowersByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] getCountFollowers()@UserDBNeo4j.java raised an exception: " + e.getMessage());
            return 0;
        }
    }

    public List<String> getUsersByCommonBoardgamePosted(String username, int limit, int skipCounter) {
        List<String> suggestedUsers = new ArrayList<>();
        try {
            return userNeo4jDB.usersByCommonBoardgamePosted(username, limit, skipCounter);
        } catch (Exception e) {
            System.err.println("[ERROR] getUsersByCommonBoardgamePosted()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return suggestedUsers;
    }

    public List<String> getUsersBySameLikedPosts(String username, int limit, int skipCounter) {
        List<String> suggestedUsers = new ArrayList<>();
        try {
            return userNeo4jDB.findUsersBySameLikedPosts(username, limit, skipCounter);
        } catch (Exception e) {
            System.err.println("[ERROR] getUsersBySameLikedPosts()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return suggestedUsers;
    }

    public List<String> getMostFollowedUsersUsernames(long minFollowersCount, int limit) {
        List<String> mostFollowedUsersUsernames = new ArrayList<>();
        try {
            return userNeo4jDB.findMostFollowedUsersUsernames(minFollowersCount, limit);
        } catch (Exception e) {
            System.err.println("[ERROR] getMostFollowedUsersUsernames()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return mostFollowedUsersUsernames;
    }

    public Optional<UserModelNeo4j> findById(String id) {
        Optional<UserModelNeo4j> user = Optional.empty();
        try {
            user = userNeo4jDB.findById(id);
        }
        catch (Exception e) {
            System.err.println("[ERROR] findById()@UserDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return user;
    }

    public List<String> getFollowedUsernamesWhoCreatedAtLeastOnePost(String username, int limitResults, int skipCounter) {
        List<String> followedUsernames = new ArrayList<>();
        try { followedUsernames = userNeo4jDB.findFollowedUsernamesWhoCreatedAtLeastOnePost(username, limitResults, skipCounter); }
        catch (Exception ex) {
            System.err.println("[ERROR] getFollowedUsernamesWhoCreatedAtLeastOnePost()@UserDBNeo4j.java raised an exception: " + ex.getMessage());
        }
        return followedUsernames;
    }
}
