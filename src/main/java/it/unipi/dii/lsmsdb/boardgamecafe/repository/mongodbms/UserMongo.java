package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
//import it.unipi.dii.lsmsdb.phoneworld.model.GenericUser;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.UserTest;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class UserMongo {

    public UserMongo() {
    }

    //private final static Logger logger = LoggerFactory.getLogger(Boardgame.class);

    @Autowired
    private INFUserMongoDB userMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public INFUserMongoDB getUserMongo() {
        return userMongo;
    }

    public boolean addUser(UserTest user) {
        boolean result = true;
        try {
            userMongo.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUser(UserTest user) {
        try {
            userMongo.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<UserTest> findByUsername(String username) {
        Optional<UserTest> user = Optional.empty();
        try {
            user = userMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public Optional<UserTest> findUserById(String id) {
        Optional<UserTest> user = Optional.empty();
        try {
            user = userMongo.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean deleteUserById(String id) {
        try {
            userMongo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}