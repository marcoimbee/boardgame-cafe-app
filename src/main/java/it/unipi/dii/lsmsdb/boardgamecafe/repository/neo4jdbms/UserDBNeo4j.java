package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDBNeo4j {

    @Autowired
    UserRepoNeo4j userNeo4jDB;
    @Autowired
    Neo4jOperations neo4jOperations; //useful for aggregation

    public UserRepoNeo4j getUserNeo4jDB() {
        return userNeo4jDB;
    }

    public boolean addUser(UserModelNeo4j user) {
        boolean result = true;
        try {
            userNeo4jDB.save(user); //L'equivalente di MERGE
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUser(UserModelNeo4j user) {
        try {
            userNeo4jDB.delete(user);   //Default method (elimina soltanto l'utente)
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteUserDetach(String username) {
        try {
            userNeo4jDB.deleteUserDetachByUsername(username); //Detach esplicito in repositoryINF
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<UserModelNeo4j> getFollowing(String username) {
        List<UserModelNeo4j> following = new ArrayList<>();
        try {
            return userNeo4jDB.findFollowingByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return following;
    }

    public List<UserModelNeo4j> getFollowers(String username) {
        List<UserModelNeo4j> followers = new ArrayList<>();
        try {
            return userNeo4jDB.findFollowersByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return followers;
    }

}
