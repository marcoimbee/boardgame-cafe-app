package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
//import it.unipi.dii.lsmsdb.phoneworld.model.GenericUser;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMongoDB {

    public UserMongoDB() {
    }

    //private final static Logger logger = LoggerFactory.getLogger(Boardgame.class);

    @Autowired
    private UserRepositoryMongo userMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public UserRepositoryMongo getUserMongo() {
        return userMongo;
    }

    public boolean addUser(UserMongo user) {
        boolean result = true;
        try {
            userMongo.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    public boolean deleteUser(UserMongo user) {
        try {
            userMongo.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<UserMongo> findByUsername(String username) {
        Optional<UserMongo> user = Optional.empty();
        try {
            user = userMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public Optional<UserMongo> findUserById(String id) {
        Optional<UserMongo> user = Optional.empty();
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