package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserNeo4jDB {

    @Autowired
    UserRepositoryNeo4j userNeo4jDB;
    @Autowired
    Neo4jOperations neo4jOperations; //useful for aggregation


    public UserRepositoryNeo4j getUserNeo4jDB() {
        return userNeo4jDB;
    }

    public boolean addUser(UserNeo4j user) {
        boolean result = true;
        try {
            userNeo4jDB.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<UserNeo4j> getFollowed(String userId) {
        List<UserNeo4j> userFollowed = new ArrayList<>();
        try {
            return userNeo4jDB.findFollowed(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userFollowed;
    }


    public List<UserNeo4j> getFollowedList(String userId) {
        List<UserNeo4j> userFollowed = new ArrayList<>();
        try {
            userFollowed.addAll(userNeo4jDB.findFollowed(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userFollowed;
    }

    public List<UserNeo4j> getFollowers(String userId) {
        List<UserNeo4j> result = new ArrayList<>();
        try {
            return userNeo4jDB.findFollowers(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
